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
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
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

public class TextAttributeRepository extends AbstractRepository<TextAttributeId, TextAttribute> {

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
  public void insert(TextAttributeId id, TextAttribute attr, WriteOptions opts, User user) {
    textAttributeDao.insert(id, attr, user);
    insertProperties(id, attr.getProperties(), user);
    insertPermissions(id, attr.getPermissions(), user);
  }

  private void insertProperties(TextAttributeId id, Multimap<String, LangValue> properties,
      User user) {
    propertyDao.insert(
        new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  private void insertPermissions(TextAttributeId id, Multimap<String, Permission> permissions,
      User user) {
    TypeId domainId = id.getDomainId();
    permissionDao.insert(
        new RolePermissionsDtoToModel<>(domainId.getGraph(), id).apply(permissions), user);
  }

  @Override
  public void update(TextAttributeId id, TextAttribute attr, WriteOptions opts, User user) {
    textAttributeDao.update(id, attr, user);
    updatePermissions(id, attr.getPermissions(), user);
    updateProperties(id, attr.getProperties(), user);
  }

  private void updatePermissions(TextAttributeId attrId, Multimap<String, Permission> permissions,
      User user) {

    TypeId domainId = attrId.getDomainId();

    Map<ObjectRolePermission<TextAttributeId>, GrantedPermission> newPermissionMap =
        tuplesToMap(
            new RolePermissionsDtoToModel<>(domainId.getGraph(), attrId).apply(permissions));
    Map<ObjectRolePermission<TextAttributeId>, GrantedPermission> oldPermissionMap =
        tuplesToMap(
            permissionDao.getEntries(new TextAttributePermissionsByTextAttributeId(attrId), user));

    MapDifference<ObjectRolePermission<TextAttributeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    permissionDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    permissionDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  private void updateProperties(TextAttributeId attributeId, Multimap<String, LangValue> properties,
      User user) {

    Map<PropertyValueId<TextAttributeId>, LangValue> newProperties =
        tuplesToMap(
            new PropertyValueDtoToModel<>(attributeId).apply(properties));
    Map<PropertyValueId<TextAttributeId>, LangValue> oldProperties =
        tuplesToMap(
            propertyDao.getEntries(new TextAttributePropertiesByAttributeId(attributeId), user));

    MapDifference<PropertyValueId<TextAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    propertyDao.update(entriesAsTuples(leftValues(diff.entriesDiffering())), user);
    propertyDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
  }

  @Override
  public void delete(TextAttributeId id, WriteOptions opts, User user) {
    deletePermissions(id, user);
    deleteProperties(id, user);
    textAttributeDao.delete(id, user);
  }

  private void deletePermissions(TextAttributeId id, User user) {
    permissionDao.delete(permissionDao.getKeys(
        new TextAttributePermissionsByTextAttributeId(id), user), user);
  }

  private void deleteProperties(TextAttributeId id, User user) {
    propertyDao.delete(propertyDao.getKeys(
        new TextAttributePropertiesByAttributeId(id), user), user);
  }

  @Override
  public boolean exists(TextAttributeId id, User user) {
    return textAttributeDao.exists(id, user);
  }

  @Override
  public Stream<TextAttribute> values(Query<TextAttributeId, TextAttribute> query, User user) {
    return textAttributeDao.getValues(query.getWhere(), user)
        .map(attribute -> populateValue(attribute, user));
  }

  @Override
  public Stream<TextAttributeId> keys(Query<TextAttributeId, TextAttribute> spec, User user) {
    return textAttributeDao.getKeys(spec.getWhere(), user);
  }

  @Override
  public Optional<TextAttribute> get(TextAttributeId id, User user, Select... selects) {
    return textAttributeDao.get(id, user).map(attribute -> populateValue(attribute, user));
  }

  private TextAttribute populateValue(TextAttribute attribute, User user) {
    TextAttributeId id = attribute.identifier();

    try (
        Stream<ObjectRolePermission<TextAttributeId>> permissionStream = permissionDao
            .getKeys(new TextAttributePermissionsByTextAttributeId(id), user);
        Stream<Tuple2<PropertyValueId<TextAttributeId>, LangValue>> propertyStream = propertyDao
            .getEntries(new TextAttributePropertiesByAttributeId(id), user)) {

      return TextAttribute.builderFromCopyOf(attribute)
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
