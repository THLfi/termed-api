package fi.thl.termed.service.type.internal;

import static fi.thl.termed.util.collect.MapUtils.leftValues;
import static fi.thl.termed.util.collect.MultimapUtils.toImmutableMultimap;
import static fi.thl.termed.util.collect.Tuple.entriesAsTuples;
import static fi.thl.termed.util.collect.Tuple.tuplesToMap;

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
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.WriteOptions;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class ReferenceAttributeRepository
    extends AbstractRepository<ReferenceAttributeId, ReferenceAttribute> {

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;
  private Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> permissionDao;
  private Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyDao;

  public ReferenceAttributeRepository(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> permissionDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyDao) {
    this.referenceAttributeDao = referenceAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyDao = propertyDao;
  }

  @Override
  public void insert(ReferenceAttributeId id, ReferenceAttribute attr, WriteOptions opts,
      User user) {
    referenceAttributeDao.insert(id, attr, user);
    insertPermissions(id, attr.getPermissions(), user);
    insertProperties(id, attr.getProperties(), user);
  }

  private void insertPermissions(ReferenceAttributeId attributeId,
      Multimap<String, Permission> permissions, User user) {
    TypeId domainId = attributeId.getDomainId();
    permissionDao.insert(new RolePermissionsDtoToModel<>(
        domainId.getGraph(), attributeId).apply(permissions), user);
  }

  private void insertProperties(ReferenceAttributeId attributeId,
      Multimap<String, LangValue> properties, User user) {
    propertyDao.insert(new PropertyValueDtoToModel<>(attributeId).apply(properties), user);
  }

  @Override
  public void update(ReferenceAttributeId id, ReferenceAttribute attribute, WriteOptions opts,
      User user) {
    referenceAttributeDao.update(id, attribute, user);
    updatePermissions(id, attribute.getPermissions(), user);
    updateProperties(id, attribute.getProperties(), user);
  }

  private void updatePermissions(ReferenceAttributeId attrId,
      Multimap<String, Permission> permissions, User user) {

    TypeId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> newPermissionMap =
        tuplesToMap(
            new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(permissions));
    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> oldPermissionMap =
        tuplesToMap(permissionDao.entries(
            new ReferenceAttributePermissionsByReferenceAttributeId(attrId), user));

    MapDifference<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    permissionDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  private void updateProperties(ReferenceAttributeId attributeId,
      Multimap<String, LangValue> properties, User user) {

    Map<PropertyValueId<ReferenceAttributeId>, LangValue> newProperties =
        tuplesToMap(new PropertyValueDtoToModel<>(attributeId).apply(properties));
    Map<PropertyValueId<ReferenceAttributeId>, LangValue> oldProperties =
        tuplesToMap(propertyDao.entries(
            new ReferenceAttributePropertiesByAttributeId(attributeId), user));

    MapDifference<PropertyValueId<ReferenceAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    propertyDao.update(entriesAsTuples(leftValues(diff.entriesDiffering())), user);
    propertyDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  @Override
  public void delete(ReferenceAttributeId id, WriteOptions opts, User user) {
    deletePermissions(id, user);
    deleteProperties(id, user);
    referenceAttributeDao.delete(id, user);
  }

  private void deletePermissions(ReferenceAttributeId id, User user) {
    permissionDao.delete(permissionDao.keys(
        new ReferenceAttributePermissionsByReferenceAttributeId(id), user), user);
  }

  private void deleteProperties(ReferenceAttributeId id, User user) {
    propertyDao.delete(propertyDao.keys(
        new ReferenceAttributePropertiesByAttributeId(id), user), user);
  }

  @Override
  public boolean exists(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.exists(id, user);
  }

  @Override
  public Stream<ReferenceAttribute> values(
      Query<ReferenceAttributeId, ReferenceAttribute> spec, User user) {
    return referenceAttributeDao.values(spec.getWhere(), user)
        .map(attribute -> populateValue(attribute, user));
  }

  @Override
  public Stream<ReferenceAttributeId> keys(
      Query<ReferenceAttributeId, ReferenceAttribute> spec, User user) {
    return referenceAttributeDao.keys(spec.getWhere(), user);
  }

  @Override
  public Optional<ReferenceAttribute> get(ReferenceAttributeId id, User user, Select... selects) {
    return referenceAttributeDao.get(id, user).map(attribute -> populateValue(attribute, user));
  }

  private ReferenceAttribute populateValue(ReferenceAttribute attribute, User user) {
    ReferenceAttributeId id = attribute.identifier();

    try (
        Stream<ObjectRolePermission<ReferenceAttributeId>> permissionStream = permissionDao
            .keys(new ReferenceAttributePermissionsByReferenceAttributeId(id), user);
        Stream<Tuple2<PropertyValueId<ReferenceAttributeId>, LangValue>> propertyStream = propertyDao
            .entries(new ReferenceAttributePropertiesByAttributeId(id), user)) {

      return ReferenceAttribute.builderFromCopyOf(attribute)
          .permissions(permissionStream.collect(toImmutableMultimap(
              ObjectRolePermission::getRole,
              ObjectRolePermission::getPermission)))
          .properties(propertyStream.collect(toImmutableMultimap(
              e -> e._1.getPropertyId(),
              e -> e._2)))
          .build();
    }
  }

}
