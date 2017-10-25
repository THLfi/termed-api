package fi.thl.termed.service.node.internal;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Coordinates read operations on node revisions to simpler DAOs. Write operations are not
 * supported.
 */
public class NodeRevisionReadRepository implements
    Service<RevisionId<NodeId>, Revision<NodeId, Node>> {

  private Dao<RevisionId<NodeId>, Revision<NodeId, Node>> nodeRevisionDao;
  private Dao<RevisionId<NodeAttributeValueId>, Revision<NodeAttributeValueId, StrictLangValue>> textAttributeValueRevDao;
  private Dao<RevisionId<NodeAttributeValueId>, Revision<NodeAttributeValueId, NodeId>> referenceAttributeValueRevDao;

  public NodeRevisionReadRepository(
      Dao<RevisionId<NodeId>, Revision<NodeId, Node>> nodeRevisionDao,
      Dao<RevisionId<NodeAttributeValueId>, Revision<NodeAttributeValueId, StrictLangValue>> textAttributeValueRevDao,
      Dao<RevisionId<NodeAttributeValueId>, Revision<NodeAttributeValueId, NodeId>> referenceAttributeValueRevDao) {
    this.nodeRevisionDao = nodeRevisionDao;
    this.textAttributeValueRevDao = textAttributeValueRevDao;
    this.referenceAttributeValueRevDao = referenceAttributeValueRevDao;
  }

  @Override
  public RevisionId<NodeId> save(Revision<NodeId, Node> rev, SaveMode mode, WriteOptions opts,
      User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(RevisionId<NodeId> id, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(RevisionId<NodeId> nodeId, User user) {
    return nodeRevisionDao.exists(nodeId, user);
  }

  @Override
  public Stream<Revision<NodeId, Node>> getValues(
      Query<RevisionId<NodeId>, Revision<NodeId, Node>> query, User user) {
    return nodeRevisionDao.getValues(query.getWhere(), user).stream().map(r -> populate(r, user));
  }

  @Override
  public Stream<RevisionId<NodeId>> getKeys(
      Query<RevisionId<NodeId>, Revision<NodeId, Node>> query, User user) {
    return nodeRevisionDao.getKeys(query.getWhere(), user).stream();
  }

  @Override
  public Optional<Revision<NodeId, Node>> get(RevisionId<NodeId> id, User user, Select... selects) {
    return nodeRevisionDao.get(id, user).map(revision -> populate(revision, user));
  }

  private Revision<NodeId, Node> populate(Revision<NodeId, Node> revision, User user) {
    if (!revision.getObject().isPresent()) {
      return revision;
    }

    Node node = new Node(revision.getObject().get());
    node.setProperties(findPropertiesFor(revision.identifier(), user));
    node.setReferences(findReferencesFor(revision.identifier(), user));
    return Revision.of(revision.getId(), revision.getRevision(), revision.getType(), node);
  }

  private Multimap<String, StrictLangValue> findPropertiesFor(RevisionId<NodeId> revId, User user) {
    Map<NodeAttributeValueId, List<Revision<NodeAttributeValueId, StrictLangValue>>> attrRevs =
        textAttributeValueRevDao
            .getMap(new NodeTextAttributeValuesLessOrEqualToNodeRevision(revId), user)
            .entrySet().stream()
            .collect(groupingBy(e -> e.getKey().getId(), mapping(Entry::getValue, toList())));

    Multimap<String, StrictLangValue> properties = LinkedHashMultimap.create();

    attrRevs.forEach((attrId, valueRevs) -> {
      Optional<StrictLangValue> lastRevValue = valueRevs.stream()
          .max(comparing(Revision::getRevision))
          .flatMap(Revision::getObject);

      lastRevValue.ifPresent(value -> properties.put(attrId.getAttributeId(), value));
    });

    return properties;
  }

  private Multimap<String, NodeId> findReferencesFor(RevisionId<NodeId> revId, User user) {
    Map<NodeAttributeValueId, List<Revision<NodeAttributeValueId, NodeId>>> attrRevs =
        referenceAttributeValueRevDao
            .getMap(new NodeReferenceAttributeValuesLessOrEqualToNodeRevision(revId), user)
            .entrySet().stream()
            .collect(groupingBy(e -> e.getKey().getId(), mapping(Entry::getValue, toList())));

    Multimap<String, NodeId> references = LinkedHashMultimap.create();

    attrRevs.forEach((attrId, valueRevs) -> {
      Optional<NodeId> lastRevValue = valueRevs.stream()
          .max(comparing(Revision::getRevision))
          .flatMap(Revision::getObject);

      lastRevValue.ifPresent(value -> references.put(attrId.getAttributeId(), value));
    });

    return references;
  }

}
