package fi.thl.termed.service.node.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.eventbus.Subscribe;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.event.ApplicationReadyEvent;
import fi.thl.termed.domain.event.ApplicationShutdownEvent;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Query.Engine;
import fi.thl.termed.util.specification.Results;

public class IndexedNodeService extends ForwardingService<NodeId, Node> {

  private Index<NodeId, Node> index;
  private User indexer = new User("indexer", "", AppRole.ADMIN);

  public IndexedNodeService(Service<NodeId, Node> delegate, Index<NodeId, Node> index) {
    super(delegate);
    this.index = index;
  }

  @Subscribe
  public void initIndexOn(ApplicationReadyEvent e) {
    if (index.isEmpty()) {
      // reindex all
      index.index(super.getKeys(new Query<>(new MatchAll<>()), indexer).getValues(),
                  key -> super.get(key, indexer));
    }
  }

  @Subscribe
  public void closeIndexOn(ApplicationShutdownEvent e) {
    index.close();
  }

  @Override
  public List<NodeId> save(List<Node> nodes, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    for (Node node : nodes) {
      reindexSet.add(new NodeId(node));
      reindexSet.addAll(nodeRelatedIds(new NodeId(node)));
    }

    List<NodeId> ids = super.save(nodes, user);

    for (Node node : nodes) {
      reindexSet.addAll(nodeRelatedIds(new NodeId(node)));
    }
    asyncReindex(reindexSet);
    return ids;
  }

  @Override
  public NodeId save(Node node, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    reindexSet.add(new NodeId(node));
    reindexSet.addAll(nodeRelatedIds(new NodeId(node)));

    NodeId id = super.save(node, user);

    reindexSet.addAll(nodeRelatedIds(new NodeId(node)));
    reindex(reindexSet);
    return id;
  }

  @Override
  public void delete(List<NodeId> nodeIds, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    for (NodeId nodeId : nodeIds) {
      reindexSet.add(nodeId);
      reindexSet.addAll(nodeRelatedIds(nodeId));
    }

    super.delete(nodeIds, user);

    asyncReindex(reindexSet);
  }

  @Override
  public void delete(NodeId nodeId, User user) {
    Set<NodeId> reindexSet = Sets.newHashSet();

    reindexSet.add(nodeId);
    reindexSet.addAll(nodeRelatedIds(nodeId));

    super.delete(nodeId, user);

    asyncReindex(reindexSet);
  }

  @Override
  public Results<Node> get(Query<NodeId, Node> query, User user) {
    return query.getEngine() == Engine.LUCENE ? index.get(query)
                                              : super.get(query, user);
  }

  @Override
  public Results<NodeId> getKeys(Query<NodeId, Node> query, User user) {
    return query.getEngine() == Engine.LUCENE ? index.getKeys(query)
                                              : super.getKeys(query, user);
  }

  private Set<NodeId> nodeRelatedIds(NodeId nodeId) {
    Set<NodeId> refValues = new HashSet<>();
    Optional<Node> nodeOptional = super.get(nodeId, indexer);

    if (nodeOptional.isPresent()) {
      refValues.addAll(nodeOptional.get().getReferences().values());
      refValues.addAll(nodeOptional.get().getReferrers().values());
    }

    return refValues;
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
  }

  private void asyncReindex(Set<NodeId> ids) {
    if (!ids.isEmpty()) {
      index.index(ImmutableList.copyOf(ids), id -> {
        return IndexedNodeService.super.get(id, indexer);
      });
    }
  }

}
