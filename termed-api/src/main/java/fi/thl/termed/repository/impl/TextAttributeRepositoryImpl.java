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
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.RolePermissionsDtoToModel;
import fi.thl.termed.repository.transform.RolePermissionsModelToDto;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.TextAttributePermissionsByTextAttributeId;
import fi.thl.termed.spesification.sql.TextAttributePropertiesByAttributeId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

public class TextAttributeRepositoryImpl
    extends AbstractRepository<TextAttributeId, TextAttribute> {

  private Dao<TextAttributeId, TextAttribute> textAttributeDao;
  private Dao<ObjectRolePermission<TextAttributeId>, Void> permissionDao;
  private Dao<PropertyValueId<TextAttributeId>, LangValue> propertyValueDao;

  private Function<TextAttribute, TextAttribute> populateAttribute;

  public TextAttributeRepositoryImpl(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ObjectRolePermission<TextAttributeId>, Void> permissionDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> propertyValueDao) {
    this.textAttributeDao = textAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyValueDao = propertyValueDao;
    this.populateAttribute = FunctionUtils.pipe(new AddTextAttributePermissions(),
                                                new AddTextAttributeProperties());
  }

  private TextAttributeId getTextAttributeId(TextAttribute textAttribute) {
    return new TextAttributeId(new ClassId(textAttribute.getDomainSchemeId(),
                                           textAttribute.getDomainId()),
                               textAttribute.getId());
  }

  @Override
  public void save(TextAttribute textAttribute) {
    save(getTextAttributeId(textAttribute), textAttribute);
  }

  @Override
  protected void insert(TextAttributeId id, TextAttribute textAttribute) {
    textAttributeDao.insert(id, textAttribute);

    insertProperties(id, textAttribute.getProperties());
    insertPermissions(id, textAttribute.getPermissions());
  }

  private void insertProperties(TextAttributeId attrId, Multimap<String, LangValue> properties) {
    propertyValueDao.insert(PropertyValueDtoToModel.create(attrId).apply(properties));
  }

  private void insertPermissions(TextAttributeId attributeId,
                                 Multimap<String, Permission> permissions) {
    ClassId domainId = attributeId.getDomainId();
    permissionDao.insert(
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attributeId).apply(permissions));
  }

  @Override
  protected void update(TextAttributeId id,
                        TextAttribute newTextAttribute,
                        TextAttribute oldTextAttribute) {

    textAttributeDao.update(id, newTextAttribute);

    updatePermissions(id, newTextAttribute.getPermissions(), oldTextAttribute.getPermissions());
    updateProperties(id, newTextAttribute.getProperties(), oldTextAttribute.getProperties());
  }

  private void updatePermissions(TextAttributeId attrId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions) {
    ClassId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<TextAttributeId>, Void> newPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<TextAttributeId>, Void> oldPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<TextAttributeId>, Void> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(diff.entriesOnlyOnLeft());
    permissionDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateProperties(TextAttributeId attributeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<TextAttributeId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<TextAttributeId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<TextAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft());
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  @Override
  protected void delete(TextAttributeId id, TextAttribute value) {
    delete(id);
  }

  @Override
  public void delete(TextAttributeId id) {
    textAttributeDao.delete(id);
  }

  @Override
  public boolean exists(TextAttributeId id) {
    return textAttributeDao.exists(id);
  }

  @Override
  public List<TextAttribute> get() {
    return Lists.transform(textAttributeDao.getValues(), populateAttribute);
  }

  @Override
  public List<TextAttribute> get(SpecificationQuery<TextAttributeId, TextAttribute> specification) {
    return Lists.transform(textAttributeDao.getValues(specification.getSpecification()),
                           populateAttribute);
  }

  @Override
  public TextAttribute get(TextAttributeId id) {
    return populateAttribute.apply(textAttributeDao.get(id));
  }

  /**
   * Load and add permissions to a text attribute.
   */
  private class AddTextAttributePermissions implements Function<TextAttribute, TextAttribute> {

    @Override
    public TextAttribute apply(TextAttribute textAttribute) {
      textAttribute.setPermissions(RolePermissionsModelToDto.<TextAttributeId>create().apply(
          permissionDao.getMap(new TextAttributePermissionsByTextAttributeId(
              new TextAttributeId(textAttribute)))));
      return textAttribute;
    }
  }

  /**
   * Load and add properties to a text attribute.
   */
  private class AddTextAttributeProperties implements Function<TextAttribute, TextAttribute> {

    @Override
    public TextAttribute apply(TextAttribute textAttribute) {
      textAttribute.setProperties(
          PropertyValueModelToDto.<TextAttributeId>create()
              .apply(propertyValueDao.getMap(new TextAttributePropertiesByAttributeId(
                  getTextAttributeId(textAttribute)))));
      return textAttribute;
    }
  }

}
