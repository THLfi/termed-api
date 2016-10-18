package fi.thl.termed.service.scheme.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsModelToDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;

public class ReferenceAttributeRepository
    extends AbstractRepository<ReferenceAttributeId, ReferenceAttribute> {

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;
  private Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> permissionDao;
  private Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyValueDao;

  public ReferenceAttributeRepository(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> permissionDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyValueDao) {
    this.referenceAttributeDao = referenceAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyValueDao = propertyValueDao;
  }

  @Override
  protected ReferenceAttributeId extractKey(ReferenceAttribute referenceAttribute) {
    return new ReferenceAttributeId(referenceAttribute);
  }

  @Override
  public void insert(ReferenceAttributeId id, ReferenceAttribute referenceAttribute, User user) {
    referenceAttributeDao.insert(id, referenceAttribute, user);
    insertPermissions(id, referenceAttribute.getPermissions(), user);
    insertProperties(id, referenceAttribute.getProperties(), user);
  }

  private void insertPermissions(ReferenceAttributeId attributeId,
                                 Multimap<String, Permission> permissions, User user) {
    ClassId domainId = attributeId.getDomainId();
    permissionDao.insert(RolePermissionsDtoToModel.create(
        domainId.getSchemeId(), attributeId).apply(permissions), user);
  }

  private void insertProperties(ReferenceAttributeId attributeId,
                                Multimap<String, LangValue> properties, User user) {
    propertyValueDao.insert(PropertyValueDtoToModel.create(attributeId).apply(properties), user);
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
    ClassId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> newPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<ReferenceAttributeId>, GrantedPermission> oldPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(oldPermissions);

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
        PropertyValueDtoToModel.create(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<ReferenceAttributeId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<ReferenceAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    propertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(ReferenceAttributeId id, ReferenceAttribute referenceAttribute, User user) {
    deletePermissions(id, referenceAttribute.getPermissions(), user);
    deleteProperties(id, referenceAttribute.getProperties(), user);
    referenceAttributeDao.delete(id, user);
  }

  private void deletePermissions(ReferenceAttributeId id, Multimap<String, Permission> permissions,
                                 User user) {
    ClassId domainId = id.getDomainId();
    permissionDao.delete(ImmutableList.copyOf(
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), id)
            .apply(permissions).keySet()), user);
  }

  private void deleteProperties(ReferenceAttributeId id, Multimap<String, LangValue> properties,
                                User user) {
    propertyValueDao.delete(ImmutableList.copyOf(
        PropertyValueDtoToModel.create(id).apply(properties).keySet()), user);
  }

  @Override
  protected boolean exists(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.exists(id, user);
  }

  @Override
  public List<ReferenceAttribute> get(
      Query<ReferenceAttributeId, ReferenceAttribute> specification, User user) {
    return referenceAttributeDao.getValues(specification.getSpecification(), user).stream()
        .map(attribute -> populateValue(attribute, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<ReferenceAttributeId> getKeys(
      Query<ReferenceAttributeId, ReferenceAttribute> specification, User user) {
    return referenceAttributeDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<ReferenceAttribute> get(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.get(id, user).map(attribute -> populateValue(attribute, user));
  }

  private ReferenceAttribute populateValue(ReferenceAttribute attribute, User user) {
    attribute = new ReferenceAttribute(attribute);

    attribute.setPermissions(RolePermissionsModelToDto.<ReferenceAttributeId>create().apply(
        permissionDao.getMap(new ReferenceAttributePermissionsByReferenceAttributeId(
            new ReferenceAttributeId(attribute)), user)));

    attribute.setProperties(
        PropertyValueModelToDto.<ReferenceAttributeId>create().apply(propertyValueDao.getMap(
            new ReferenceAttributePropertiesByAttributeId(
                new ReferenceAttributeId(attribute)), user)));

    return attribute;
  }

}
