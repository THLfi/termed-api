package fi.thl.termed.service.type.internal;

import static com.google.common.collect.ImmutableList.copyOf;

import com.google.common.collect.ImmutableList;
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
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Specification;
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
  public void insert(ReferenceAttributeId id, ReferenceAttribute referenceAttribute, User user) {
    referenceAttributeDao.insert(id, referenceAttribute, user);
    insertPermissions(id, referenceAttribute.getPermissions(), user);
    insertProperties(id, referenceAttribute.getProperties(), user);
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
  public void update(ReferenceAttributeId id,
      ReferenceAttribute newAttribute,
      ReferenceAttribute oldAttribute,
      User user) {
    referenceAttributeDao.update(id, newAttribute, user);
    updatePermissions(id, newAttribute.getPermissions(), oldAttribute.getPermissions(), user);
    updateProperties(id, newAttribute.getProperties(), oldAttribute.getProperties(), user);
  }

  private void updatePermissions(ReferenceAttributeId attrId,
      Multimap<String, Permission> newPermissions,
      Multimap<String, Permission> oldPermissions,
      User user) {
    TypeId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> oldPermissionMap =
        new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(diff.entriesOnlyOnLeft(), user);
    permissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(ReferenceAttributeId attributeId,
      Multimap<String, LangValue> newPropertyMultimap,
      Multimap<String, LangValue> oldPropertyMultimap,
      User user) {

    Map<PropertyValueId<ReferenceAttributeId>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<ReferenceAttributeId>, LangValue> oldProperties =
        new PropertyValueDtoToModel<>(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<ReferenceAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    propertyDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(ReferenceAttributeId id, ReferenceAttribute referenceAttribute, User user) {
    deletePermissions(id, referenceAttribute.getPermissions(), user);
    deleteProperties(id, referenceAttribute.getProperties(), user);
    referenceAttributeDao.delete(id, user);
  }

  private void deletePermissions(ReferenceAttributeId id, Multimap<String, Permission> permissions,
      User user) {
    TypeId domainId = id.getDomainId();
    permissionDao.delete(ImmutableList.copyOf(
        new RolePermissionsDtoToModel<>(domainId.getGraph(), id)
            .apply(permissions).keySet()), user);
  }

  private void deleteProperties(ReferenceAttributeId id, Multimap<String, LangValue> properties,
      User user) {
    propertyDao.delete(ImmutableList.copyOf(
        new PropertyValueDtoToModel<>(id).apply(properties).keySet()), user);
  }

  @Override
  public boolean exists(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.exists(id, user);
  }

  @Override
  public Stream<ReferenceAttribute> get(
      Specification<ReferenceAttributeId, ReferenceAttribute> specification, User user) {
    return referenceAttributeDao.getValues(specification, user).stream()
        .map(attribute -> populateValue(attribute, user));
  }

  @Override
  public Stream<ReferenceAttributeId> getKeys(
      Specification<ReferenceAttributeId, ReferenceAttribute> specification, User user) {
    return referenceAttributeDao.getKeys(specification, user).stream();
  }

  @Override
  public Optional<ReferenceAttribute> get(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.get(id, user).map(attribute -> populateValue(attribute, user));
  }

  private ReferenceAttribute populateValue(ReferenceAttribute attribute, User user) {
    attribute = new ReferenceAttribute(attribute);

    attribute.setPermissions(new RolePermissionsModelToDto<ReferenceAttributeId>().apply(
        permissionDao.getMap(new ReferenceAttributePermissionsByReferenceAttributeId(
            new ReferenceAttributeId(attribute)), user)));

    attribute.setProperties(
        new PropertyValueModelToDto<ReferenceAttributeId>().apply(propertyDao.getMap(
            new ReferenceAttributePropertiesByAttributeId(
                new ReferenceAttributeId(attribute)), user)));

    return attribute;
  }

}
