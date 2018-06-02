package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.NodeTextAttributeValueDtoToModel;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdDtoToModel;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.WriteOptions;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Coordinates CRUD-operations on Nodes to simpler DAOs. Revision reads are typically done here.
 * Incremental revision updates are automatically made by NodeRepository. This repository can do
 * full revision saves which are useful in e.g. admin operations.
 */
public class NodeRevisionRepository implements
    Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private Dao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionDao;
  private Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttributeValueRevDao;
  private Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> referenceAttributeValueRevDao;

  private Service2<Long, Revision> revisionService;
  private SequenceService revisionSeqService;

  public NodeRevisionRepository(
      Dao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionDao,
      Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttributeValueRevDao,
      Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> referenceAttributeValueRevDao,
      Service2<Long, Revision> revisionService, SequenceService revisionSeqService) {
    this.nodeRevisionDao = nodeRevisionDao;
    this.textAttributeValueRevDao = textAttributeValueRevDao;
    this.referenceAttributeValueRevDao = referenceAttributeValueRevDao;
    this.revisionService = revisionService;
    this.revisionSeqService = revisionSeqService;
  }

  @Override
  public List<RevisionId<NodeId>> save(List<Tuple2<RevisionType, Node>> typeNodePairs,
      SaveMode mode, WriteOptions opts, User user) {

    Preconditions.checkArgument(mode == INSERT);

    Long revision = opts.getRevision().orElseGet(() -> newRevision(user));

    Map<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevs = new LinkedHashMap<>();
    Map<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttrRevs = new LinkedHashMap<>();
    Map<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> refAttrRevs = new LinkedHashMap<>();

    typeNodePairs.forEach(typeAndNode -> {
      RevisionType type = typeAndNode._1;

      Node node = typeAndNode._2;
      NodeId id = node.identifier();

      Map<NodeAttributeValueId, StrictLangValue> textAttrValues =
          new NodeTextAttributeValueDtoToModel(id).apply(node.getProperties());
      Map<NodeAttributeValueId, NodeId> refAttrValues =
          new ReferenceAttributeValueIdDtoToModel(id).apply(node.getReferences());

      nodeRevs.put(RevisionId.of(id, revision), Tuple.of(type, node));
      textAttrRevs.putAll(toRevs(textAttrValues, revision, type));
      refAttrRevs.putAll(toRevs(refAttrValues, revision, type));
    });

    nodeRevisionDao.insert(nodeRevs, user);
    textAttributeValueRevDao.insert(textAttrRevs, user);
    referenceAttributeValueRevDao.insert(refAttrRevs, user);

    return ImmutableList.copyOf(nodeRevs.keySet());
  }

  @Override
  public RevisionId<NodeId> save(Tuple2<RevisionType, Node> typeAndNode, SaveMode mode,
      WriteOptions opts, User user) {

    Preconditions.checkArgument(mode == INSERT);

    Long revision = opts.getRevision().orElseGet(() -> newRevision(user));
    RevisionType type = typeAndNode._1;
    Node node = typeAndNode._2;
    NodeId id = node.identifier();

    Map<NodeAttributeValueId, StrictLangValue> textAttrValues =
        new NodeTextAttributeValueDtoToModel(id).apply(node.getProperties());
    Map<NodeAttributeValueId, NodeId> refAttrValues =
        new ReferenceAttributeValueIdDtoToModel(id).apply(node.getReferences());

    nodeRevisionDao.insert(RevisionId.of(id, revision), Tuple.of(type, node), user);
    textAttributeValueRevDao.insert(toRevs(textAttrValues, revision, type), user);
    referenceAttributeValueRevDao.insert(toRevs(refAttrValues, revision, type), user);

    return RevisionId.of(id, revision);
  }

  private Long newRevision(User user) {
    return revisionService.save(
        Revision.of(revisionSeqService.getAndAdvance(user), user.getUsername(), new Date()),
        INSERT, defaultOpts(), user);
  }

  private <K extends Serializable, V> Map<RevisionId<K>, Tuple2<RevisionType, V>> toRevs(
      Map<K, V> map, Long revision, RevisionType revisionType) {
    return map.entrySet().stream().collect(toMap(
        e -> RevisionId.of(e.getKey(), revision),
        e -> Tuple.of(revisionType, e.getValue())));
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
    Map<NodeAttributeValueId, List<Tuple2<Long, StrictLangValue>>> attrValueRevs =
        textAttributeValueRevDao
            .getMap(new NodeRevisionTextAttributeValuesLessOrEqualToRevision(revId), user)
            .entrySet().stream()
            .collect(groupingBy(e -> e.getKey().getId(), LinkedHashMap::new,
                mapping(e -> Tuple.of(e.getKey().getRevision(), e.getValue()._2), toList())));

    Multimap<String, StrictLangValue> properties = LinkedHashMultimap.create();

    attrValueRevs.forEach((attrId, valueRevs) -> {
      Optional<StrictLangValue> lastRevValue = valueRevs.stream()
          .max(comparing(t -> t._1))
          .map(t -> t._2);

      lastRevValue.ifPresent(value -> properties.put(attrId.getAttributeId(), value));
    });

    return properties;
  }

  private Multimap<String, NodeId> findReferencesFor(RevisionId<NodeId> revId, User user) {
    Map<NodeAttributeValueId, List<Tuple2<Long, NodeId>>> attrValueRevs =
        referenceAttributeValueRevDao
            .getMap(new NodeRevisionReferenceAttributeValuesLessOrEqualToRevision(revId), user)
            .entrySet().stream()
            .collect(groupingBy(e -> e.getKey().getId(), LinkedHashMap::new,
                mapping(e -> Tuple.of(e.getKey().getRevision(), e.getValue()._2), toList())));

    Multimap<String, NodeId> references = LinkedHashMultimap.create();

    attrValueRevs.forEach((attrId, valueRevs) -> {
      Optional<NodeId> lastRevValue = valueRevs.stream()
          .max(comparing(t -> t._1))
          .map(t -> t._2);

      lastRevValue.ifPresent(value -> references.put(attrId.getAttributeId(), value));
    });

    return references;
  }

}
