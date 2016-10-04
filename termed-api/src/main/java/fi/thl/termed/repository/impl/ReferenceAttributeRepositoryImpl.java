package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.RolePermissionsDtoToModel;
import fi.thl.termed.repository.transform.RolePermissionsModelToDto;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.ReferenceAttributePermissionsByReferenceAttributeId;
import fi.thl.termed.spesification.sql.ReferenceAttributePropertiesByAttributeId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

import static com.google.common.collect.ImmutableList.copyOf;

public class ReferenceAttributeRepositoryImpl
    extends AbstractRepository<ReferenceAttributeId, ReferenceAttribute> {

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;
  private Dao<ObjectRolePermission<ReferenceAttributeId>, Empty> permissionDao;
  private Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyValueDao;

  public ReferenceAttributeRepositoryImpl(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ObjectRolePermission<ReferenceAttributeId>, Empty> permissionDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyValueDao) {
    this.referenceAttributeDao = referenceAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyValueDao = propertyValueDao;
  }

  @Override
  public void save(ReferenceAttribute referenceAttribute, User user) {
    save(new ReferenceAttributeId(referenceAttribute), referenceAttribute, user);
  }

  @Override
  protected void insert(ReferenceAttributeId id, ReferenceAttribute referenceAttribute, User user) {
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
  protected void update(ReferenceAttributeId id,
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

    Map<ObjectRolePermission<ReferenceAttributeId>, Empty> newPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<ReferenceAttributeId>, Empty> oldPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<ReferenceAttributeId>, Empty> diff =
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
  public void delete(ReferenceAttributeId id, User user) {
    delete(id, get(id, user), user);
  }

  @Override
  protected void delete(ReferenceAttributeId id, ReferenceAttribute referenceAttribute, User user) {
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
  public boolean exists(ReferenceAttributeId id, User user) {
    return referenceAttributeDao.exists(id, user);
  }

  @Override
  public List<ReferenceAttribute> get(
      SpecificationQuery<ReferenceAttributeId, ReferenceAttribute> specification, User user) {
    return Lists.transform(referenceAttributeDao.getValues(specification.getSpecification(), user),
                           populateAttributeFunction(user));
  }

  @Override
  public ReferenceAttribute get(ReferenceAttributeId id, User user) {
    return populateAttributeFunction(user).apply(referenceAttributeDao.get(id, user));
  }

  private Function<ReferenceAttribute, ReferenceAttribute> populateAttributeFunction(User user) {
    return FunctionUtils.pipe(new CreateCopy(),
                              new AddReferenceAttributePermissions(user),
                              new AddReferenceAttributeProperties(user));
  }

  private class CreateCopy implements Function<ReferenceAttribute, ReferenceAttribute> {

    public ReferenceAttribute apply(ReferenceAttribute attribute) {
      return new ReferenceAttribute(attribute);
    }
  }

  /**
   * Load and add permissions to a reference attribute.
   */
  private class AddReferenceAttributePermissions
      implements Function<ReferenceAttribute, ReferenceAttribute> {

    private User user;

    public AddReferenceAttributePermissions(User user) {
      this.user = user;
    }

    @Override
    public ReferenceAttribute apply(ReferenceAttribute attribute) {
      attribute.setPermissions(RolePermissionsModelToDto.<ReferenceAttributeId>create().apply(
          permissionDao.getMap(new ReferenceAttributePermissionsByReferenceAttributeId(
              new ReferenceAttributeId(attribute)), user)));
      return attribute;
    }
  }

  /**
   * Load and add properties to a reference attribute.
   */
  private class AddReferenceAttributeProperties
      implements Function<ReferenceAttribute, ReferenceAttribute> {

    private User user;

    public AddReferenceAttributeProperties(User user) {
      this.user = user;
    }

    @Override
    public ReferenceAttribute apply(ReferenceAttribute referenceAttribute) {
      referenceAttribute.setProperties(
          PropertyValueModelToDto.<ReferenceAttributeId>create().apply(propertyValueDao.getMap(
              new ReferenceAttributePropertiesByAttributeId(
                  new ReferenceAttributeId(referenceAttribute)), user)));
      return referenceAttribute;
    }
  }

}
