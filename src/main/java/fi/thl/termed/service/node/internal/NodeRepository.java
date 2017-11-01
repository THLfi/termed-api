package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.difference;
import static fi.thl.termed.domain.RevisionType.DELETE;
import static fi.thl.termed.domain.RevisionType.INSERT;
import static fi.thl.termed.domain.RevisionType.UPDATE;
import static fi.thl.termed.util.collect.MapUtils.leftValues;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.MapDifference;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.NodeTextAttributeValueDtoToModel;
import fi.thl.termed.domain.transform.NodeTextAttributeValueModelToDto;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdDtoToModel;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToDto;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToReferrerDto;
import fi.thl.termed.util.collect.Pair;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.WriteOptions;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Coordinates CRUD-operations on Nodes to simpler DAOs.
 */
public class NodeRepository extends AbstractRepository<NodeId, Node> {

  private Dao<NodeId, Node> nodeDao;
  private Dao<NodeAttributeValueId, StrictLangValue> textAttrValueDao;
  private Dao<NodeAttributeValueId, NodeId> refAttrValueDao;

  private Dao<RevisionId<NodeId>, Pair<RevisionType, Node>> nodeRevDao;
  private Dao<RevisionId<NodeAttributeValueId>, Pair<RevisionType, StrictLangValue>> textAttrValueRevDao;
  private Dao<RevisionId<NodeAttributeValueId>, Pair<RevisionType, NodeId>> refAttrValueRevDao;

  public NodeRepository(Dao<NodeId, Node> nodeDao,
      Dao<NodeAttributeValueId, StrictLangValue> textAttrValueDao,
      Dao<NodeAttributeValueId, NodeId> refAttrValueDao,
      Dao<RevisionId<NodeId>, Pair<RevisionType, Node>> nodeRevDao,
      Dao<RevisionId<NodeAttributeValueId>, Pair<RevisionType, StrictLangValue>> textAttrValueRevDao,
      Dao<RevisionId<NodeAttributeValueId>, Pair<RevisionType, NodeId>> refAttrValueRevDao) {
    this.nodeDao = nodeDao;
    this.textAttrValueDao = textAttrValueDao;
    this.refAttrValueDao = refAttrValueDao;
    this.nodeRevDao = nodeRevDao;
    this.textAttrValueRevDao = textAttrValueRevDao;
    this.refAttrValueRevDao = refAttrValueRevDao;
  }

  /**
   * With bulk insert, first save all nodes, then dependant values.
   */
  @Override
  public void insert(Map<NodeId, Node> nodes, SaveMode mode, WriteOptions opts, User user) {
    Map<NodeAttributeValueId, StrictLangValue> allTextAttrValues = new LinkedHashMap<>();
    Map<NodeAttributeValueId, NodeId> allRefAttrValues = new LinkedHashMap<>();
    nodes.forEach((k, v) -> {
      allTextAttrValues.putAll(new NodeTextAttributeValueDtoToModel(k).apply(v.getProperties()));
      allRefAttrValues.putAll(new ReferenceAttributeValueIdDtoToModel(k).apply(v.getReferences()));
    });

    nodeDao.insert(nodes, user);
    textAttrValueDao.insert(allTextAttrValues, user);
    refAttrValueDao.insert(allRefAttrValues, user);

    opts.getRevision().ifPresent(r -> {
      nodeRevDao.insert(toRevs(nodes, r, INSERT), user);
      textAttrValueRevDao.insert(toRevs(allTextAttrValues, r, INSERT), user);
      refAttrValueRevDao.insert(toRevs(allRefAttrValues, r, INSERT), user);
    });
  }

  @Override
  public void insert(NodeId id, Node node, SaveMode mode, WriteOptions opts, User user) {
    Map<NodeAttributeValueId, StrictLangValue> textAttrValues =
        new NodeTextAttributeValueDtoToModel(id).apply(node.getProperties());
    Map<NodeAttributeValueId, NodeId> refAttrValues =
        new ReferenceAttributeValueIdDtoToModel(id).apply(node.getReferences());

    nodeDao.insert(id, node, user);
    textAttrValueDao.insert(textAttrValues, user);
    refAttrValueDao.insert(refAttrValues, user);

    opts.getRevision().ifPresent(r -> {
      nodeRevDao.insert(RevisionId.of(id, r), Pair.of(INSERT, node), user);
      textAttrValueRevDao.insert(toRevs(textAttrValues, r, INSERT), user);
      refAttrValueRevDao.insert(toRevs(refAttrValues, r, INSERT), user);
    });
  }

