package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableSet.of;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_REFERRERS_FIELD;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_RESULT_FIELD;
import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.Queries.query;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.select.SelectAllReferrers;
import fi.thl.termed.service.node.select.SelectReferrer;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodeRevisionsByRevisionNumber;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.query.CompositeSpecification;
import fi.thl.termed.util.query.DependentSpecification;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.OrSpecification;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedNodeService extends ForwardingService<NodeId, Node> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Index<NodeId, Node> index;
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService;

  private User indexer = new User("indexer", "", AppRole.ADMIN);
  private Gson gson;

  public IndexedNodeService(Service<NodeId, Node> delegate, Index<NodeId, Node> index,
      Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService, Gson gson) {
    super(delegate);
    this.index = index;
    this.nodeRevisionService = nodeRevisionService;
    this.gson = gson;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.isEmpty()) {
      // async reindex all
      index.index(
          () -> super.keys(new Query<>(new MatchAll<>()), indexer),
          key -> super.get(key, indexer));
    }
  }

  @Subscribe
  public void closeIndexOn(ApplicationShutdownEvent e) {
    index.close();
  }

  @Subscribe
  public void reindexOn(ReindexEvent<NodeId> e) {
    log.info("Indexing");
    reindex(e.getKeyStreamSupplier().get());
    log.info("Done");
  }

  private Optional<Node> getFromIndex(NodeId nodeId, User user) {
    try (Stream<Node> indexedNode = values(new Query<>(and(
        new NodesByGraphId(nodeId.getTypeGraphId()),
        new NodesByTypeId(nodeId.getTypeId()),
        new NodeById(nodeId.getId()))), user)) {
      return indexedNode.findAny();
    }
  }

  @Override
  public void save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    super.save(nodes, mode, opts, user);

    Long revisionNumber = opts.getRevision()
        .orElseThrow(() -> new IllegalStateException("Revision not initialized"));

    long nodeCount = nodeRevisionService
        .count(NodeRevisionsByRevisionNumber.of(revisionNumber), user);

    log.info("Indexing {} nodes", nodeCount);

    Cache<NodeId, Boolean> indexed = CacheBuilder.newBuilder().maximumSize(100_000).build();

    AtomicInteger check = new AtomicInteger();
    AtomicInteger index = new AtomicInteger();

    // first pass: index saved nodes
    try (Stream<NodeId> idsInRevision = nodeRevisionService
        .keys(query(NodeRevisionsByRevisionNumber.of(revisionNumber)), user)
        .map(RevisionId::getId)) {
      reindex(idsInRevision
          .peek(id -> check.incrementAndGet())
          .filter(refId -> indexed.getIfPresent(refId) == null)
          .peek(id -> index.incrementAndGet())
          .peek(id -> indexed.put(id, true)));
    }

    log.debug("Checked {} values", check.get());
    log.debug("Indexed {} values", index.get());

    index.set(0);
    check.set(0);

    // second pass: index each saved node refs
    try (Stream<NodeId> idsInRevision = nodeRevisionService
        .keys(query(NodeRevisionsByRevisionNumber.of(revisionNumber)), user)
        .map(RevisionId::getId)) {
      reindex(idsInRevision
          .flatMap(id -> super.get(id, indexer)
              .map(node -> Stream.concat(
                  node.getReferences().values().stream(),
                  node.getReferrers().values().stream())
                  .distinct())
              .orElseGet(Stream::of))
          .peek(id -> check.incrementAndGet())
          .filter(refId -> indexed.getIfPresent(refId) == null)
          .peek(id -> index.incrementAndGet())
          .peek(id -> indexed.put(id, true)));
    }

    log.debug("Checked {} db refs", check.get());
    log.debug("Indexed {} db refs", index.get());

    index.set(0);
    check.set(0);

    // final pass: index each saved node refs in index
    try (Stream<NodeId> idsInRevision = nodeRevisionService
        .keys(query(NodeRevisionsByRevisionNumber.of(revisionNumber)), user)
        .map(RevisionId::getId)) {
      reindex(idsInRevision
          .flatMap(id ->
              keys(Queries.query(OrSpecification.or(
                  NodeAllReferences.of(id),
                  NodeAllReferrers.of(id)
              )), user))
          .peek(id -> check.incrementAndGet())
          .filter(refId -> indexed.getIfPresent(refId) == null)
          .peek(id -> index.incrementAndGet())
          .peek(id -> indexed.put(id, true)));
    }

    log.debug("Checked {} index refs", check.get());
    log.debug("Indexed {} index refs", index.get());

    waitLuceneIndexRefresh();

    log.info("Done");
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    NodeId nodeId = super.save(node, mode, opts, user);

    Set<NodeId> waiting = new HashSet<>();

    waiting.add(nodeId);

    getFromIndex(nodeId, user).ifPresent(indexedNode -> {
      waiting.addAll(indexedNode.getReferences().values());
      waiting.addAll(indexedNode.getReferrers().values());
    });

    super.get(nodeId, indexer).ifPresent(n -> {
      waiting.addAll(n.getReferences().values());
      waiting.addAll(n.getReferrers().values());
    });

    waiting.forEach(id ->
        super.get(id, indexer).ifPresent(n -> index.index(id, n)));

    waitLuceneIndexRefresh();

    return nodeId;
  }

  @Override
  public void delete(Stream<NodeId> idStream, WriteOptions opts, User user) {
    List<NodeId> deleted = new ArrayList<>();

    super.delete(idStream.peek(deleted::add), opts, user);

    Set<NodeId> reindexingRequired = new HashSet<>(deleted);

    deleted.forEach(nodeId -> getFromIndex(nodeId, user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    }));

    reindex(reindexingRequired.stream());
  }

  @Override
  public void delete(NodeId nodeId, WriteOptions opts, User user) {
    super.delete(nodeId, opts, user);

    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.add(nodeId);

    getFromIndex(nodeId, user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    });

    reindex(reindexingRequired.stream());
  }

  private void reindex(Stream<NodeId> ids) {
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

      waitLuceneIndexRefresh();
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
