package fi.thl.termed.service.node.internal;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Maps.difference;
import static fi.thl.termed.domain.NodeTransformations.nodePropertiesToRows;
import static fi.thl.termed.domain.NodeTransformations.nodeReferencesToRows;
import static fi.thl.termed.domain.RevisionType.DELETE;
import static fi.thl.termed.domain.RevisionType.INSERT;
import static fi.thl.termed.domain.RevisionType.UPDATE;
import static fi.thl.termed.util.collect.MapUtils.leftValues;
import static fi.thl.termed.util.collect.MultimapUtils.toImmutableMultimap;
import static fi.thl.termed.util.collect.Tuple.entriesAsTuples;
import static fi.thl.termed.util.collect.Tuple.tuplesToMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository2;
import fi.thl.termed.util.service.WriteOptions;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Coordinates CRUD-operations on Nodes to simpler DAOs.
 */
public class NodeRepository extends AbstractRepository2<NodeId, Node> {

  private final Dao2<NodeId, Node> nodeDao;
  private final Dao2<NodeAttributeValueId, StrictLangValue> textAttrValueDao;
  private final Dao2<NodeAttributeValueId, NodeId> refAttrValueDao;

  private final Dao2<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevDao;
  private final Dao2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttrValueRevDao;
  private final Dao2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> refAttrValueRevDao;

  public NodeRepository(
      Dao2<NodeId, Node> nodeDao,
      Dao2<NodeAttributeValueId, StrictLangValue> textAttrValueDao,
      Dao2<NodeAttributeValueId, NodeId> refAttrValueDao,
      Dao2<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevDao,
      Dao2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttrValueRevDao,
      Dao2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> refAttrValueRevDao,
      int batchSize) {
    super(batchSize);
    this.nodeDao = nodeDao;
    this.textAttrValueDao = textAttrValueDao;
    this.refAttrValueDao = refAttrValueDao;
    this.nodeRevDao = nodeRevDao;
    this.textAttrValueRevDao = textAttrValueRevDao;
    this.refAttrValueRevDao = refAttrValueRevDao;
  }

  @Override
  protected Stream<NodeId> insertBatch(List<Tuple2<NodeId, Node>> nodes, WriteOptions opts,
      User user) {

    ImmutableList<Tuple2<NodeAttributeValueId, StrictLangValue>> textValues = nodes.stream()
        .flatMap(idNode -> nodePropertiesToRows(idNode._1, idNode._2.getProperties()))
        .collect(toImmutableList());
    ImmutableList<Tuple2<NodeAttributeValueId, NodeId>> refValues = nodes.stream()
        .flatMap(idNode -> nodeReferencesToRows(idNode._1, idNode._2.getReferences()))
        .collect(toImmutableList());

    // first save all nodes, then all dependant values
    nodeDao.insert(nodes.stream(), user);
    textAttrValueDao.insert(textValues.stream(), user);
    refAttrValueDao.insert(refValues.stream(), user);

    opts.getRevision().ifPresent(r -> {
      nodeRevDao.insert(toRevs(nodes.stream(), r, INSERT), user);
      textAttrValueRevDao.insert(toRevs(textValues.stream(), r, INSERT), user);
      refAttrValueRevDao.insert(toRevs(refValues.stream(), r, INSERT), user);
    });

    return nodes.stream().map(e -> e._1);
  }

  @Override
  public void insert(NodeId id, Node node, WriteOptions opts, User user) {
    ImmutableList<Tuple2<NodeAttributeValueId, StrictLangValue>> textAttrValues =
        nodePropertiesToRows(id, node.getProperties()).collect(toImmutableList());
    ImmutableList<Tuple2<NodeAttributeValueId, NodeId>> refAttrValues =
        nodeReferencesToRows(id, node.getReferences()).collect(toImmutableList());

    nodeDao.insert(id, node, user);
    textAttrValueDao.insert(textAttrValues.stream(), user);
    refAttrValueDao.insert(refAttrValues.stream(), user);

    opts.getRevision().ifPresent(r -> {
      nodeRevDao.insert(RevisionId.of(id, r), Tuple.of(INSERT, node), user);
      textAttrValueRevDao.insert(toRevs(textAttrValues.stream(), r, INSERT), user);
      refAttrValueRevDao.insert(toRevs(refAttrValues.stream(), r, INSERT), user);
    });
  }

