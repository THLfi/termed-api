package fi.thl.termed.service.node.internal;

import static fi.thl.termed.domain.NodeTransformations.nodePropertiesToRows;
import static fi.thl.termed.domain.NodeTransformations.nodeReferencesToRows;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.NodeTransformations;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Coordinates CRUD-operations on Nodes to simpler DAOs. Revision reads are typically done here.
 * Incremental revision updates are automatically made by NodeRepository. This repository can do
 * full node revision saves which are useful in e.g. admin operations.
 */
public class NodeRevisionRepository implements
    Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private static final int BATCH_SIZE = 5000;

  private Dao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionDao;
  private Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttributeValueRevDao;
  private Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> referenceAttributeValueRevDao;

  private Service<Long, Revision> revisionService;
  private SequenceService revisionSeqService;

  public NodeRevisionRepository(
      Dao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionDao,
      Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> textAttributeValueRevDao,
      Dao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>> referenceAttributeValueRevDao,
      Service<Long, Revision> revisionService, SequenceService revisionSeqService) {
    this.nodeRevisionDao = nodeRevisionDao;
    this.textAttributeValueRevDao = textAttributeValueRevDao;
    this.referenceAttributeValueRevDao = referenceAttributeValueRevDao;
    this.revisionService = revisionService;
    this.revisionSeqService = revisionSeqService;
  }

  private <K, V> Tuple2<RevisionId<K>, Tuple2<RevisionType, V>> toRev(
      K key, V value, RevisionType revisionType, Long revision) {
    return Tuple.of(RevisionId.of(key, revision), Tuple.of(revisionType, value));
  }

  @Override
  public void save(Stream<Tuple2<RevisionType, Node>> entries, SaveMode mode, WriteOptions opts,
      User user) {

    Preconditions.checkArgument(mode == INSERT);

    Long revision = opts.getRevision().orElseGet(() -> newRevision(user));

    try (Stream<Tuple2<RevisionType, Node>> closeable = entries) {
      Iterators.partition(closeable.iterator(), BATCH_SIZE).forEachRemaining(batch -> {
        nodeRevisionDao.insert(batch.stream()
            .map(e -> toRev(e._2.identifier(), e._2, e._1, revision)), user);

        textAttributeValueRevDao.insert(batch.stream()
            .flatMap(e -> nodePropertiesToRows(e._2.identifier(), e._2.getProperties())
                .map(property -> toRev(property._1, property._2, e._1, revision))), user);

        referenceAttributeValueRevDao.insert(batch.stream()
            .flatMap(e -> nodeReferencesToRows(e._2.identifier(), e._2.getReferences())
                .map(reference -> toRev(reference._1, reference._2, e._1, revision))), user);
      });
    }
  }

  @Override
  public RevisionId<NodeId> save(Tuple2<RevisionType, Node> revisionTypeAndNode, SaveMode mode,
      WriteOptions opts, User user) {

    Preconditions.checkArgument(mode == INSERT);

    Long revision = opts.getRevision().orElseGet(() -> newRevision(user));
    RevisionType type = revisionTypeAndNode._1;
    Node node = revisionTypeAndNode._2;
    NodeId id = node.identifier();

    nodeRevisionDao.insert(RevisionId.of(id, revision), Tuple.of(type, node), user);

    textAttributeValueRevDao.insert(
        nodePropertiesToRows(id, node.getProperties())
            .map(t -> Tuple.of(RevisionId.of(t._1, revision), Tuple.of(type, t._2))), user);

    referenceAttributeValueRevDao.insert(
        NodeTransformations.nodeReferencesToRows(id, node.getReferences())
            .map(t -> Tuple.of(RevisionId.of(t._1, revision), Tuple.of(type, t._2))), user);

    return RevisionId.of(id, revision);
  }

  private Long newRevision(User user) {
    return revisionService.save(
        Revision.of(revisionSeqService.getAndAdvance(user), user.getUsername(), new Date()),
        INSERT, defaultOpts(), user);
  }

  @Override
  public void delete(Stream<RevisionId<NodeId>> keys, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void delete(RevisionId<NodeId> id, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveAndDelete(Stream<Tuple2<RevisionType, Node>> saves,
      Stream<RevisionId<NodeId>> deletes, SaveMode mode, WriteOptions opts, User user) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(RevisionId<NodeId> nodeId, User user) {
    return nodeRevisionDao.exists(nodeId, user);
  }

  @Override
  public Stream<Tuple2<RevisionType, Node>> values(
      Query<RevisionId<NodeId>, Tuple2<RevisionType, Node>> query, User user) {
    return nodeRevisionDao.entries(query.getWhere(), user)
        .map(e -> populate(e._1, e._2, user));
  }

  @Override
  public long count(Specification<RevisionId<NodeId>, Tuple2<RevisionType, Node>> spec, User user) {
    try (Stream<RevisionId<NodeId>> keys = keys(new Query<>(spec), user)) {
      return keys.count();
    }
  }

  @Override
  public Stream<RevisionId<NodeId>> keys(
      Query<RevisionId<NodeId>, Tuple2<RevisionType, Node>> query, User user) {
    return nodeRevisionDao.keys(query.getWhere(), user);
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

    Node.Builder nodeBuilder = Node.builderFromCopyOf(rev._2)
        .properties(findPropertiesFor(id, user))
        .references(findReferencesFor(id, user));

    return Tuple.of(rev._1, nodeBuilder.build());
  }

  private Multimap<String, StrictLangValue> findPropertiesFor(RevisionId<NodeId> revId, User user) {
    try (Stream<Tuple2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>>>
        textAttributeValueRevisions = textAttributeValueRevDao.entries(
        new NodeRevisionTextAttributeValuesLessOrEqualToRevision(revId), user)) {

      Map<NodeAttributeValueId, List<Tuple2<Long, StrictLangValue>>> idMappedValueRevisions =
          textAttributeValueRevisions
              .collect(groupingBy(e -> e._1.getId(), LinkedHashMap::new,
                  mapping(e -> Tuple.of(e._1.getRevision(), e._2._2), toList())));

      Multimap<String, StrictLangValue> properties = LinkedHashMultimap.create();

      idMappedValueRevisions.forEach((attrId, valueRevs) -> {
        Optional<StrictLangValue> lastRevValue = valueRevs.stream()
            .max(comparing(t -> t._1))
            .map(t -> t._2);

        lastRevValue.ifPresent(value -> properties.put(attrId.getAttributeId(), value));
      });

      return properties;
    }
  }

  private Multimap<String, NodeId> findReferencesFor(RevisionId<NodeId> revId, User user) {
    try (Stream<Tuple2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, NodeId>>>
        referenceAttributeValueRevisions = referenceAttributeValueRevDao.entries(
        new NodeRevisionReferenceAttributeValuesLessOrEqualToRevision(revId), user)) {

      Map<NodeAttributeValueId, List<Tuple2<Long, NodeId>>> idMappedValueRevisions =
          referenceAttributeValueRevisions
              .collect(groupingBy(e -> e._1.getId(), LinkedHashMap::new,
                  mapping(e -> Tuple.of(e._1.getRevision(), e._2._2), toList())));

      Multimap<String, NodeId> references = LinkedHashMultimap.create();

      idMappedValueRevisions.forEach((attrId, valueRevs) -> {
        Optional<NodeId> lastRevValue = valueRevs.stream()
            .max(comparing(t -> t._1))
            .map(t -> t._2);

        lastRevValue.ifPresent(value -> references.put(attrId.getAttributeId(), value));
      });

      return references;
    }
  }

}
