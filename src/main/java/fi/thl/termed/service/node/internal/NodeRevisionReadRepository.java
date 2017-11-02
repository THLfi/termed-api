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
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.collect.Tuple3;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Coordinates read operations on node revisions to simpler DAOs. Write operations are not
 * supported.
 */
public class NodeRevisionReadRepository implements
    Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private Dao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionDao;
  private Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttributeValueRevDao;
  private Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> referenceAttributeValueRevDao;

  public NodeRevisionReadRepository(
      Dao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionDao,
      Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttributeValueRevDao,
      Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> referenceAttributeValueRevDao) {
    this.nodeRevisionDao = nodeRevisionDao;
    this.textAttributeValueRevDao = textAttributeValueRevDao;
    this.referenceAttributeValueRevDao = referenceAttributeValueRevDao;
  }

  @Override
  public RevisionId<NodeId> save(Tuple2<RevisionType, Node> rev, SaveMode mode, WriteOptions opts,
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
  public Stream<Tuple2<RevisionType, Node>> getValueStream(
      Query<RevisionId<NodeId>, Tuple2<RevisionType, Node>> query, User user) {
    return nodeRevisionDao.getMap(query.getWhere(), user).entrySet().stream()
        .map(e -> populate(e.getKey(), e.getValue(), user));
  }

  @Override
  public Stream<RevisionId<NodeId>> getKeyStream(
      Query<RevisionId<NodeId>, Tuple2<RevisionType, Node>> query, User user) {
    return nodeRevisionDao.getKeys(query.getWhere(), user).stream();
  }

  @Override
  public Optional<Tuple2<RevisionType, Node>> get(RevisionId<NodeId> id, User user,
      Select... selects) {
    return nodeRevisionDao.get(id, user).map(revision -> populate(id, revision, user));
  }

  private Tuple2<RevisionType, Node> populate(RevisionId<NodeId> id, Tuple2<RevisionType, Node> rev,
      User user) {
    if (rev._2 == null) {
      return rev;
    }

    Node node = new Node(rev._2);
    node.setProperties(findPropertiesFor(id, user));
    node.setReferences(findReferencesFor(id, user));
    return Tuple.of(rev._1, node);
  }

  private Multimap<String, StrictLangValue> findPropertiesFor(RevisionId<NodeId> revId, User user) {
    Map<NodeAttributeValueId, List<Tuple3<Long, RevisionType, StrictLangValue>>> attrRevs =
        textAttributeValueRevDao
            .getMap(new NodeTextAttributeValuesLessOrEqualToNodeRevision(revId), user)
            .entrySet().stream()
            .collect(groupingBy(e -> e.getKey().getId(), mapping(
                e -> Tuple.of(e.getKey().getRevision(), e.getValue()._1, e.getValue()._2),
                toList())));

    Multimap<String, StrictLangValue> properties = LinkedHashMultimap.create();

    attrRevs.forEach((attrId, valueRevs) -> {
      Optional<StrictLangValue> lastRevValue = valueRevs.stream()
          .max(comparing(t -> t._1))
          .map(t -> t._3);

      lastRevValue.ifPresent(value -> properties.put(attrId.getAttributeId(), value));
    });

    return properties;
  }

  private Multimap<String, NodeId> findReferencesFor(RevisionId<NodeId> revId, User user) {
    Map<NodeAttributeValueId, List<Tuple3<Long, RevisionType, NodeId>>> attrRevs =
        referenceAttributeValueRevDao
            .getMap(new NodeReferenceAttributeValuesLessOrEqualToNodeRevision(revId), user)
            .entrySet().stream()
            .collect(groupingBy(e -> e.getKey().getId(), mapping(
                e -> Tuple.of(e.getKey().getRevision(), e.getValue()._1, e.getValue()._2),
                toList())));

    Multimap<String, NodeId> references = LinkedHashMultimap.create();

    attrRevs.forEach((attrId, valueRevs) -> {
      Optional<NodeId> lastRevValue = valueRevs.stream()
          .max(comparing(t -> t._1))
          .map(t -> t._3);

      lastRevValue.ifPresent(value -> references.put(attrId.getAttributeId(), value));
    });

    return references;
  }

}