  @Override
  public void update(NodeId id, Node node, WriteOptions opts, User user) {
    MapDifference<NodeAttributeValueId, StrictLangValue> textsDiff = difference(
        tuplesToMap(nodePropertiesToRows(id, node.getProperties())),
        tuplesToMap(textAttrValueDao.getEntries(new NodeTextAttributeValuesByNodeId(id), user)));
    MapDifference<NodeAttributeValueId, NodeId> refsDiff = difference(
        tuplesToMap(nodeReferencesToRows(id, node.getReferences())),
        tuplesToMap(
            refAttrValueDao.getEntries(new NodeReferenceAttributeValuesByNodeId(id), user)));

    nodeDao.update(id, node, user);

    textAttrValueDao.insert(entriesAsTuples(textsDiff.entriesOnlyOnLeft()), user);
    textAttrValueDao.update(entriesAsTuples(leftValues(textsDiff.entriesDiffering())), user);
    textAttrValueDao.delete(textsDiff.entriesOnlyOnRight().keySet().stream(), user);

    refAttrValueDao.insert(entriesAsTuples(refsDiff.entriesOnlyOnLeft()), user);
    refAttrValueDao.update(entriesAsTuples(leftValues(refsDiff.entriesDiffering())), user);
    refAttrValueDao.delete(refsDiff.entriesOnlyOnRight().keySet().stream(), user);

    opts.getRevision().ifPresent(r -> {
      nodeRevDao.insert(RevisionId.of(id, r), Tuple.of(UPDATE, node), user);

      textAttrValueRevDao.insert(
          toRevs(entriesAsTuples(textsDiff.entriesOnlyOnLeft()), r, INSERT), user);
      textAttrValueRevDao.insert(
          toRevs(entriesAsTuples(leftValues(textsDiff.entriesDiffering())), r, UPDATE), user);
      textAttrValueRevDao.insert(
          toRevs(textsDiff.entriesOnlyOnRight().keySet(), r, DELETE), user);

      refAttrValueRevDao.insert(
          toRevs(entriesAsTuples(refsDiff.entriesOnlyOnLeft()), r, INSERT), user);
      refAttrValueRevDao.insert(
          toRevs(entriesAsTuples(leftValues(refsDiff.entriesDiffering())), r, UPDATE), user);
      refAttrValueRevDao.insert(
          toRevs(refsDiff.entriesOnlyOnRight().keySet(), r, DELETE), user);
    });
  }

  // first delete all dependant values, then all nodes
  @Override
  protected void deleteBatch(List<NodeId> ids, WriteOptions opts, User user) {
    // note that flatMap will close streams returned by DAOs
    ImmutableList<NodeAttributeValueId> allTextAttrValueIds = ids.stream()
        .flatMap(id -> textAttrValueDao.getKeys(new NodeTextAttributeValuesByNodeId(id), user))
        .collect(toImmutableList());
    ImmutableList<NodeAttributeValueId> allRefAttrValueIds = ids.stream()
        .flatMap(id -> refAttrValueDao.getKeys(new NodeReferenceAttributeValuesByNodeId(id), user))
        .collect(toImmutableList());

    textAttrValueDao.delete(allTextAttrValueIds.stream(), user);
    refAttrValueDao.delete(allRefAttrValueIds.stream(), user);
    nodeDao.delete(ids.stream(), user);

    opts.getRevision().ifPresent(r -> {
      textAttrValueRevDao.insert(toRevs(allTextAttrValueIds, r, DELETE), user);
      refAttrValueRevDao.insert(toRevs(allRefAttrValueIds, r, DELETE), user);
      nodeRevDao.insert(toRevs(ids, r, DELETE), user);
    });
  }

