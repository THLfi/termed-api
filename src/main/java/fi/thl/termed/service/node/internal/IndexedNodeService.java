package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableSet.of;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_REFERRERS_FIELD;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_RESULT_FIELD;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.IndexingQueueItemId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.select.SelectAllReferrers;
import fi.thl.termed.service.node.select.SelectReferrer;
import fi.thl.termed.service.node.specification.NodeIndexingQueueItemsByQueueId;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.dao.SystemDao;
import fi.thl.termed.util.dao.SystemSequenceDao;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.query.CompositeSpecification;
import fi.thl.termed.util.query.DependentSpecification;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.Specifications;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedNodeService extends ForwardingService<NodeId, Node> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Index<NodeId, Node> index;

  private SystemSequenceDao nodeIndexingQueueSequenceDao;
  private SystemDao<Long, Empty> nodeIndexingQueueDao;
  private SystemDao<IndexingQueueItemId<NodeId>, Empty> nodeIndexingQueueItemDao;

  private User indexer = new User("indexer", "", AppRole.ADMIN);
  private Gson gson;

  public IndexedNodeService(
      Service<NodeId, Node> delegate,
      Index<NodeId, Node> index,
      SystemSequenceDao nodeIndexingQueueSequenceDao,
      SystemDao<Long, Empty> nodeIndexingQueueDao,
      SystemDao<IndexingQueueItemId<NodeId>, Empty> nodeIndexingQueueItemDao,
      Gson gson) {
    super(delegate);
    this.index = index;
    this.nodeIndexingQueueSequenceDao = nodeIndexingQueueSequenceDao;
    this.nodeIndexingQueueDao = nodeIndexingQueueDao;
    this.nodeIndexingQueueItemDao = nodeIndexingQueueItemDao;
    this.gson = gson;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.isEmpty()) {
      asyncReindexAll();
    } else {
      // if app was shut down mid indexing, resume by indexing all queues
      indexAllQueues();
    }
  }

  private void asyncReindexAll() {
    index.index(
        () -> super.keys(Queries.matchAll(), indexer),
        key -> super.get(key, indexer));
  }

  private void indexAllQueues() {
    try (Stream<Long> queues = nodeIndexingQueueDao.keys(Specifications.matchAll())) {
      queues.forEach(this::index);
    }
  }

  @Subscribe
  public void closeIndexOn(ApplicationShutdownEvent e) {
    index.close();
  }

  @Subscribe
  public void reindexOn(ReindexEvent<NodeId> e) {
    log.info("Indexing");
    index(e.getKeyStreamSupplier().get());
    log.info("Done");
  }

  @Override
  public void save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    Long queueId = initQueue();

    try {
      super.save(nodes.peek(node -> enqueue(queueId, node.identifier())), mode, opts, user);
    } finally {
      index(queueId);
    }
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    Long queueId = initQueue();
    enqueue(queueId, node.identifier());

    try {
      return super.save(node, mode, opts, user);
    } finally {
      index(queueId);
    }
  }

  @Override
  public void delete(Stream<NodeId> idStream, WriteOptions opts, User user) {
    Long queueId = initQueue();

    try {
      super.delete(idStream.peek(id -> enqueue(queueId, id)), opts, user);
    } finally {
      index(queueId);
    }
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    Long queueId = initQueue();
    enqueue(queueId, id);

    try {
      super.delete(id, opts, user);
    } finally {
      index(queueId);
    }
  }

  private Long initQueue() {
    Long queueId = nodeIndexingQueueSequenceDao.getAndAdvance();
    nodeIndexingQueueDao.insert(queueId, Empty.INSTANCE);
    return queueId;
  }

  private void enqueue(Long queueId, NodeId nodeId) {
    nodeIndexingQueueItemDao.insert(IndexingQueueItemId.of(nodeId, queueId), Empty.INSTANCE);
  }

  private void index(Long queueId) {
    log.trace("Indexing queue {}", queueId);

    index(() -> nodeIndexingQueueItemDao
        .keys(NodeIndexingQueueItemsByQueueId.of(queueId))
        .map(IndexingQueueItemId::getId));

    log.trace("Deleting queue {}", queueId);
    nodeIndexingQueueDao.delete(queueId);
  }

  // index nodes and its references and referrers
  private void index(Supplier<Stream<NodeId>> idsSupplier) {
    long nodeCount = StreamUtils.countAndClose(idsSupplier.get());

    if (nodeCount > 1) {
      log.info("Indexing {} nodes", nodeCount);
    }

    Cache<NodeId, Boolean> indexed = CacheBuilder.newBuilder().softValues().build();

    AtomicInteger checkCounter = new AtomicInteger();
    AtomicInteger indexCounter = new AtomicInteger();

    // first pass: index nodes
    index(idsSupplier.get()
        .peek(id -> checkCounter.incrementAndGet())
        .filter(refId -> indexed.getIfPresent(refId) == null)
        .peek(id -> indexCounter.incrementAndGet())
        .peek(id -> indexed.put(id, true)));

    log.trace("Checked {} values", checkCounter.get());
    log.trace("Indexed {} values", indexCounter.get());

    indexCounter.set(0);
    checkCounter.set(0);

    // second pass: index each reference and referrer of a db node
    index(idsSupplier.get()
        .flatMap(id -> super.get(id, indexer)
            .map(node -> Stream.concat(
                node.getReferences().values().stream(),
                node.getReferrers().values().stream())
                .distinct())
            .orElseGet(Stream::of))
        .peek(id -> checkCounter.incrementAndGet())
        .filter(refId -> indexed.getIfPresent(refId) == null)
        .peek(id -> indexCounter.incrementAndGet())
        .peek(id -> indexed.put(id, true)));

    log.trace("Checked {} db refs", checkCounter.get());
    log.trace("Indexed {} db refs", indexCounter.get());

    indexCounter.set(0);
    checkCounter.set(0);

    // final pass: index each reference and referrer of an index node
    index(idsSupplier.get()
        .flatMap(id ->
            keys(Queries.query(OrSpecification.or(
                NodeAllReferences.of(id),
                NodeAllReferrers.of(id)
            )), indexer))
        .peek(id -> checkCounter.incrementAndGet())
        .filter(refId -> indexed.getIfPresent(refId) == null)
        .peek(id -> indexCounter.incrementAndGet())
        .peek(id -> indexed.put(id, true)));

    log.trace("Checked {} index refs", checkCounter.get());
    log.trace("Indexed {} index refs", indexCounter.get());

    waitLuceneIndexRefresh();

    if (nodeCount > 1) {
      log.info("Done");
    }
  }

  // index all nodes identified by given ids, closes the stream
  private void index(Stream<NodeId> ids) {
    try (Stream<NodeId> closeable = ids) {
      StreamUtils.zipIndex(closeable, Tuple::of).forEach(t -> {
        NodeId id = t._1;
        int i = t._2;

        Optional<Node> node = super.get(id, indexer);

        if (node.isPresent()) {
          index.index(id, node.get());
        } else {
          index.delete(id);
        }

        if (i % 1000 == 0) {
          log.debug("Indexed {} values", i);
        }
      });
    }
  }

  // wait for searcher to reflect updates to make sure that all updates are done and visible
  private void waitLuceneIndexRefresh() {
    if (index instanceof LuceneIndex) {
      ((LuceneIndex) index).refreshBlocking();
    }
  }

  @Override
  public Stream<Node> values(Query<NodeId, Node> query, User user) {
    if (!(query.getWhere() instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.values(query, user);
    }

    resolve(query.getWhere(), user);

    boolean loadReferrers = query.getSelect().stream()
        .anyMatch(select -> select instanceof SelectReferrer
            || select instanceof SelectAllReferrers
            || select instanceof SelectAll);

    return ((LuceneIndex<NodeId, Node>) index).get(
        query.getWhere(),
        query.getSort(),
        query.getMax(),
        loadReferrers ? of(CACHED_RESULT_FIELD, CACHED_REFERRERS_FIELD) : of(CACHED_RESULT_FIELD),
        new DocumentToNode(gson, loadReferrers));
  }

  @Override
  public Stream<NodeId> keys(Query<NodeId, Node> query, User user) {
    if (!(query.getWhere() instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.keys(query, user);
    }

    resolve(query.getWhere(), user);

    return index.getKeys(
        query.getWhere(),
        query.getSort(),
        query.getMax());
  }

  @Override
  public long count(Specification<NodeId, Node> spec, User user) {
    if (!(spec instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.count(spec, user);
    }

    resolve(spec, user);

    return index.count(spec);
  }

  private void resolve(Specification<NodeId, Node> spec, User user) {
    if (spec instanceof DependentSpecification) {
      ((DependentSpecification<NodeId, Node>) spec).resolve(s -> keys(new Query<>(s), user));
    }
    if (spec instanceof NotSpecification) {
      resolve(((NotSpecification<NodeId, Node>) spec).getSpecification(), user);
    }
    if (spec instanceof CompositeSpecification) {
      for (Specification<NodeId, Node> s : ((CompositeSpecification<NodeId, Node>) spec)
          .getSpecifications()) {
        resolve(s, user);
      }
    }
  }

}
