package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.difference;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.NodeTextAttributeValueDtoToModel;
import fi.thl.termed.domain.transform.NodeTextAttributeValueModelToDto;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdDtoToModel;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToDto;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToReferrerDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class NodeRepository extends AbstractRepository<NodeId, Node> {

  private Dao<NodeId, Node> nodeDao;
  private Dao<NodeAttributeValueId, StrictLangValue> textAttributeValueDao;
  private Dao<NodeAttributeValueId, NodeId> referenceAttributeValueDao;

  public NodeRepository(
      Dao<NodeId, Node> nodeDao,
      Dao<NodeAttributeValueId, StrictLangValue> textAttributeValueDao,
      Dao<NodeAttributeValueId, NodeId> referenceAttributeValueDao) {

    this.nodeDao = nodeDao;
    this.textAttributeValueDao = textAttributeValueDao;
    this.referenceAttributeValueDao = referenceAttributeValueDao;
  }

  /**
   * With bulk insert, first save all nodes, then dependant values.
   */
  @Override
  public void insert(Map<NodeId, Node> map, SaveMode mode, WriteOptions opts, User user) {
    addCreatedInfo(map.values(), user);
    nodeDao.insert(map, user);

    Map<NodeAttributeValueId, StrictLangValue> textAttrValues = new LinkedHashMap<>();
    Map<NodeAttributeValueId, NodeId> refAttrValues = new LinkedHashMap<>();

    map.forEach((k, v) -> {
      textAttrValues.putAll(new NodeTextAttributeValueDtoToModel(k).apply(v.getProperties()));
      refAttrValues.putAll(new ReferenceAttributeValueIdDtoToModel(k).apply(v.getReferences()));
    });

    textAttributeValueDao.insert(textAttrValues, user);
    referenceAttributeValueDao.insert(refAttrValues, user);
  }

  @Override
  public void insert(NodeId id, Node node, SaveMode mode, WriteOptions opts, User user) {
    addCreatedInfo(node, new Date(), user);
    nodeDao.insert(id, node, user);
    insertTextAttrValues(id, node.getProperties(), user);
    insertRefAttrValues(id, node.getReferences(), user);
  }

  private void addCreatedInfo(Iterable<Node> values, User user) {
    Date now = new Date();
    for (Node node : values) {
      addCreatedInfo(node, now, user);
    }
  }

  private void addCreatedInfo(Node node, Date now, User user) {
    node.setCreatedDate(now);
    node.setCreatedBy(user.getUsername());
    node.setLastModifiedDate(now);
    node.setLastModifiedBy(user.getUsername());
  }

  private void insertTextAttrValues(NodeId id, Multimap<String, StrictLangValue> properties,
      User user) {
    textAttributeValueDao.insert(
        new NodeTextAttributeValueDtoToModel(id).apply(properties), user);
  }

  private void insertRefAttrValues(NodeId id, Multimap<String, NodeId> references, User user) {
    referenceAttributeValueDao.insert(
        new ReferenceAttributeValueIdDtoToModel(id).apply(references), user);
  }

  @Override
  public void update(NodeId id, Node node, SaveMode mode, WriteOptions opts, User user) {
    addLastModifiedInfo(node, nodeDao.get(id, user).orElseThrow(IllegalStateException::new), user);
    nodeDao.update(id, node, user);
    updateTextAttrValues(id, node.getProperties(), user);
    updateRefAttrValues(id, node.getReferences(), user);
  }

  private void addLastModifiedInfo(Node newNode, Node oldNode, User user) {
    Date now = new Date();
    newNode.setCreatedDate(oldNode.getCreatedDate());
    newNode.setCreatedBy(oldNode.getCreatedBy());
    newNode.setLastModifiedDate(now);
    newNode.setLastModifiedBy(user.getUsername());
  }

  private void updateTextAttrValues(NodeId nodeId, Multimap<String, StrictLangValue> properties,
      User user) {

    Map<NodeAttributeValueId, StrictLangValue> newMappedProperties =
        new NodeTextAttributeValueDtoToModel(nodeId).apply(properties);
    Map<NodeAttributeValueId, StrictLangValue> oldMappedProperties =
        textAttributeValueDao.getMap(new NodeTextAttributeValuesByNodeId(nodeId), user);

    MapDifference<NodeAttributeValueId, StrictLangValue> diff =
        difference(newMappedProperties, oldMappedProperties);

    textAttributeValueDao.insert(diff.entriesOnlyOnLeft(), user);
    textAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    textAttributeValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateRefAttrValues(NodeId nodeId, Multimap<String, NodeId> references, User user) {

    Map<NodeAttributeValueId, NodeId> newMappedRefs =
        new ReferenceAttributeValueIdDtoToModel(nodeId).apply(references);
    Map<NodeAttributeValueId, NodeId> oldMappedRefs =
        referenceAttributeValueDao.getMap(new NodeReferenceAttributeValuesByNodeId(nodeId), user);

    MapDifference<NodeAttributeValueId, NodeId> diff =
        difference(newMappedRefs, oldMappedRefs);

    referenceAttributeValueDao.insert(diff.entriesOnlyOnLeft(), user);
    referenceAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    referenceAttributeValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(NodeId nodeId, WriteOptions opts, User user) {
    deleteRefAttrValues(nodeId, user);
    deleteInverseRefAttrValues(nodeId, user);
    deleteTextAttrValues(nodeId, user);
    nodeDao.delete(nodeId, user);
  }

  private void deleteRefAttrValues(NodeId id, User user) {
    referenceAttributeValueDao.delete(referenceAttributeValueDao.getKeys(
        new NodeReferenceAttributeValuesByNodeId(id), user), user);
  }

  private void deleteInverseRefAttrValues(NodeId id, User user) {
    referenceAttributeValueDao.delete(referenceAttributeValueDao.getKeys(
        new NodeReferenceAttributeNodesByValueId(id), user), user);
  }

  private void deleteTextAttrValues(NodeId id, User user) {
    textAttributeValueDao.delete(textAttributeValueDao.getKeys(
        new NodeTextAttributeValuesByNodeId(id), user), user);
  }

  @Override
  public boolean exists(NodeId nodeId, User user) {
    return nodeDao.exists(nodeId, user);
  }

  @Override
  public Stream<Node> getValues(Query<NodeId, Node> query, User user) {
    return nodeDao.getValues(query.getWhere(), user).stream()
        .map(node -> populateValue(node, user));
  }

  @Override
  public Stream<NodeId> getKeys(Query<NodeId, Node> query, User user) {
    return nodeDao.getKeys(query.getWhere(), user).stream();
  }

  @Override
  public Optional<Node> get(NodeId id, User user, Select... selects) {
    return nodeDao.get(id, user).map(node -> populateValue(node, user));
  }

  private Node populateValue(Node node, User user) {
    node = new Node(node);

    node.setProperties(
        new NodeTextAttributeValueModelToDto().apply(textAttributeValueDao.getMap(
            new NodeTextAttributeValuesByNodeId(new NodeId(node)), user)));

    node.setReferences(
        new ReferenceAttributeValueIdModelToDto()
            .apply(referenceAttributeValueDao.getMap(
                new NodeReferenceAttributeValuesByNodeId(new NodeId(node)), user)));

    node.setReferrers(
        new ReferenceAttributeValueIdModelToReferrerDto()
            .apply(referenceAttributeValueDao.getMap(
                new NodeReferenceAttributeNodesByValueId(new NodeId(node)), user)));

    return node;
  }

}
