package fi.thl.termed.service.scheme.internal;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsModelToDto;
import fi.thl.termed.domain.transform.SchemeRoleDtoToModel;
import fi.thl.termed.domain.transform.SchemeRoleModelToDto;
import fi.thl.termed.service.scheme.specification.ClassesBySchemeId;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;

public class SchemeRepository extends AbstractRepository<UUID, Scheme> {

  private Dao<UUID, Scheme> schemeDao;
  private Dao<SchemeRole, Empty> schemeRoleDao;
  private Dao<ObjectRolePermission<UUID>, GrantedPermission> schemePermissionDao;
  private Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao;
  private AbstractRepository<ClassId, Class> classRepository;

  public SchemeRepository(Dao<UUID, Scheme> schemeDao,
                          Dao<SchemeRole, Empty> schemeRoleDao,
                          Dao<ObjectRolePermission<UUID>, GrantedPermission> schemePermissionDao,
                          Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
                          AbstractRepository<ClassId, Class> classRepository) {
    this.schemeDao = schemeDao;
    this.schemeRoleDao = schemeRoleDao;
    this.schemePermissionDao = schemePermissionDao;
    this.schemePropertyValueDao = schemePropertyValueDao;
    this.classRepository = classRepository;
  }

  @Override
  protected UUID extractKey(Scheme scheme) {
    return scheme.getId();
  }

  @Override
  public void insert(UUID id, Scheme scheme, User user) {
    schemeDao.insert(id, scheme, user);
    insertRoles(id, scheme.getRoles(), user);
    insertPermissions(id, scheme.getPermissions(), user);
    insertProperties(id, scheme.getProperties(), user);
    insertClasses(id, scheme.getClasses(), user);
  }

  private void insertRoles(UUID schemeId, List<String> roles, User user) {
    schemeRoleDao.insert(SchemeRoleDtoToModel.create(schemeId).apply(roles), user);
  }

  private void insertPermissions(UUID schemeId, Multimap<String, Permission> permissions,
                                 User user) {
    schemePermissionDao.insert(
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(permissions), user);
  }

  private void insertProperties(UUID schemeId, Multimap<String, LangValue> properties, User user) {
    schemePropertyValueDao.insert(PropertyValueDtoToModel.create(schemeId).apply(properties), user);
  }

  private void insertClasses(UUID schemeId, List<Class> classes, User user) {
    classRepository.insert(
        MapUtils.newLinkedHashMap(Lists.transform(
            addClassIndices(classes), new ClassToIdEntry(schemeId))), user);
  }

  private List<Class> addClassIndices(List<Class> classes) {
    int i = 0;
    for (Class cls : classes) {
      cls.setIndex(i++);
    }
    return classes;
  }

  @Override
  public void update(UUID id, Scheme newScheme, Scheme oldScheme, User user) {
    schemeDao.update(id, newScheme, user);
    updateRoles(id, newScheme.getRoles(), oldScheme.getRoles(), user);
    updatePermissions(id, newScheme.getPermissions(), oldScheme.getPermissions(), user);
    updateProperties(id, newScheme.getProperties(), oldScheme.getProperties(), user);
    updateClasses(id, addClassIndices(newScheme.getClasses()), oldScheme.getClasses(), user);
  }

