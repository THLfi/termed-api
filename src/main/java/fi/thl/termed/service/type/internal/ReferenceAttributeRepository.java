package fi.thl.termed.service.type.internal;

import static com.google.common.collect.ImmutableList.copyOf;

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
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsModelToDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.SaveMode;
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
  public void insert(ReferenceAttributeId id, ReferenceAttribute attr, SaveMode mode,
      WriteOptions opts, User user) {
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
  public void update(ReferenceAttributeId id, ReferenceAttribute attribute, SaveMode mode,
      WriteOptions opts, User user) {
    referenceAttributeDao.update(id, attribute, user);
    updatePermissions(id, attribute.getPermissions(), user);
    updateProperties(id, attribute.getProperties(), user);
  }

  private void updatePermissions(ReferenceAttributeId attrId,
      Multimap<String, Permission> permissions, User user) {

    TypeId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(permissions);
    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> oldPermissionMap =
        permissionDao.getMap(new ReferenceAttributePermissionsByReferenceAttributeId(attrId), user);

    MapDifference<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(diff.entriesOnlyOnLeft(), user);
    permissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(ReferenceAttributeId attributeId,
      Multimap<String, LangValue> properties, User user) {

    Map<PropertyValueId<ReferenceAttributeId>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(attributeId).apply(properties);
    Map<PropertyValueId<ReferenceAttributeId>, LangValue> oldProperties =
        propertyDao.getMap(new ReferenceAttributePropertiesByAttributeId(attributeId), user);

    MapDifference<PropertyValueId<ReferenceAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    propertyDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(ReferenceAttributeId id, WriteOptions opts, User user) {
    deletePermissions(id, user);
    deleteProperties(id, user);
    referenceAttributeDao.delete(id, user);
  }

  private void deletePermissions(ReferenceAttributeId id, User user) {
    permissionDao.delete(permissionDao.getKeys(
        new ReferenceAttributePermissionsByReferenceAttributeId(id), user), user);
  }

  private void deleteProperties(ReferenceAttributeId id, User user) {
    propertyDao.delete(propertyDao.getKeys(
        new ReferenceAttributePropertiesByAttributeId(id), user), user);
  }

  @Override
  public boolean exists(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.exists(id, user);
  }

  @Override
  public Stream<ReferenceAttribute> getValues(
      Query<ReferenceAttributeId, ReferenceAttribute> spec, User user) {
    return referenceAttributeDao.getValues(spec.getWhere(), user).stream()
        .map(attribute -> populateValue(attribute, user));
  }

  @Override
  public Stream<ReferenceAttributeId> getKeys(
      Query<ReferenceAttributeId, ReferenceAttribute> spec, User user) {
    return referenceAttributeDao.getKeys(spec.getWhere(), user).stream();
  }

  @Override
  public Optional<ReferenceAttribute> get(ReferenceAttributeId id, User user, Select... selects) {
    return referenceAttributeDao.get(id, user).map(attribute -> populateValue(attribute, user));
  }

  private ReferenceAttribute populateValue(ReferenceAttribute attribute, User user) {
    return ReferenceAttribute.builderFromCopyOf(attribute)
        .permissions(new RolePermissionsModelToDto<ReferenceAttributeId>().apply(
            permissionDao.getMap(new ReferenceAttributePermissionsByReferenceAttributeId(
                new ReferenceAttributeId(attribute)), user)))
        .properties(
            new PropertyValueModelToDto<ReferenceAttributeId>().apply(propertyDao.getMap(
                new ReferenceAttributePropertiesByAttributeId(
                    new ReferenceAttributeId(attribute)), user)))
        .build();
  }

}
