package fi.thl.termed.service.type.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.thl.termed.domain.TypeId;
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
  private Dao<PropertyValueId<TextAttributeId>, LangValue> propertyDao;

  public TextAttributeRepository(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<ObjectRolePermission<TextAttributeId>, GrantedPermission> permissionDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> propertyDao) {
    this.textAttributeDao = textAttributeDao;
    this.permissionDao = permissionDao;
    this.propertyDao = propertyDao;
  }

  @Override
  public void insert(TextAttributeId id, TextAttribute textAttribute, User user) {
    textAttributeDao.insert(id, textAttribute, user);
    insertProperties(id, textAttribute.getProperties(), user);
    insertPermissions(id, textAttribute.getPermissions(), user);
  }

  private void insertProperties(TextAttributeId id, Multimap<String, LangValue> properties,
                                User user) {
    propertyDao.insert(new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  private void insertPermissions(TextAttributeId id, Multimap<String, Permission> permissions,
                                 User user) {
    TypeId domainId = id.getDomainId();
    permissionDao.insert(
        new RolePermissionsDtoToModel<>(domainId.getGraph(), id).apply(permissions), user);
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
    TypeId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<TextAttributeId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(newPermissions);
    Map<ObjectRolePermission<TextAttributeId>, GrantedPermission> oldPermissionMap =
        new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(oldPermissions);

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
        new PropertyValueDtoToModel<>(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<TextAttributeId>, LangValue> oldProperties =
        new PropertyValueDtoToModel<>(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<TextAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    propertyDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(TextAttributeId id, TextAttribute textAttribute, User user) {
    deletePermissions(id, textAttribute.getPermissions(), user);
    deleteProperties(id, textAttribute.getProperties(), user);
    textAttributeDao.delete(id, user);
  }

  private void deletePermissions(TextAttributeId id, Multimap<String, Permission> permissions,
                                 User user) {
    TypeId domainId = id.getDomainId();
    permissionDao.delete(ImmutableList.copyOf(
        new RolePermissionsDtoToModel<>(domainId.getGraph(), id)
            .apply(permissions).keySet()), user);
  }

  private void deleteProperties(TextAttributeId id, Multimap<String, LangValue> properties,
                                User user) {
    propertyDao.delete(ImmutableList.copyOf(
        new PropertyValueDtoToModel<>(id).apply(properties).keySet()), user);
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

    attribute.setPermissions(new RolePermissionsModelToDto<TextAttributeId>().apply(
        permissionDao.getMap(new TextAttributePermissionsByTextAttributeId(
            new TextAttributeId(attribute)), user)));

    attribute.setProperties(
        new PropertyValueModelToDto<TextAttributeId>()
            .apply(propertyDao.getMap(new TextAttributePropertiesByAttributeId(
                new TextAttributeId(attribute)), user)));

    return attribute;
  }

}
