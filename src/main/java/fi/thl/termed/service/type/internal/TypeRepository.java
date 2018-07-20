package fi.thl.termed.service.type.internal;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.collect.MapUtils.leftValues;
import static fi.thl.termed.util.collect.MultimapUtils.toImmutableMultimap;
import static fi.thl.termed.util.collect.Tuple.entriesAsTuples;
import static fi.thl.termed.util.collect.Tuple.tuplesToMap;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.SaveMode.UPSERT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel2;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel2;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository2;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.service.WriteOptions;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TypeRepository extends AbstractRepository2<TypeId, Type> {

  private Dao2<TypeId, Type> typeDao;
  private Dao2<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao;
  private Dao2<PropertyValueId<TypeId>, LangValue> typePropertyDao;

  private Service2<TextAttributeId, TextAttribute> textAttributeRepository;
  private Service2<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository;

  public TypeRepository(
      Dao2<TypeId, Type> typeDao,
      Dao2<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao,
      Dao2<PropertyValueId<TypeId>, LangValue> typePropertyDao,
      Service2<TextAttributeId, TextAttribute> textAttributeRepository,
      Service2<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository,
      int batchSize) {
    super(batchSize);
    this.typeDao = typeDao;
    this.typePermissionDao = typePermissionDao;
    this.typePropertyDao = typePropertyDao;
    this.textAttributeRepository = textAttributeRepository;
    this.referenceAttributeRepository = referenceAttributeRepository;
  }

  @Override
  protected Stream<TypeId> insertBatch(List<Tuple2<TypeId, Type>> types, WriteOptions opts,
      User user) {
    typeDao.insert(types.stream(), user);
    types.forEach(t -> {
      insertPermissions(t._1, t._2.getPermissions(), user);
      insertProperties(t._1, t._2.getProperties(), user);
      saveTextAttributes(t._1, t._2.getTextAttributes(), INSERT, opts, user);
      saveReferenceAttributes(t._1, t._2.getReferenceAttributes(), INSERT, opts, user);
    });
    return types.stream().map(t -> t._1);
  }

  @Override
  public void insert(TypeId id, Type type, WriteOptions opts, User user) {
    typeDao.insert(id, type, user);

    insertPermissions(id, type.getPermissions(), user);
    insertProperties(id, type.getProperties(), user);

    saveTextAttributes(id, type.getTextAttributes(), INSERT, opts, user);
    saveReferenceAttributes(id, type.getReferenceAttributes(), INSERT, opts, user);
  }

  private void insertPermissions(TypeId id, Multimap<String, Permission> permissions, User user) {
    typePermissionDao.insert(
        new RolePermissionsDtoToModel2<>(id.getGraph(), id).apply(permissions), user);
  }

  private void insertProperties(TypeId id, Multimap<String, LangValue> properties, User user) {
    typePropertyDao.insert(
        new PropertyValueDtoToModel2<>(id).apply(properties), user);
  }

  private void saveTextAttributes(TypeId id, List<TextAttribute> textAttributes, SaveMode mode,
      WriteOptions opts, User user) {

    try (Stream<TextAttributeId> oldAttrIds = textAttributeRepository
        .keys(new Query<>(new TextAttributesByTypeId(id)), user)) {

      Set<TextAttributeId> textAttributeIds =
          textAttributes.stream().map(TextAttribute::identifier).collect(toSet());
      List<TextAttributeId> deletedAttributeIds = oldAttrIds
          .filter(oldAttrId -> !textAttributeIds.contains(oldAttrId)).collect(toList());

      textAttributeRepository.delete(deletedAttributeIds.stream(), opts, user);
      textAttributeRepository.save(textAttributes.stream(), mode, opts, user);
    }
  }

  private void saveReferenceAttributes(TypeId id, List<ReferenceAttribute> refAttrs, SaveMode mode,
      WriteOptions opts, User user) {
    try (Stream<ReferenceAttributeId> oldAttrIds = referenceAttributeRepository
        .keys(new Query<>(new ReferenceAttributesByTypeId(id)), user)) {

      Set<ReferenceAttributeId> refAttributeIds =
          refAttrs.stream().map(ReferenceAttribute::identifier).collect(toSet());
      List<ReferenceAttributeId> deletedAttributeIds = oldAttrIds
          .filter(oldAttrId -> !refAttributeIds.contains(oldAttrId)).collect(toList());

      referenceAttributeRepository.delete(deletedAttributeIds.stream(), opts, user);
      referenceAttributeRepository.save(refAttrs.stream(), mode, opts, user);
    }
  }

  @Override
  public void update(TypeId id, Type type, WriteOptions opts, User user) {
    typeDao.update(id, type, user);

    updatePermissions(id, type.getPermissions(), user);
    updateProperties(id, type.getProperties(), user);

    // upsert dependent objects even if given mode is UPDATE
    saveTextAttributes(id, type.getTextAttributes(), UPSERT, opts, user);
    saveReferenceAttributes(id, type.getReferenceAttributes(), UPSERT, opts, user);
  }

  private void updatePermissions(TypeId id, Multimap<String, Permission> permissions, User user) {
    Map<ObjectRolePermission<TypeId>, GrantedPermission> newPermissionMap =
        tuplesToMap(new RolePermissionsDtoToModel2<>(id.getGraph(), id).apply(permissions));
    Map<ObjectRolePermission<TypeId>, GrantedPermission> oldPermissionMap =
        tuplesToMap(typePermissionDao.getEntries(new TypePermissionsByTypeId(id), user));

    MapDifference<ObjectRolePermission<TypeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    typePermissionDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    typePermissionDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  private void updateProperties(TypeId id, Multimap<String, LangValue> properties, User user) {
    Map<PropertyValueId<TypeId>, LangValue> newProperties =
        tuplesToMap(new PropertyValueDtoToModel2<>(id).apply(properties));
    Map<PropertyValueId<TypeId>, LangValue> oldProperties =
        tuplesToMap(typePropertyDao.getEntries(new TypePropertiesByTypeId(id), user));

    MapDifference<PropertyValueId<TypeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    typePropertyDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    typePropertyDao.update(entriesAsTuples(leftValues(diff.entriesDiffering())), user);
    typePropertyDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  @Override
  public void delete(TypeId id, WriteOptions opts, User user) {
    deletePermissions(id, user);
    deleteProperties(id, user);
    deleteTextAttributes(id, opts, user);
    deleteReferenceAttributes(id, opts, user);
    deleteReferringReferenceAttributes(id, opts, user);
    typeDao.delete(id, user);
  }

  private void deletePermissions(TypeId id, User user) {
    typePermissionDao
        .delete(typePermissionDao.getKeys(new TypePermissionsByTypeId(id), user), user);
  }

  private void deleteProperties(TypeId id, User user) {
    typePropertyDao.delete(typePropertyDao.getKeys(new TypePropertiesByTypeId(id), user), user);
  }

  private void deleteTextAttributes(TypeId id, WriteOptions opts, User user) {
    textAttributeRepository.delete(textAttributeRepository.keys(
        new Query<>(new TextAttributesByTypeId(id)), user), opts, user);
  }

  private void deleteReferenceAttributes(TypeId id, WriteOptions opts, User user) {
    referenceAttributeRepository.delete(referenceAttributeRepository.keys(
        new Query<>(new ReferenceAttributesByTypeId(id)), user), opts, user);
  }

  private void deleteReferringReferenceAttributes(TypeId id, WriteOptions opts, User user) {
    referenceAttributeRepository.delete(referenceAttributeRepository.keys(
        new Query<>(new ReferenceAttributesByRangeId(id)), user), opts, user);
  }

  @Override
  public boolean exists(TypeId id, User user) {
    return typeDao.exists(id, user);
  }

  @Override
  public Stream<Type> values(Query<TypeId, Type> spec, User user) {
    return typeDao.getValues(spec.getWhere(), user).map(cls -> populateValue(cls, user));
  }

  @Override
  public Stream<TypeId> keys(Query<TypeId, Type> spec, User user) {
    return typeDao.getKeys(spec.getWhere(), user);
  }

  @Override
  public Optional<Type> get(TypeId id, User user, Select... selects) {
    return typeDao.get(id, user).map(cls -> populateValue(cls, user));
  }

  private Type populateValue(Type type, User user) {
    TypeId id = type.identifier();

    try (
        Stream<ObjectRolePermission<TypeId>> permissionStream = typePermissionDao
            .getKeys(new TypePermissionsByTypeId(id), user);
        Stream<Tuple2<PropertyValueId<TypeId>, LangValue>> propertyStream = typePropertyDao
            .getEntries(new TypePropertiesByTypeId(id), user);
        Stream<TextAttribute> textAttributeStream = textAttributeRepository
            .values(new Query<>(new TextAttributesByTypeId(id)), user);
        Stream<ReferenceAttribute> referenceAttributeStream = referenceAttributeRepository
            .values(new Query<>(new ReferenceAttributesByTypeId(id)), user)) {

      return Type.builderFromCopyOf(type)
          .permissions(permissionStream.collect(toImmutableMultimap(
              ObjectRolePermission::getRole,
              ObjectRolePermission::getPermission)))
          .properties(propertyStream.collect(toImmutableMultimap(
              e -> e._1.getPropertyId(),
              e -> e._2)))
          .textAttributes(textAttributeStream.collect(toImmutableList()))
          .referenceAttributes(referenceAttributeStream.collect(toImmutableList()))
          .build();
    }
  }

}
