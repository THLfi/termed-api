package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.of;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_REFERRERS_FIELD;
import static fi.thl.termed.util.index.lucene.LuceneConstants.CACHED_RESULT_FIELD;
import static fi.thl.termed.util.query.AndSpecification.and;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.select.SelectAllReferrers;
import fi.thl.termed.service.node.select.SelectReferrer;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.query.CompositeSpecification;
import fi.thl.termed.util.query.DependentSpecification;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.MatchNone;
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.ForwardingService2;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.WriteOptions;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedNodeService extends ForwardingService2<NodeId, Node> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Index<NodeId, Node> index;
  private User indexer = new User("indexer", "", AppRole.ADMIN);
  private Gson gson;

  public IndexedNodeService(Service2<NodeId, Node> delegate, Index<NodeId, Node> index, Gson gson) {
    super(delegate);
    this.index = index;
    this.gson = gson;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.isEmpty()) {
      // async reindex all
      index.index(
          () -> super.keys(new Query<>(new MatchNone<>()), indexer),
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
    reindex(e.getKeys());
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
  public Stream<NodeId> save(Stream<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    ImmutableList<NodeId> idList;

    try (Stream<NodeId> idStream = super.save(nodes, mode, opts, user)) {
      idList = idStream.collect(toImmutableList());
    }

    log.info("Indexing {} nodes", idList.size());
    ProgressReporter reporter = new ProgressReporter(log, "Index", 1000, idList.size());

    Set<NodeId> indexed = new HashSet<>();
    Set<NodeId> reindexingRequired = new HashSet<>();

    idList.forEach(id -> {
      getFromIndex(id, user).ifPresent(n -> {
        reindexingRequired.addAll(n.getReferences().values());
        reindexingRequired.addAll(n.getReferrers().values());
      });

      Node node = super.get(id, indexer).orElseThrow(IllegalStateException::new);

      index.index(id, node);
      indexed.add(id);

      reindexingRequired.addAll(node.getReferences().values());
      reindexingRequired.addAll(node.getReferrers().values());

      reporter.tick();
    });

    reindexingRequired.removeAll(indexed);
    reporter.report();

    // reindex remaining and wait for index refresh
    reindex(reindexingRequired.stream());

    log.info("Done");
    return idList.stream();
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    NodeId id = super.save(node, mode, opts, user);

    Set<NodeId> reindexingRequired = Sets.newHashSet();

    reindexingRequired.add(id);

    getFromIndex(id, user).ifPresent(indexedNode -> {
      reindexingRequired.addAll(indexedNode.getReferences().values());
      reindexingRequired.addAll(indexedNode.getReferrers().values());
    });

    super.get(id, indexer).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    });

    reindexingRequired.forEach(reindexId ->
        index.index(reindexId, super.get(reindexId, indexer)
            .orElseThrow(IllegalStateException::new)));

    waitLuceneIndexRefresh();

    return id;
  }

  @Override
  public void delete(Stream<NodeId> idStream, WriteOptions opts, User user) {
    ImmutableList<NodeId> idList;

    try (Stream<NodeId> closeable = idStream) {
      idList = closeable.collect(toImmutableList());
    }

    super.delete(idList.stream(), opts, user);

    Set<NodeId> reindexingRequired = new HashSet<>(idList);

    idList.forEach(nodeId -> getFromIndex(nodeId, user).ifPresent(n -> {
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
    ids.forEach(id -> {
      Optional<Node> node = super.get(id, indexer);

      if (node.isPresent()) {
        index.index(id, node.get());
      } else {
        index.delete(id);
      }
    });

    waitLuceneIndexRefresh();
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
