package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.RolePermissionsDtoToModel;
import fi.thl.termed.repository.transform.RolePermissionsModelToDto;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.ResourceTextAttributeValuesByAttributeId;
import fi.thl.termed.spesification.sql.TextAttributePermissionsByTextAttributeId;
import fi.thl.termed.spesification.sql.TextAttributePropertiesByAttributeId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;
import fi.thl.termed.util.StrictLangValue;

import static com.google.common.collect.ImmutableList.copyOf;

public class TextAttributeRepositoryImpl
    extends AbstractRepository<TextAttributeId, TextAttribute> {

  private Dao<TextAttributeId, TextAttribute> textAttributeDao;
  private Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> permissionDao;
  private Dao<PropertyValueId<TextAttributeId>, LangValue> propertyValueDao;
  private Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao;

  public TextAttributeRepositoryImpl(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> permissionDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> propertyValueDao,
      Dao<ResourceAttributeValueId, StrictLangValue> textAttributeValueDao) {
    this.textAttributeDao = textAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyValueDao = propertyValueDao;
    this.textAttributeValueDao = textAttributeValueDao;
  }

  @Override
  protected TextAttributeId extractKey(TextAttribute textAttribute) {
    return new TextAttributeId(textAttribute);
  }

  @Override
  protected void insert(TextAttributeId id, TextAttribute textAttribute, User user) {
    textAttributeDao.insert(id, textAttribute, user);
    insertProperties(id, textAttribute.getProperties(), user);
    insertPermissions(id, textAttribute.getPermissions(), user);
  }

  private void insertProperties(TextAttributeId id, Multimap<String, LangValue> properties,
                                User user) {
    propertyValueDao.insert(PropertyValueDtoToModel.create(id).apply(properties), user);
  }

  private void insertPermissions(TextAttributeId id, Multimap<String, Permission> permissions,
                                 User user) {
    ClassId domainId = id.getDomainId();
    permissionDao.insert(
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), id).apply(permissions), user);
  }

  @Override
  protected void update(TextAttributeId id,
                        TextAttribute newAttribute,
                        TextAttribute oldAttribute,
                        User user) {
    textAttributeDao.update(id, newAttribute, user);
    updatePermissions(id, newAttribute.getPermissions(), oldAttribute.getPermissions(), user);
    updateProperties(id, newAttribute.getProperties(), oldAttribute.getProperties(), user);
  }

  private void updatePermissions(TextAttributeId attrId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {
    ClassId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<TextAttributeId>, GrantedPermission> newPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<TextAttributeId>, GrantedPermission> oldPermissionMap =
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), attrId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<TextAttributeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(diff.entriesOnlyOnLeft(), user);
    permissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(TextAttributeId attributeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<TextAttributeId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<TextAttributeId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<TextAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    propertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(TextAttributeId id, User user) {
    delete(id, get(id, user).get(), user);
  }

  @Override
  protected void delete(TextAttributeId id, TextAttribute textAttribute, User user) {
    deletePermissions(id, textAttribute.getPermissions(), user);
    deleteProperties(id, textAttribute.getProperties(), user);
    textAttributeValueDao.delete(textAttributeValueDao.getKeys(
        new ResourceTextAttributeValuesByAttributeId(id), user), user);
    textAttributeDao.delete(id, user);
  }

  private void deletePermissions(TextAttributeId id, Multimap<String, Permission> permissions,
                                 User user) {
    ClassId domainId = id.getDomainId();
    permissionDao.delete(ImmutableList.copyOf(
        RolePermissionsDtoToModel.create(domainId.getSchemeId(), id)
            .apply(permissions).keySet()), user);
  }

  private void deleteProperties(TextAttributeId id, Multimap<String, LangValue> properties,
                                User user) {
    propertyValueDao.delete(ImmutableList.copyOf(
        PropertyValueDtoToModel.create(id).apply(properties).keySet()), user);
  }

  @Override
  public boolean exists(TextAttributeId id, User user) {
    return textAttributeDao.exists(id, user);
  }

  @Override
  public List<TextAttribute> get(SpecificationQuery<TextAttributeId, TextAttribute> specification,
                                 User user) {
    return Lists.transform(textAttributeDao.getValues(specification.getSpecification(), user),
                           populateAttributeFunction(user));
  }

  @Override
  public Optional<TextAttribute> get(TextAttributeId id, User user) {
    Optional<TextAttribute> o = textAttributeDao.get(id, user);
    return o.isPresent() ? Optional.of(populateAttributeFunction(user).apply(o.get()))
                         : Optional.<TextAttribute>absent();
  }

  private Function<TextAttribute, TextAttribute> populateAttributeFunction(User user) {
    return FunctionUtils.pipe(new CreateCopy(),
                              new AddTextAttributePermissions(user),
                              new AddTextAttributeProperties(user));
  }

  private class CreateCopy implements Function<TextAttribute, TextAttribute> {

    public TextAttribute apply(TextAttribute attribute) {
      return new TextAttribute(attribute);
    }
  }

  /**
   * Load and add permissions to a text attribute.
   */
  private class AddTextAttributePermissions implements Function<TextAttribute, TextAttribute> {

    private User user;

    public AddTextAttributePermissions(User user) {
      this.user = user;
    }

    @Override
    public TextAttribute apply(TextAttribute textAttribute) {
      textAttribute.setPermissions(RolePermissionsModelToDto.<TextAttributeId>create().apply(
          permissionDao.getMap(new TextAttributePermissionsByTextAttributeId(
              new TextAttributeId(textAttribute)), user)));
      return textAttribute;
    }
  }

  /**
   * Load and add properties to a text attribute.
   */
  private class AddTextAttributeProperties implements Function<TextAttribute, TextAttribute> {

    private User user;

    public AddTextAttributeProperties(User user) {
      this.user = user;
    }

    @Override
    public TextAttribute apply(TextAttribute textAttribute) {
      textAttribute.setProperties(
          PropertyValueModelToDto.<TextAttributeId>create()
              .apply(propertyValueDao.getMap(new TextAttributePropertiesByAttributeId(
                  new TextAttributeId(textAttribute)), user)));
      return textAttribute;
    }
  }

}
