package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.RolePermissionsDtoToModel;
import fi.thl.termed.repository.transform.RolePermissionsModelToDto;
import fi.thl.termed.repository.transform.SchemeRoleDtoToModel;
import fi.thl.termed.repository.transform.SchemeRoleModelToDto;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.ClassesBySchemeId;
import fi.thl.termed.spesification.sql.SchemePermissionsBySchemeId;
import fi.thl.termed.spesification.sql.SchemePropertiesBySchemeId;
import fi.thl.termed.spesification.sql.SchemeRolesBySchemeId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

import static com.google.common.collect.ImmutableList.copyOf;

public class SchemeRepositoryImpl extends AbstractRepository<UUID, Scheme> {

  private Dao<UUID, Scheme> schemeDao;
  private Dao<SchemeRole, Void> schemeRoleDao;
  private Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao;
  private Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao;
  private AbstractRepository<ClassId, Class> classRepository;

  public SchemeRepositoryImpl(Dao<UUID, Scheme> schemeDao,
                              Dao<SchemeRole, Void> schemeRoleDao,
                              Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao,
                              Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao,
                              AbstractRepository<ClassId, Class> classRepository) {
    this.schemeDao = schemeDao;
    this.schemeRoleDao = schemeRoleDao;
    this.schemePermissionDao = schemePermissionDao;
    this.schemePropertyValueDao = schemePropertyValueDao;
    this.classRepository = classRepository;
  }

  @Override
  public void save(Scheme scheme, User user) {
    save(scheme.getId(), scheme, user);
  }

  @Override
  protected void insert(UUID id, Scheme scheme, User user) {
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
  protected void update(UUID id, Scheme newScheme, Scheme oldScheme, User user) {
    schemeDao.update(id, newScheme, user);
    updateRoles(id, newScheme.getRoles(), oldScheme.getRoles(), user);
    updatePermissions(id, newScheme.getPermissions(), oldScheme.getPermissions(), user);
    updateProperties(id, newScheme.getProperties(), oldScheme.getProperties(), user);
    updateClasses(id, addClassIndices(newScheme.getClasses()), oldScheme.getClasses(), user);
  }

  private void updateRoles(UUID schemeId, List<String> newRoles, List<String> oldRoles, User user) {
    Map<SchemeRole, Void> newRolesMap = SchemeRoleDtoToModel.create(schemeId).apply(newRoles);
    Map<SchemeRole, Void> oldRolesMap = SchemeRoleDtoToModel.create(schemeId).apply(oldRoles);

    MapDifference<SchemeRole, Void> diff = Maps.difference(newRolesMap, oldRolesMap);

    schemeRoleDao.insert(diff.entriesOnlyOnLeft(), user);
    schemeRoleDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updatePermissions(UUID schemeId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {

    Map<ObjectRolePermission<UUID>, Void> newPermissionMap =
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(newPermissions);
    Map<ObjectRolePermission<UUID>, Void> oldPermissionMap =
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<UUID>, Void> diff =
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
  public void delete(UUID id, User user) {
    delete(id, get(id, user), user);
  }

  @Override
  protected void delete(UUID id, Scheme scheme, User user) {
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
  public List<Scheme> get(SpecificationQuery<UUID, Scheme> specification, User user) {
    return Lists.transform(schemeDao.getValues(specification.getSpecification(), user),
                           populateSchemeFunction(user));
  }

  @Override
  public Scheme get(UUID id, User user) {
    return populateSchemeFunction(user).apply(schemeDao.get(id, user));
  }

  private Function<Scheme, Scheme> populateSchemeFunction(User user) {
    return FunctionUtils.pipe(
        new CreateCopy(),
        new AddSchemeRoles(user),
        new AddSchemePermissions(user),
        new AddSchemeProperties(user),
        new AddSchemeClasses(user));
  }

  private class CreateCopy implements Function<Scheme, Scheme> {

    public Scheme apply(Scheme scheme) {
      return new Scheme(scheme);
    }

  }

  private class AddSchemeRoles implements Function<Scheme, Scheme> {

    private User user;

    public AddSchemeRoles(User user) {
      this.user = user;
    }

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setRoles(SchemeRoleModelToDto.create().apply(schemeRoleDao.getMap(
          new SchemeRolesBySchemeId(scheme.getId()), user)));
      return scheme;
    }
  }

  private class AddSchemePermissions implements Function<Scheme, Scheme> {

    private User user;

    public AddSchemePermissions(User user) {
      this.user = user;
    }

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setPermissions(RolePermissionsModelToDto.<UUID>create().apply(
          schemePermissionDao.getMap(new SchemePermissionsBySchemeId(scheme.getId()), user)));
      return scheme;
    }
  }

  private class AddSchemeProperties implements Function<Scheme, Scheme> {

    private User user;

    public AddSchemeProperties(User user) {
      this.user = user;
    }

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setProperties(
          PropertyValueModelToDto.<UUID>create().apply(schemePropertyValueDao.getMap(
              new SchemePropertiesBySchemeId(scheme.getId()), user)));
      return scheme;
    }
  }

  private class AddSchemeClasses implements Function<Scheme, Scheme> {

    private User user;

    public AddSchemeClasses(User user) {
      this.user = user;
    }

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setClasses(classRepository.get(
          new SpecificationQuery<ClassId, Class>(new ClassesBySchemeId(scheme.getId())), user));
      return scheme;
    }
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
