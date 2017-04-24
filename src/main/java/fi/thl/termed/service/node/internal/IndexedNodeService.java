package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.ObjectUtils.castBoolean;
import static fi.thl.termed.util.ObjectUtils.castInteger;
import static fi.thl.termed.util.ObjectUtils.castStringList;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;
import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.domain.event.ReindexEvent;
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.Specification;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedNodeService extends ForwardingService<NodeId, Node> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Index<NodeId, Node> index;
  private User indexer = new User("indexer", "", AppRole.ADMIN);

  public IndexedNodeService(Service<NodeId, Node> delegate, Index<NodeId, Node> index) {
    super(delegate);
    this.index = index;
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

  @Override
  public List<NodeId> save(List<Node> nodes, Map<String, Object> args, User user) {
    Set<NodeId> referencedIds = Sets.newHashSet();

    for (Node node : nodes) {
      Optional<Node> oldNodeFromIndex = super.get(new AndSpecification<>(
          new NodesByGraphId(node.getTypeGraphId()),
          new NodesByTypeId(node.getTypeId()),
          new NodeById(node.getId())), user).stream().findFirst();

      oldNodeFromIndex.ifPresent(n -> {
        referencedIds.addAll(n.getReferences().values());
        referencedIds.addAll(n.getReferrers().values());
      });
    }

    List<NodeId> ids = super.save(nodes, args, user);

    log.info("Indexing {} nodes", ids.size());
    ProgressReporter reporter = new ProgressReporter(log, "Index", 1000, ids.size());

    Set<NodeId> indexed = new HashSet<>();

    ids.forEach(id -> {
      Node node = super.get(id, indexer).orElseThrow(IllegalStateException::new);
      index.index(id, node);
      indexed.add(id);
      referencedIds.addAll(node.getReferences().values());
      referencedIds.addAll(node.getReferrers().values());
      reporter.tick();
    });

    referencedIds.removeAll(indexed);

    reporter.report();
    log.info("Indexing {} referenced nodes", referencedIds.size());
    ProgressReporter refReporter = new ProgressReporter(log, "Index", 1000, referencedIds.size());

    referencedIds.forEach(id -> {
      Node node = super.get(id, indexer).orElseThrow(IllegalStateException::new);
      index.index(node.identifier(), node);
      refReporter.tick();
    });

    refReporter.report();
    
    waitLuceneIndexRefresh();
    log.info("Done");

    return ids;
  }

  @Override
  public NodeId save(Node node, Map<String, Object> args, User user) {
    Set<NodeId> referencedIds = Sets.newHashSet();

    Optional<Node> oldNodeFromIndex = super.get(new AndSpecification<>(
        new NodesByGraphId(node.getTypeGraphId()),
        new NodesByTypeId(node.getTypeId()),
        new NodeById(node.getId())), user).stream().findFirst();

    oldNodeFromIndex.ifPresent(n -> {
      referencedIds.addAll(n.getReferences().values());
      referencedIds.addAll(n.getReferrers().values());
    });

    NodeId id = super.save(node, args, user);

    List<Node> nodesForIndexing = new ArrayList<>();

    Node newNodeForIndexing = super.get(id, indexer).orElseThrow(IllegalStateException::new);
    referencedIds.addAll(newNodeForIndexing.getReferences().values());
    referencedIds.addAll(newNodeForIndexing.getReferrers().values());

    nodesForIndexing.add(newNodeForIndexing);

    for (NodeId refId : referencedIds) {
      nodesForIndexing.add(super.get(refId, indexer).orElseThrow(IllegalStateException::new));
    }

    reindex(nodesForIndexing);

    return id;
  }

  private void reindex(Collection<Node> nodes) {
    ProgressReporter reporter = new ProgressReporter(log, "Index", 1000, nodes.size());
    nodes.forEach(node -> {
      index.index(node.identifier(), node);
      reporter.tick();
    });
    reporter.report();
    waitLuceneIndexRefresh();
  }

  @Override
  public void delete(List<NodeId> nodeIds, Map<String, Object> args, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    reindexSet.addAll(nodeIds);
    reindexSet.addAll(nodeIds.stream()
        .flatMap(nodeId -> nodeNeighbourIds(nodeId).stream()).collect(toList()));

    super.delete(nodeIds, args, user);

    reindex(reindexSet);
  }

  @Override
  public void delete(NodeId nodeId, Map<String, Object> args, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    reindexSet.add(nodeId);
    reindexSet.addAll(nodeNeighbourIds(nodeId));

    super.delete(nodeId, args, user);

    reindex(reindexSet);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Node> get(Specification<NodeId, Node> spec, Map<String, Object> args, User user) {
    boolean bypassIndex = castBoolean(args.get("bypassIndex"), false);

    return !bypassIndex && spec instanceof LuceneSpecification ?
        index.get(spec, castStringList(args.get("sort")), castInteger(args.get("max"), -1)) :
        super.get(spec, args, user);
  }

  @Override
  public List<NodeId> getKeys(Specification<NodeId, Node> spec, Map<String, Object> args,
      User user) {
    boolean bypassIndex = castBoolean(args.get("bypassIndex"), false);

    return !bypassIndex && spec instanceof LuceneSpecification ?
        index.getKeys(spec, castStringList(args.get("sort")), castInteger(args.get("max"), -1)) :
        super.getKeys(spec, args, user);
  }

  private Set<NodeId> nodeNeighbourIds(NodeId nodeId) {
    ImmutableSet.Builder<NodeId> ids = new Builder<>();
    Optional<Node> nodeOptional = super.get(nodeId, indexer);

    if (nodeOptional.isPresent()) {
      ids.addAll(nodeOptional.get().getReferences().values());
      ids.addAll(nodeOptional.get().getReferrers().values());
    }

    return ids.build();
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

}