  @Override
  public void update(NodeId id, Node node, SaveMode mode, WriteOptions opts, User user) {
    MapDifference<NodeAttributeValueId, StrictLangValue> textsDiff = difference(
        new NodeTextAttributeValueDtoToModel(id).apply(node.getProperties()),
        textAttrValueDao.getMap(new NodeTextAttributeValuesByNodeId(id), user));
    MapDifference<NodeAttributeValueId, NodeId> refsDiff = difference(
        new ReferenceAttributeValueIdDtoToModel(id).apply(node.getReferences()),
        refAttrValueDao.getMap(new NodeReferenceAttributeValuesByNodeId(id), user));

    nodeDao.update(id, node, user);
    textAttrValueDao.insert(textsDiff.entriesOnlyOnLeft(), user);
    textAttrValueDao.update(leftValues(textsDiff.entriesDiffering()), user);
    textAttrValueDao.delete(copyOf(textsDiff.entriesOnlyOnRight().keySet()), user);
    refAttrValueDao.insert(refsDiff.entriesOnlyOnLeft(), user);
    refAttrValueDao.update(leftValues(refsDiff.entriesDiffering()), user);
    refAttrValueDao.delete(copyOf(refsDiff.entriesOnlyOnRight().keySet()), user);

    opts.getRevision().ifPresent(r -> {
      nodeRevDao.insert(RevisionId.of(id, r), Pair.of(UPDATE, node), user);
      textAttrValueRevDao.insert(toRevs(textsDiff.entriesOnlyOnLeft(), r, INSERT), user);
      textAttrValueRevDao.insert(toRevs(leftValues(textsDiff.entriesDiffering()), r, UPDATE), user);
      textAttrValueRevDao.insert(toRevs(textsDiff.entriesOnlyOnRight().keySet(), r, DELETE), user);
      refAttrValueRevDao.insert(toRevs(refsDiff.entriesOnlyOnLeft(), r, INSERT), user);
      refAttrValueRevDao.insert(toRevs(leftValues(refsDiff.entriesDiffering()), r, UPDATE), user);
      refAttrValueRevDao.insert(toRevs(refsDiff.entriesOnlyOnRight().keySet(), r, DELETE), user);
    });
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    List<NodeAttributeValueId> refAttrValues = refAttrValueDao.getKeys(
        new NodeReferenceAttributeValuesByNodeId(id), user);
    List<NodeAttributeValueId> backRefAttrValues = refAttrValueDao.getKeys(
        new NodeReferenceAttributeNodesByValueId(id), user);
    List<NodeAttributeValueId> textAttrValues = textAttrValueDao.getKeys(
        new NodeTextAttributeValuesByNodeId(id), user);

    refAttrValueDao.delete(refAttrValues, user);
    refAttrValueDao.delete(backRefAttrValues, user);
    textAttrValueDao.delete(textAttrValues, user);
    nodeDao.delete(id, user);

    opts.getRevision().ifPresent(r -> {
      refAttrValueRevDao.insert(toRevs(refAttrValues, r, DELETE), user);
      refAttrValueRevDao.insert(toRevs(backRefAttrValues, r, DELETE), user);
      textAttrValueRevDao.insert(toRevs(textAttrValues, r, DELETE), user);
      nodeRevDao.insert(RevisionId.of(id, r), Pair.of(DELETE, null), user);
    });
  }

  private <K extends Serializable, V> Map<RevisionId<K>, Pair<RevisionType, V>> toRevs(
      Map<K, V> map, Long revision, RevisionType revisionType) {
    return map.entrySet().stream().collect(toMap(
        e -> RevisionId.of(e.getKey(), revision),
        e -> Pair.of(revisionType, e.getValue())));
  }

  private <K extends Serializable, V> Map<RevisionId<K>, Pair<RevisionType, V>> toRevs(
      Collection<K> keys, Long revision, RevisionType revisionType) {
    return keys.stream().collect(toMap(
        key -> RevisionId.of(key, revision),
        key -> Pair.of(revisionType, null)));
  }

  @Override
  public boolean exists(NodeId nodeId, User user) {
    return nodeDao.exists(nodeId, user);
  }

  @Override
  public Stream<Node> getValueStream(Query<NodeId, Node> query, User user) {
    return nodeDao.getValues(query.getWhere(), user).stream()
        .map(node -> populateValue(node, user));
  }

  @Override
  public Stream<NodeId> getKeyStream(Query<NodeId, Node> query, User user) {
    return nodeDao.getKeys(query.getWhere(), user).stream();
  }

  @Override
  public Optional<Node> get(NodeId id, User user, Select... selects) {
    return nodeDao.get(id, user).map(node -> populateValue(node, user));
  }

  private Node populateValue(Node node, User user) {
    node = new Node(node);

    node.setProperties(
        new NodeTextAttributeValueModelToDto().apply(textAttrValueDao.getMap(
            new NodeTextAttributeValuesByNodeId(new NodeId(node)), user)));

    node.setReferences(
        new ReferenceAttributeValueIdModelToDto()
            .apply(refAttrValueDao.getMap(
                new NodeReferenceAttributeValuesByNodeId(new NodeId(node)), user)));

    node.setReferrers(
        new ReferenceAttributeValueIdModelToReferrerDto()
            .apply(refAttrValueDao.getMap(
                new NodeReferenceAttributeNodesByValueId(new NodeId(node)), user)));

    return node;
  }

}
