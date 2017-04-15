package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Maps.difference;

import com.google.common.collect.ImmutableList;
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
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdDtoToReferrerModel;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToDto;
import fi.thl.termed.domain.transform.ReferenceAttributeValueIdModelToReferrerDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Specification;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
  public void insert(Map<NodeId, Node> map, User user) {
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
  public void insert(NodeId id, Node node, User user) {
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

  private void insertTextAttrValues(NodeId id,
      Multimap<String, StrictLangValue> properties, User user) {
    textAttributeValueDao.insert(
        new NodeTextAttributeValueDtoToModel(id).apply(properties), user);
  }

  private void insertRefAttrValues(NodeId id, Multimap<String, NodeId> references,
      User user) {
    referenceAttributeValueDao.insert(
        new ReferenceAttributeValueIdDtoToModel(id).apply(references), user);
  }

  @Override
  public void update(NodeId id, Node newNode, Node oldNode, User user) {
    addLastModifiedInfo(newNode, oldNode, user);
    nodeDao.update(id, newNode, user);
    updateTextAttrValues(id, newNode.getProperties(), oldNode.getProperties(), user);
    updateRefAttrValues(id, newNode.getReferences(), oldNode.getReferences(), user);
  }

  private void addLastModifiedInfo(Node newNode, Node oldNode, User user) {
    Date now = new Date();
    newNode.setCreatedDate(oldNode.getCreatedDate());
    newNode.setCreatedBy(oldNode.getCreatedBy());
    newNode.setLastModifiedDate(now);
    newNode.setLastModifiedBy(user.getUsername());
  }

  private void updateTextAttrValues(NodeId nodeId,
      Multimap<String, StrictLangValue> newProperties,
      Multimap<String, StrictLangValue> oldProperties,
      User user) {

    Map<NodeAttributeValueId, StrictLangValue> newMappedProperties =
        new NodeTextAttributeValueDtoToModel(nodeId).apply(newProperties);
    Map<NodeAttributeValueId, StrictLangValue> oldMappedProperties =
        new NodeTextAttributeValueDtoToModel(nodeId).apply(oldProperties);

    MapDifference<NodeAttributeValueId, StrictLangValue> diff =
        difference(newMappedProperties, oldMappedProperties);

    textAttributeValueDao.insert(diff.entriesOnlyOnLeft(), user);
    textAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    textAttributeValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateRefAttrValues(NodeId nodeId,
      Multimap<String, NodeId> newRefs,
      Multimap<String, NodeId> oldRefs,
      User user) {

    Map<NodeAttributeValueId, NodeId> newMappedRefs =
        new ReferenceAttributeValueIdDtoToModel(nodeId).apply(newRefs);
    Map<NodeAttributeValueId, NodeId> oldMappedRefs =
        new ReferenceAttributeValueIdDtoToModel(nodeId).apply(oldRefs);

    MapDifference<NodeAttributeValueId, NodeId> diff =
        difference(newMappedRefs, oldMappedRefs);

    referenceAttributeValueDao.insert(diff.entriesOnlyOnLeft(), user);
    referenceAttributeValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    referenceAttributeValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(NodeId nodeId, Node node, User user) {
    deleteRefAttrValues(nodeId, node.getReferences(), user);
    deleteInverseRefAttrValues(nodeId, node.getReferrers(), user);
    deleteTextAttrValues(nodeId, node.getProperties(), user);
    nodeDao.delete(nodeId, user);
  }

  private void deleteRefAttrValues(NodeId id, Multimap<String, NodeId> refs, User user) {
    Map<NodeAttributeValueId, NodeId> mappedRefs =
        new ReferenceAttributeValueIdDtoToModel(id).apply(refs);
    referenceAttributeValueDao.delete(ImmutableList.copyOf(mappedRefs.keySet()), user);
  }

  private void deleteInverseRefAttrValues(NodeId id, Multimap<String, NodeId> refs, User user) {
    Map<NodeAttributeValueId, NodeId> mappedRefs =
        new ReferenceAttributeValueIdDtoToReferrerModel(id).apply(refs);
    referenceAttributeValueDao.delete(ImmutableList.copyOf(mappedRefs.keySet()), user);
  }

  private void deleteTextAttrValues(NodeId id, Multimap<String, StrictLangValue> properties,
      User user) {
    Map<NodeAttributeValueId, StrictLangValue> mappedProperties =
        new NodeTextAttributeValueDtoToModel(id).apply(properties);
    textAttributeValueDao.delete(ImmutableList.copyOf(mappedProperties.keySet()), user);
  }

  @Override
  public boolean exists(NodeId nodeId, User user) {
    return nodeDao.exists(nodeId, user);
  }

  @Override
  public List<Node> get(Specification<NodeId, Node> specification, User user) {
    return nodeDao.getValues(specification, user).stream()
        .map(node -> populateValue(node, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<NodeId> getKeys(Specification<NodeId, Node> specification, User user) {
    return nodeDao.getKeys(specification, user);
  }

  @Override
  public Optional<Node> get(NodeId id, User user) {
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
