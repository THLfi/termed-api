package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.ObjectUtils.castBoolean;
import static fi.thl.termed.util.ObjectUtils.castInteger;
import static fi.thl.termed.util.ObjectUtils.castStringList;
import static fi.thl.termed.util.collect.ArgUtils.findBoolean;
import static java.util.stream.Collectors.toList;

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
import fi.thl.termed.service.node.specification.NodeById;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.collect.ArgUtils;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.AndSpecification;
import fi.thl.termed.util.specification.CompositeSpecification;
import fi.thl.termed.util.specification.DependentSpecification;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.NotSpecification;
import fi.thl.termed.util.specification.Specification;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
      index.index(super.getKeys(indexer).collect(toList()), key -> super.get(key, indexer));
    }
  }

  @Subscribe
  public void closeIndexOn(ApplicationShutdownEvent e) {
    index.close();
  }

  @Subscribe
  public void reindexOn(ReindexEvent e) {
    index.index(super.getKeys(indexer).collect(toList()), key -> super.get(key, indexer));
  }

  @Override
  public List<NodeId> save(List<Node> nodes, User user, Arg... args) {
    Set<NodeId> reindexingRequired = new HashSet<>();

    nodes.forEach(node -> super.get(new AndSpecification<>(
        new NodesByGraphId(node.getTypeGraphId()),
        new NodesByTypeId(node.getTypeId()),
        new NodeById(node.getId())), indexer)
        .findFirst()
        .ifPresent(n -> {
          reindexingRequired.addAll(n.getReferences().values());
          reindexingRequired.addAll(n.getReferrers().values());
        }));

    List<NodeId> ids = super.save(nodes, user, args);

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
  public NodeId save(Node node, User user, Arg... args) {
    Set<NodeId> reindexingRequired = Sets.newHashSet();

    super.get(new AndSpecification<>(
        new NodesByGraphId(node.getTypeGraphId()),
        new NodesByTypeId(node.getTypeId()),
        new NodeById(node.getId())), user)
        .findFirst()
        .ifPresent(n -> {
          reindexingRequired.addAll(n.getReferences().values());
          reindexingRequired.addAll(n.getReferrers().values());
        });

    NodeId id = super.save(node, user, args);
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
  public void delete(List<NodeId> nodeIds, User user, Arg... args) {
    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.addAll(nodeIds);

    nodeIds.forEach(nodeId -> super.get(new AndSpecification<>(
        new NodesByGraphId(nodeId.getTypeGraphId()),
        new NodesByTypeId(nodeId.getTypeId()),
        new NodeById(nodeId.getId())), indexer)
        .findFirst()
        .ifPresent(n -> {
          reindexingRequired.addAll(n.getReferences().values());
          reindexingRequired.addAll(n.getReferrers().values());
        }));

    super.delete(nodeIds, user, args);

    reindex(reindexingRequired);
  }

  @Override
  public void delete(NodeId nodeId, User user, Arg... args) {
    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.add(nodeId);

    super.get(new AndSpecification<>(
        new NodesByGraphId(nodeId.getTypeGraphId()),
        new NodesByTypeId(nodeId.getTypeId()),
        new NodeById(nodeId.getId())), indexer)
        .findFirst()
        .ifPresent(n -> {
          reindexingRequired.addAll(n.getReferences().values());
          reindexingRequired.addAll(n.getReferrers().values());
        });

    super.delete(nodeId, user, args);

    reindex(reindexingRequired);
  }

  @Override
  public List<NodeId> deleteAndSave(List<NodeId> deletes, List<Node> saves, User user,
      Arg... args) {

    Set<NodeId> reindexingRequired = new HashSet<>();

    reindexingRequired.addAll(deletes);

    deletes.forEach(delete -> super.get(new AndSpecification<>(
        new NodesByGraphId(delete.getTypeGraphId()),
        new NodesByTypeId(delete.getTypeId()),
        new NodeById(delete.getId())), indexer)
        .findFirst()
        .ifPresent(n -> {
          reindexingRequired.addAll(n.getReferences().values());
          reindexingRequired.addAll(n.getReferrers().values());
        }));

    saves.forEach(save -> super.get(new AndSpecification<>(
        new NodesByGraphId(save.getTypeGraphId()),
        new NodesByTypeId(save.getTypeId()),
        new NodeById(save.getId())), indexer)
        .findFirst()
        .ifPresent(n -> {
          reindexingRequired.addAll(n.getReferences().values());
          reindexingRequired.addAll(n.getReferrers().values());
        }));

    List<NodeId> ids = super.deleteAndSave(deletes, saves, user, args);

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
  public Stream<Node> get(Specification<NodeId, Node> spec, User user, Arg... args) {
    Map<String, Object> argMap = ArgUtils.map(args);

    boolean bypassIndex = castBoolean(argMap.get("bypassIndex"), false);

    if (bypassIndex || !(spec instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.get(spec, user, args);
    }

    resolve(spec, user);

    return ((LuceneIndex<NodeId, Node>) index).get(spec,
        castStringList(argMap.get("sort")),
        castInteger(argMap.get("max"), -1),
        new DocumentToNode(gson, castBoolean(argMap.get("loadReferrers"), true)));
  }

  @Override
  public Stream<NodeId> getKeys(Specification<NodeId, Node> spec, User user, Arg... args) {
    Map<String, Object> argMap = ArgUtils.map(args);

    boolean bypassIndex = castBoolean(argMap.get("bypassIndex"), false);

    if (bypassIndex || !(spec instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.getKeys(spec, user, args);
    }

    resolve(spec, user);

    return index.getKeys(spec,
        castStringList(argMap.get("sort")),
        castInteger(argMap.get("max"), -1));
  }

  @Override
  public long count(Specification<NodeId, Node> spec, User user, Arg... args) {
    boolean bypassIndex = findBoolean(args, "bypassIndex", false);

    if (bypassIndex || !(spec instanceof LuceneSpecification) || !(index instanceof LuceneIndex)) {
      return super.count(spec, user, args);
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