  private void updateRoles(UUID schemeId, List<String> newRoles, List<String> oldRoles, User user) {
    Map<SchemeRole, Empty> newRolesMap = SchemeRoleDtoToModel.create(schemeId).apply(newRoles);
    Map<SchemeRole, Empty> oldRolesMap = SchemeRoleDtoToModel.create(schemeId).apply(oldRoles);

    MapDifference<SchemeRole, Empty> diff = Maps.difference(newRolesMap, oldRolesMap);

    schemeRoleDao.insert(diff.entriesOnlyOnLeft(), user);
    schemeRoleDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updatePermissions(UUID schemeId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {

    Map<ObjectRolePermission<UUID>, GrantedPermission> newPermissionMap =
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(newPermissions);
    Map<ObjectRolePermission<UUID>, GrantedPermission> oldPermissionMap =
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<UUID>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    schemePermissionDao.insert(diff.entriesOnlyOnLeft(), user);
    schemePermissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(UUID schemeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<UUID>, LangValue> newProperties =
        PropertyValueDtoToModel.create(schemeId).apply(newPropertyMultimap);
    Map<PropertyValueId<UUID>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(schemeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<UUID>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    schemePropertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    schemePropertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    schemePropertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateClasses(UUID schemeId, List<Class> newClasses, List<Class> oldClasses,
                             User user) {
    Map<ClassId, Class> newMappedClasses =
        MapUtils.newLinkedHashMap(Lists.transform(newClasses, new ClassToIdEntry(schemeId)));
    Map<ClassId, Class> oldMappedClasses =
        MapUtils.newLinkedHashMap(Lists.transform(oldClasses, new ClassToIdEntry(schemeId)));

    MapDifference<ClassId, Class> diff = Maps.difference(newMappedClasses, oldMappedClasses);

    classRepository.insert(diff.entriesOnlyOnLeft(), user);
    classRepository.update(diff.entriesDiffering(), user);
    classRepository.delete(diff.entriesOnlyOnRight(), user);
  }

  @Override
  public void delete(UUID id, Scheme scheme, User user) {
    deleteRoles(id, scheme.getRoles(), user);
    deletePermissions(id, scheme.getPermissions(), user);
    deleteProperties(id, scheme.getProperties(), user);
    deleteClasses(id, scheme.getClasses(), user);
    schemeDao.delete(id, user);
  }

  private void deleteRoles(UUID id, List<String> roles, User user) {
    schemeRoleDao.delete(ImmutableList.copyOf(
        SchemeRoleDtoToModel.create(id).apply(roles).keySet()), user);
  }

  private void deletePermissions(UUID id, Multimap<String, Permission> permissions, User user) {
    schemePermissionDao.delete(ImmutableList.copyOf(
        RolePermissionsDtoToModel.create(id, id).apply(permissions).keySet()), user);
  }

  private void deleteProperties(UUID id, Multimap<String, LangValue> properties, User user) {
    schemePropertyValueDao.delete(ImmutableList.copyOf(
        PropertyValueDtoToModel.create(id).apply(properties).keySet()), user);
  }

  private void deleteClasses(UUID id, List<Class> classes, User user) {
    classRepository.delete(ImmutableMap.copyOf(
        Lists.transform(classes, new ClassToIdEntry(id))), user);
  }

  @Override
  public boolean exists(UUID id, User user) {
    return schemeDao.exists(id, user);
  }

  @Override
  public List<Scheme> get(Query<UUID, Scheme> specification, User user) {
    return schemeDao.getValues(specification.getSpecification(), user).stream()
        .map(scheme -> populateValue(scheme, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<UUID> getKeys(Query<UUID, Scheme> specification, User user) {
    return schemeDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<Scheme> get(UUID id, User user) {
    return schemeDao.get(id, user).map(scheme -> populateValue(scheme, user));
  }

  private Scheme populateValue(Scheme scheme, User user) {
    scheme = new Scheme(scheme);

    scheme.setRoles(SchemeRoleModelToDto.create().apply(schemeRoleDao.getMap(
        new SchemeRolesBySchemeId(scheme.getId()), user)));

    scheme.setPermissions(RolePermissionsModelToDto.<UUID>create().apply(
        schemePermissionDao.getMap(new SchemePermissionsBySchemeId(scheme.getId()), user)));

    scheme.setProperties(
        PropertyValueModelToDto.<UUID>create().apply(schemePropertyValueDao.getMap(
            new SchemePropertiesBySchemeId(scheme.getId()), user)));

    scheme.setClasses(classRepository.get(
        new Query<>(new ClassesBySchemeId(scheme.getId())), user));

    return scheme;
  }

  /**
   * Transform a Class into Map.Entry of ClassId and Class.
   */
  private class ClassToIdEntry implements Function<Class, Map.Entry<ClassId, Class>> {

    private UUID schemeId;

    public ClassToIdEntry(UUID schemeId) {
      this.schemeId = schemeId;
    }

    @Override
    public Map.Entry<ClassId, Class> apply(Class input) {
      return MapUtils.simpleEntry(new ClassId(schemeId, input.getId()), input);
    }
  }

}
