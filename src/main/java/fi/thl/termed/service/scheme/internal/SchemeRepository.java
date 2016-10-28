package fi.thl.termed.service.scheme.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsModelToDto;
import fi.thl.termed.domain.transform.SchemeRoleDtoToModel;
import fi.thl.termed.domain.transform.SchemeRoleModelToDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;

public class SchemeRepository extends AbstractRepository<SchemeId, Scheme> {

  private Dao<SchemeId, Scheme> schemeDao;
  private Dao<SchemeRole, Empty> schemeRoleDao;
  private Dao<ObjectRolePermission<SchemeId>, GrantedPermission> schemePermissionDao;
  private Dao<PropertyValueId<SchemeId>, LangValue> schemePropertyValueDao;

  public SchemeRepository(Dao<SchemeId, Scheme> schemeDao,
                          Dao<SchemeRole, Empty> schemeRoleDao,
                          Dao<ObjectRolePermission<SchemeId>, GrantedPermission> schemePermissionDao,
                          Dao<PropertyValueId<SchemeId>, LangValue> schemePropertyValueDao) {
    this.schemeDao = schemeDao;
    this.schemeRoleDao = schemeRoleDao;
    this.schemePermissionDao = schemePermissionDao;
    this.schemePropertyValueDao = schemePropertyValueDao;
  }

  @Override
  public void insert(SchemeId id, Scheme scheme, User user) {
    schemeDao.insert(id, scheme, user);
    insertRoles(id, scheme.getRoles(), user);
    insertPermissions(id, scheme.getPermissions(), user);
    insertProperties(id, scheme.getProperties(), user);
  }

  private void insertRoles(SchemeId schemeId, List<String> roles, User user) {
    schemeRoleDao.insert(new SchemeRoleDtoToModel(schemeId).apply(roles), user);
  }

  private void insertPermissions(SchemeId schemeId, Multimap<String, Permission> permissions,
                                 User user) {
    schemePermissionDao.insert(
        new RolePermissionsDtoToModel<>(schemeId, schemeId).apply(permissions), user);
  }

  private void insertProperties(SchemeId schemeId, Multimap<String, LangValue> properties,
                                User user) {
    schemePropertyValueDao.insert(new PropertyValueDtoToModel<>(schemeId).apply(properties), user);
  }

  @Override
  public void update(SchemeId id, Scheme newScheme, Scheme oldScheme, User user) {
    schemeDao.update(id, newScheme, user);
    updateRoles(id, newScheme.getRoles(), oldScheme.getRoles(), user);
    updatePermissions(id, newScheme.getPermissions(), oldScheme.getPermissions(), user);
    updateProperties(id, newScheme.getProperties(), oldScheme.getProperties(), user);
  }

  private void updateRoles(SchemeId scheme, List<String> newRoles, List<String> oldRoles,
                           User user) {
    Map<SchemeRole, Empty> newRolesMap = new SchemeRoleDtoToModel(scheme).apply(newRoles);
    Map<SchemeRole, Empty> oldRolesMap = new SchemeRoleDtoToModel(scheme).apply(oldRoles);

    MapDifference<SchemeRole, Empty> diff = Maps.difference(newRolesMap, oldRolesMap);

    schemeRoleDao.insert(diff.entriesOnlyOnLeft(), user);
    schemeRoleDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updatePermissions(SchemeId schemeId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {

    Map<ObjectRolePermission<SchemeId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(schemeId, schemeId).apply(newPermissions);
    Map<ObjectRolePermission<SchemeId>, GrantedPermission> oldPermissionMap =
        new RolePermissionsDtoToModel<>(schemeId, schemeId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<SchemeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    schemePermissionDao.insert(diff.entriesOnlyOnLeft(), user);
    schemePermissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(SchemeId schemeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<SchemeId>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(schemeId).apply(newPropertyMultimap);
    Map<PropertyValueId<SchemeId>, LangValue> oldProperties =
        new PropertyValueDtoToModel<>(schemeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<SchemeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    schemePropertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    schemePropertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    schemePropertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(SchemeId id, Scheme scheme, User user) {
    deleteRoles(id, scheme.getRoles(), user);
    deletePermissions(id, scheme.getPermissions(), user);
    deleteProperties(id, scheme.getProperties(), user);
    schemeDao.delete(id, user);
  }

  private void deleteRoles(SchemeId id, List<String> roles, User user) {
    schemeRoleDao.delete(ImmutableList.copyOf(
        new SchemeRoleDtoToModel(id).apply(roles).keySet()), user);
  }

  private void deletePermissions(SchemeId id, Multimap<String, Permission> permissions, User user) {
    schemePermissionDao.delete(ImmutableList.copyOf(
        new RolePermissionsDtoToModel<>(id, id).apply(permissions).keySet()), user);
  }

  private void deleteProperties(SchemeId id, Multimap<String, LangValue> properties, User user) {
    schemePropertyValueDao.delete(ImmutableList.copyOf(
        new PropertyValueDtoToModel<>(id).apply(properties).keySet()), user);
  }

  @Override
  public boolean exists(SchemeId id, User user) {
    return schemeDao.exists(id, user);
  }

  @Override
  public List<Scheme> get(Query<SchemeId, Scheme> specification, User user) {
    return schemeDao.getValues(specification.getSpecification(), user).stream()
        .map(scheme -> populateValue(scheme, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<SchemeId> getKeys(Query<SchemeId, Scheme> specification, User user) {
    return schemeDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<Scheme> get(SchemeId id, User user) {
    return schemeDao.get(id, user).map(scheme -> populateValue(scheme, user));
  }

  private Scheme populateValue(Scheme scheme, User user) {
    scheme = new Scheme(scheme);

    scheme.setRoles(new SchemeRoleModelToDto().apply(schemeRoleDao.getMap(
        new SchemeRolesBySchemeId(scheme.getId()), user)));

    scheme.setPermissions(new RolePermissionsModelToDto<SchemeId>().apply(
        schemePermissionDao.getMap(new SchemePermissionsBySchemeId(new SchemeId(scheme)), user)));

    scheme.setProperties(
        new PropertyValueModelToDto<SchemeId>().apply(schemePropertyValueDao.getMap(
            new SchemePropertiesBySchemeId(new SchemeId(scheme)), user)));

    return scheme;
  }

}
