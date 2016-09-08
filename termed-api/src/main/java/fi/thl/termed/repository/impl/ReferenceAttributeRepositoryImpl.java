package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
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
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
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

public class ReferenceAttributeRepositoryImpl
    extends AbstractRepository<ReferenceAttributeId, ReferenceAttribute> {

  private Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao;
  private Dao<ObjectRolePermission<ReferenceAttributeId>, Void> permissionDao;
  private Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyValueDao;

  private Function<ReferenceAttribute, ReferenceAttribute> populateAttribute;

  public ReferenceAttributeRepositoryImpl(
      Dao<ReferenceAttributeId, ReferenceAttribute> referenceAttributeDao,
      Dao<ObjectRolePermission<ReferenceAttributeId>, Void> permissionDao,
      Dao<PropertyValueId<ReferenceAttributeId>, LangValue> propertyValueDao) {
    this.referenceAttributeDao = referenceAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyValueDao = propertyValueDao;
    this.populateAttribute = FunctionUtils.pipe(new AddReferenceAttributePermissions(),
                                                new AddReferenceAttributeProperties());
  }

  @Override
  public void save(ReferenceAttribute referenceAttribute) {
    save(new ReferenceAttributeId(referenceAttribute), referenceAttribute);
  }

  @Override
  protected void insert(ReferenceAttributeId id, ReferenceAttribute referenceAttribute) {
    referenceAttributeDao.insert(id, referenceAttribute);

    insertPermissions(id, referenceAttribute.getPermissions());
    insertProperties(id, referenceAttribute.getProperties());
  }

  private void insertPermissions(ReferenceAttributeId attributeId,
                                 Multimap<String, Permission> permissions) {
    ClassId domainId = attributeId.getDomainId();
    permissionDao.insert(RolePermissionsDtoToModel.create(
        domainId.getSchemeId(), attributeId).apply(permissions));
  }

  private void insertProperties(ReferenceAttributeId attributeId,
                                Multimap<String, LangValue> properties) {
    propertyValueDao.insert(PropertyValueDtoToModel.create(attributeId).apply(properties));
  }

  @Override
  protected void update(ReferenceAttributeId id,
                        ReferenceAttribute newReferenceAttribute,
                        ReferenceAttribute oldReferenceAttribute) {

    referenceAttributeDao.update(id, newReferenceAttribute);
    updatePermissions(id, newReferenceAttribute.getPermissions(),
                      oldReferenceAttribute.getPermissions());
    updateProperties(id, newReferenceAttribute.getProperties(),
                     oldReferenceAttribute.getProperties());
  }

  private void updatePermissions(ReferenceAttributeId attrId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions) {
    ClassId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<ReferenceAttributeId>, Void> newPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<ReferenceAttributeId>, Void> oldPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<ReferenceAttributeId>, Void> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(diff.entriesOnlyOnLeft());
    permissionDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateProperties(ReferenceAttributeId attributeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<ReferenceAttributeId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<ReferenceAttributeId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<ReferenceAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft());
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  @Override
  protected void delete(ReferenceAttributeId id, ReferenceAttribute value) {
    delete(id);
  }

  @Override
  public void delete(ReferenceAttributeId id) {
    referenceAttributeDao.delete(id);
  }

  @Override
  public boolean exists(ReferenceAttributeId id) {
    return referenceAttributeDao.exists(id);
  }

  @Override
  public List<ReferenceAttribute> get() {
    return Lists.transform(referenceAttributeDao.getValues(), populateAttribute);
  }

  @Override
  public List<ReferenceAttribute> get(
      SpecificationQuery<ReferenceAttributeId, ReferenceAttribute> specification) {
    return Lists.transform(referenceAttributeDao.getValues(specification.getSpecification()),
                           populateAttribute);
  }

  @Override
  public ReferenceAttribute get(ReferenceAttributeId id) {
    return populateAttribute.apply(referenceAttributeDao.get(id));
  }

  /**
   * Load and add permissions to a reference attribute.
   */
  private class AddReferenceAttributePermissions
      implements Function<ReferenceAttribute, ReferenceAttribute> {

    @Override
    public ReferenceAttribute apply(ReferenceAttribute attribute) {
      attribute.setPermissions(RolePermissionsModelToDto.<ReferenceAttributeId>create().apply(
          permissionDao.getMap(new ReferenceAttributePermissionsByReferenceAttributeId(
              new ReferenceAttributeId(attribute)))));
      return attribute;
    }
  }

  /**
   * Load and add properties to a reference attribute.
   */
  private class AddReferenceAttributeProperties
      implements Function<ReferenceAttribute, ReferenceAttribute> {

    @Override
    public ReferenceAttribute apply(ReferenceAttribute referenceAttribute) {
      referenceAttribute.setProperties(
          PropertyValueModelToDto.<ReferenceAttributeId>create().apply(propertyValueDao.getMap(
              new ReferenceAttributePropertiesByAttributeId(
                  new ReferenceAttributeId(referenceAttribute)))));
      return referenceAttribute;
    }
  }

}
