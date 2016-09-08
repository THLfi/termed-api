package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
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

public class SchemeRepositoryImpl extends AbstractRepository<UUID, Scheme> {

  private Dao<UUID, Scheme> schemeDao;
  private Dao<SchemeRole, Void> schemeRoleDao;
  private Dao<ObjectRolePermission<UUID>, Void> schemePermissionDao;
  private Dao<PropertyValueId<UUID>, LangValue> schemePropertyValueDao;

  private AbstractRepository<ClassId, Class> classRepository;

  private Function<Scheme, Scheme> populateScheme;

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
    this.populateScheme = FunctionUtils.pipe(
        new AddSchemeRoles(),
        new AddSchemePermissions(),
        new AddSchemeProperties(),
        new AddSchemeClasses());
  }

  @Override
  public void save(Scheme scheme) {
    save(scheme.getId(), scheme);
  }

  @Override
  protected void insert(UUID id, Scheme scheme) {
    schemeDao.insert(id, scheme);
    insertRoles(id, scheme.getRoles());
    insertPermissions(id, scheme.getPermissions());
    insertProperties(id, scheme.getProperties());
    insertClasses(id, scheme.getClasses());
  }

  private void insertRoles(UUID schemeId, List<String> roles) {
    schemeRoleDao.insert(SchemeRoleDtoToModel.create(schemeId).apply(roles));
  }

  private void insertPermissions(UUID schemeId, Multimap<String, Permission> permissions) {
    schemePermissionDao.insert(
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(permissions));
  }

  private void insertProperties(UUID schemeId, Multimap<String, LangValue> properties) {
    schemePropertyValueDao.insert(PropertyValueDtoToModel.create(schemeId).apply(properties));
  }

  private void insertClasses(UUID schemeId, List<Class> classes) {
    classRepository.insert(
        MapUtils.newLinkedHashMap(Lists.transform(
            addClassIndices(classes), new ClassToIdEntry(schemeId))));
  }

  private List<Class> addClassIndices(List<Class> classes) {
    int i = 0;
    for (Class cls : classes) {
      cls.setIndex(i++);
    }
    return classes;
  }

  @Override
  protected void update(UUID id, Scheme newScheme, Scheme oldScheme) {
    schemeDao.update(id, newScheme);
    updateRoles(id, newScheme.getRoles(), oldScheme.getRoles());
    updatePermissions(id, newScheme.getPermissions(), oldScheme.getPermissions());
    updateProperties(id, newScheme.getProperties(), oldScheme.getProperties());
    updateClasses(id, addClassIndices(newScheme.getClasses()), oldScheme.getClasses());
  }

  private void updateRoles(UUID schemeId, List<String> newRoles, List<String> oldRoles) {
    Map<SchemeRole, Void> newRolesMap = SchemeRoleDtoToModel.create(schemeId).apply(newRoles);
    Map<SchemeRole, Void> oldRolesMap = SchemeRoleDtoToModel.create(schemeId).apply(oldRoles);

    MapDifference<SchemeRole, Void> diff = Maps.difference(newRolesMap, oldRolesMap);

    schemeRoleDao.insert(diff.entriesOnlyOnLeft());
    schemeRoleDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updatePermissions(UUID schemeId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions) {

    Map<ObjectRolePermission<UUID>, Void> newPermissionMap =
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(newPermissions);
    Map<ObjectRolePermission<UUID>, Void> oldPermissionMap =
        RolePermissionsDtoToModel.create(schemeId, schemeId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<UUID>, Void> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    schemePermissionDao.insert(diff.entriesOnlyOnLeft());
    schemePermissionDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateProperties(UUID schemeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<UUID>, LangValue> newProperties =
        PropertyValueDtoToModel.create(schemeId).apply(newPropertyMultimap);
    Map<PropertyValueId<UUID>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(schemeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<UUID>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    schemePropertyValueDao.insert(diff.entriesOnlyOnLeft());
    schemePropertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    schemePropertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateClasses(UUID schemeId, List<Class> newClasses, List<Class> oldClasses) {
    Map<ClassId, Class> newMappedClasses =
        MapUtils.newLinkedHashMap(Lists.transform(newClasses, new ClassToIdEntry(schemeId)));
    Map<ClassId, Class> oldMappedClasses =
        MapUtils.newLinkedHashMap(Lists.transform(oldClasses, new ClassToIdEntry(schemeId)));

    MapDifference<ClassId, Class> diff = Maps.difference(newMappedClasses, oldMappedClasses);

    classRepository.insert(diff.entriesOnlyOnLeft());
    classRepository.update(diff.entriesDiffering());
    classRepository.delete(diff.entriesOnlyOnRight());
  }

  @Override
  protected void delete(UUID id, Scheme value) {
    delete(id);
  }

  @Override
  public void delete(UUID id) {
    schemeDao.delete(id);
  }

  @Override
  public boolean exists(UUID id) {
    return schemeDao.exists(id);
  }

  @Override
  public List<Scheme> get() {
    return Lists.transform(schemeDao.getValues(), new AddSchemeProperties());
  }

  @Override
  public List<Scheme> get(SpecificationQuery<UUID, Scheme> specification) {
    return Lists.transform(schemeDao.getValues(specification.getSpecification()), populateScheme);
  }

  @Override
  public Scheme get(UUID id) {
    return populateScheme.apply(schemeDao.get(id));
  }

  private class AddSchemeRoles implements Function<Scheme, Scheme> {

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setRoles(SchemeRoleModelToDto.create().apply(schemeRoleDao.getMap(
          new SchemeRolesBySchemeId(scheme.getId()))));
      return scheme;
    }
  }

  private class AddSchemePermissions implements Function<Scheme, Scheme> {

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setPermissions(RolePermissionsModelToDto.<UUID>create().apply(
          schemePermissionDao.getMap(new SchemePermissionsBySchemeId(scheme.getId()))));
      return scheme;
    }
  }

  private class AddSchemeProperties implements Function<Scheme, Scheme> {

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setProperties(
          PropertyValueModelToDto.<UUID>create().apply(schemePropertyValueDao.getMap(
              new SchemePropertiesBySchemeId(scheme.getId()))));
      return scheme;
    }
  }

  private class AddSchemeClasses implements Function<Scheme, Scheme> {

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setClasses(classRepository.get(
          new SpecificationQuery<ClassId, Class>(new ClassesBySchemeId(scheme.getId()))));
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
