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
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
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

public class TextAttributeRepository
    extends AbstractRepository<TextAttributeId, TextAttribute> {

  private Dao<TextAttributeId, TextAttribute> textAttributeDao;
  private Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> permissionDao;
  private Dao<PropertyValueId<TextAttributeId>, LangValue> propertyValueDao;

  public TextAttributeRepository(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> permissionDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> propertyValueDao) {
    this.textAttributeDao = textAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyValueDao = propertyValueDao;
  }

  @Override
  protected TextAttributeId extractKey(TextAttribute textAttribute) {
    return new TextAttributeId(textAttribute);
  }

  @Override
  public void insert(TextAttributeId id, TextAttribute textAttribute, User user) {
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
  public void update(TextAttributeId id,
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
  public void delete(TextAttributeId id, TextAttribute textAttribute, User user) {
    deletePermissions(id, textAttribute.getPermissions(), user);
    deleteProperties(id, textAttribute.getProperties(), user);
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
  protected boolean exists(TextAttributeId id, User user) {
    return textAttributeDao.exists(id, user);
  }

  @Override
  public List<TextAttribute> get(Query<TextAttributeId, TextAttribute> specification,
                                 User user) {
    return textAttributeDao.getValues(specification.getSpecification(), user).stream()
        .map(attribute -> populateValue(attribute, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<TextAttributeId> getKeys(
      Query<TextAttributeId, TextAttribute> specification, User user) {
    return textAttributeDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<TextAttribute> get(TextAttributeId id, User user) {
    return textAttributeDao.get(id, user).map(attribute -> populateValue(attribute, user));
  }

  private TextAttribute populateValue(TextAttribute attribute, User user) {
    attribute = new TextAttribute(attribute);

    attribute.setPermissions(RolePermissionsModelToDto.<TextAttributeId>create().apply(
        permissionDao.getMap(new TextAttributePermissionsByTextAttributeId(
            new TextAttributeId(attribute)), user)));

    attribute.setProperties(
        PropertyValueModelToDto.<TextAttributeId>create()
            .apply(propertyValueDao.getMap(new TextAttributePropertiesByAttributeId(
                new TextAttributeId(attribute)), user)));

    return attribute;
  }

}
