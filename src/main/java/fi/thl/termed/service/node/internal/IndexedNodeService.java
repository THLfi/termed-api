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
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.Specification;
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

  @Override
  public List<NodeId> save(List<Node> nodes, Map<String, Object> args, User user) {
    List<NodeId> nodeIds = nodes.stream().map(Node::identifier).collect(toList());

    Set<NodeId> reindexSet = Sets.newHashSet();

    reindexSet.addAll(nodeIds);
    reindexSet.addAll(nodeIds.stream()
        .flatMap(nodeId -> nodeNeighbourIds(nodeId).stream()).collect(toList()));

    List<NodeId> ids = super.save(nodes, args, user);

    reindexSet.addAll(ids);
    reindexSet.addAll(ids.stream()
        .flatMap(nodeId -> nodeNeighbourIds(nodeId).stream()).collect(toList()));

    log.info("Indexing {} nodes", reindexSet.size());

    reindex(reindexSet);

    log.info("Done");

    return ids;
  }

  @Override
  public NodeId save(Node node, Map<String, Object> args, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    reindexSet.add(node.identifier());
    reindexSet.addAll(nodeNeighbourIds(node.identifier()));

    NodeId id = super.save(node, args, user);

    reindexSet.add(id);
    reindexSet.addAll(nodeNeighbourIds(id));

    reindex(reindexSet);

    return id;
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
    List<String> sort = castStringList(args.get("sort"));
    int max = castInteger(args.get("max"), -1);

    return !bypassIndex && spec instanceof LuceneSpecification ?
        index.get(spec, sort, max) : super.get(spec, args, user);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<NodeId> getKeys(Specification<NodeId, Node> spec, Map<String, Object> args,
      User user) {
    boolean bypassIndex = castBoolean(args.get("bypassIndex"), false);
    List<String> sort = castStringList(args.get("sort"));
    int max = castInteger(args.get("max"), -1);

    return !bypassIndex && spec instanceof LuceneSpecification ?
        index.getKeys(spec, sort, max) : super.getKeys(spec, args, user);
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
    for (NodeId id : ids) {
      Optional<Node> node = super.get(id, indexer);
      if (node.isPresent()) {
        index.index(id, node.get());
      } else {
        index.delete(id);
      }
    }

    waitLuceneIndexRefresh();
  }

  // wait for searcher to reflect updates to make sure that all updates are done and visible
  private void waitLuceneIndexRefresh() {
    if (index instanceof LuceneIndex) {
      ((LuceneIndex) index).refreshBlocking();
    }
  }

}
