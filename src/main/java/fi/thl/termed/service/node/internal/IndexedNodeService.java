package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.query.AndSpecification.and;

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
import fi.thl.termed.util.query.NotSpecification;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedNodeService extends ForwardingService<NodeId, Node> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Index<NodeId, Node> index;
  private User indexer = new User("indexer", "", AppRole.ADMIN);
  private Gson gson;

  public IndexedNodeService(Service<NodeId, Node> delegate, Index<NodeId, Node> index, Gson gson) {
    super(delegate);
    this.index = index;
    this.gson = gson;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.isEmpty()) {
      // async reindex all
      index.index(super.getKeys(indexer), key -> super.get(key, indexer));
    }
  }

  @Subscribe
  public void closeIndexOn(ApplicationShutdownEvent e) {
    index.close();
  }

  @Subscribe
  public void reindexOn(ReindexEvent e) {
    index.index(super.getKeys(indexer), key -> super.get(key, indexer));
  }

  private Optional<Node> getFromIndex(NodeId nodeId, User user) {
    try (Stream<Node> indexedNode = getValueStream(and(
        new NodesByGraphId(nodeId.getTypeGraphId()),
        new NodesByTypeId(nodeId.getTypeId()),
        new NodeById(nodeId.getId())), user)) {
      return indexedNode.findAny();
    }
  }

  @Override
  public List<NodeId> save(List<Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    Set<NodeId> reindexingRequired = new HashSet<>();

    // collect existing node reference and referrer ids from index
    nodes.forEach(node -> getFromIndex(node.identifier(), user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    }));

    List<NodeId> ids = super.save(nodes, mode, opts, user);

    log.info("Indexing {} nodes", ids.size());
    ProgressReporter reporter = new ProgressReporter(log, "Index", 1000, ids.size());
    Set<NodeId> indexed = new HashSet<>();

    ids.forEach(id -> {
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
    reindex(reindexingRequired);

    log.info("Done");
    return ids;
  }

  @Override
  public NodeId save(Node node, SaveMode mode, WriteOptions opts, User user) {
    Set<NodeId> reindexingRequired = Sets.newHashSet();

    getFromIndex(node.identifier(), user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    });

    NodeId id = super.save(node, mode, opts, user);
    reindexingRequired.add(id);

    super.get(id, indexer).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    });

    reindexingRequired.forEach(reindexId ->
        index.index(reindexId,
            super.get(reindexId, indexer).orElseThrow(IllegalStateException::new)));

    waitLuceneIndexRefresh();

    return id;
  }

  @Override
  public void delete(List<NodeId> nodeIds, WriteOptions opts, User user) {
    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.addAll(nodeIds);

    nodeIds.forEach(nodeId -> getFromIndex(nodeId, user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    }));

    super.delete(nodeIds, opts, user);

    reindex(reindexingRequired);
  }

  @Override
  public void delete(NodeId nodeId, WriteOptions opts, User user) {
    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.add(nodeId);

    getFromIndex(nodeId, user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    });

    super.delete(nodeId, opts, user);

    reindex(reindexingRequired);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves, SaveMode mode,
      WriteOptions opts, User user) {

    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.addAll(deletes);

    deletes.forEach(nodeId -> getFromIndex(nodeId, user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    }));

    saves.forEach(node -> getFromIndex(node.identifier(), user).ifPresent(n -> {
      reindexingRequired.addAll(n.getReferences().values());
      reindexingRequired.addAll(n.getReferrers().values());
    }));

    List<NodeId> ids = super.deleteAndSave(deletes, saves, mode, opts, user);

    log.info("Indexing {} nodes", ids.size());
    ProgressReporter reporter = new ProgressReporter(log, "Index", 1000, ids.size());
    Set<NodeId> indexed = new HashSet<>();

    ids.forEach(id -> {
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
    reindex(reindexingRequired);

    return ids;
  }

  private void reindex(Set<NodeId> ids) {
    ProgressReporter reporter = new ProgressReporter(log, "Index", 1000, ids.size());

    for (NodeId id : ids) {
      Optional<Node> node = super.get(id, indexer);
      if (node.isPresent()) {
        index.index(id, node.get());
      } else {
        index.delete(id);
      }
      reporter.tick();
    }

    reporter.report();
    waitLuceneIndexRefresh();
  }

  // wait for searcher to reflect updates to make sure that all updates are done and visible
  private void waitLuceneIndexRefresh() {
    if (index instanceof LuceneIndex) {
      ((LuceneIndex) index).refreshBlocking();
    }
  }

  @Override
  public Stream<Node> getValueStream(Query<NodeId, Node> query, User user) {
    if (!(query.getWhere() instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.getValueStream(query, user);
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
        new DocumentToNode(gson, loadReferrers));
  }

  @Override
  public Stream<NodeId> getKeyStream(Query<NodeId, Node> query, User user) {
    if (!(query.getWhere() instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.getKeyStream(query, user);
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
      ((DependentSpecification<NodeId, Node>) spec).resolve(s -> getKeys(s, user));
    }
    if (spec instanceof NotSpecification) {
      resolve(((NotSpecification<NodeId, Node>) spec).getSpecification(), user);
    }
    if (spec instanceof CompositeSpecification) {
      for (Specification<NodeId, Node> s :
          ((CompositeSpecification<NodeId, Node>) spec).getSpecifications()) {
        resolve(s, user);
      }
    }
  }

}