  @Override
  public void delete(NodeId id, WriteOptions opts, User user) {
    try (
        Stream<NodeAttributeValueId> textAttrValueIds = textAttrValueDao.getKeys(
            new NodeTextAttributeValuesByNodeId(id), user);
        Stream<NodeAttributeValueId> refAttrValueIds = refAttrValueDao.getKeys(
            new NodeReferenceAttributeValuesByNodeId(id), user)) {

      ImmutableList<NodeAttributeValueId> textAttrValueIdList = textAttrValueIds
          .collect(toImmutableList());
      ImmutableList<NodeAttributeValueId> refAttrValueIdList = refAttrValueIds
          .collect(toImmutableList());

      textAttrValueDao.delete(textAttrValueIdList.stream(), user);
      refAttrValueDao.delete(refAttrValueIdList.stream(), user);
      nodeDao.delete(id, user);

      opts.getRevision().ifPresent(r -> {
        textAttrValueRevDao.insert(toRevs(textAttrValueIdList, r, DELETE), user);
        refAttrValueRevDao.insert(toRevs(refAttrValueIdList, r, DELETE), user);
        nodeRevDao.insert(RevisionId.of(id, r), Tuple.of(DELETE, null), user);
      });
    }
  }

  private <K extends Serializable, V> Stream<Tuple2<RevisionId<K>, Tuple2<RevisionType, V>>> toRevs(
      Stream<Tuple2<K, V>> entries, Long revision, RevisionType revisionType) {
    return entries.map(e -> Tuple.of(RevisionId.of(e._1, revision), Tuple.of(revisionType, e._2)));
  }

  private <K extends Serializable, V> Stream<Tuple2<RevisionId<K>, Tuple2<RevisionType, V>>> toRevs(
      Collection<K> keys, Long revision, RevisionType revisionType) {
    return keys.stream()
        .map(key -> Tuple.of(RevisionId.of(key, revision), Tuple.of(revisionType, (V) null)));
  }

  @Override
  public boolean exists(NodeId nodeId, User user) {
    return nodeDao.exists(nodeId, user);
  }

  @Override
  public Stream<Node> values(Query<NodeId, Node> query, User user) {
    return nodeDao.getValues(query.getWhere(), user).map(node -> populateValue(node, user));
  }

  @Override
  public Stream<NodeId> keys(Query<NodeId, Node> query, User user) {
    return nodeDao.getKeys(query.getWhere(), user);
  }

  @Override
  public Optional<Node> get(NodeId id, User user, Select... selects) {
    return nodeDao.get(id, user).map(node -> populateValue(node, user));
  }

  private Node populateValue(Node node, User user) {
    NodeId nodeId = node.identifier();

    try (
        Stream<Tuple2<NodeAttributeValueId, StrictLangValue>> texts = textAttrValueDao
            .getEntries(new NodeTextAttributeValuesByNodeId(nodeId), user);
        Stream<Tuple2<NodeAttributeValueId, NodeId>> references = refAttrValueDao.getEntries(
            new NodeReferenceAttributeValuesByNodeId(nodeId), user);
        Stream<Tuple2<NodeAttributeValueId, NodeId>> referrers = refAttrValueDao.getEntries(
            new NodeReferenceAttributeNodesByValueId(nodeId), user)) {

      node = new Node(node);

      node.setProperties(texts.collect(toImmutableMultimap(
          e -> e._1.getAttributeId(),
          e -> e._2)));

      node.setReferences(references.collect(toImmutableMultimap(
          e -> e._1.getAttributeId(),
          e -> e._2)));

      node.setReferrers(referrers.collect(toImmutableMultimap(
          e -> e._1.getAttributeId(),
          e -> e._1.getNodeId())));

      return node;
    }
  }

}
