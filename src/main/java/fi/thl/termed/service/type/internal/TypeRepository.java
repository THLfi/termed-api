package fi.thl.termed.service.type.internal;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.domain.transform.RolePermissionsDtoToModel;
import fi.thl.termed.domain.transform.RolePermissionsModelToDto;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Specification;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TypeRepository extends AbstractRepository<TypeId, Type> {

  private Dao<TypeId, Type> typeDao;
  private Dao<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao;
  private Dao<PropertyValueId<TypeId>, LangValue> typePropertyDao;

  private AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository;
  private AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository;

  public TypeRepository(
      Dao<TypeId, Type> typeDao,
      Dao<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao,
      Dao<PropertyValueId<TypeId>, LangValue> typePropertyDao,
      AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository,
      AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository) {
    this.typeDao = typeDao;
    this.typePermissionDao = typePermissionDao;
    this.typePropertyDao = typePropertyDao;
    this.textAttributeRepository = textAttributeRepository;
    this.referenceAttributeRepository = referenceAttributeRepository;
  }

  @Override
  public List<TypeId> save(List<Type> types, Map<String, Object> args, User user) {
    return super.save(addTypeIndices(types), args, user);
  }

  private List<Type> addTypeIndices(List<Type> types) {
    int i = 0;
    for (Type type : types) {
      type.setIndex(i++);
    }
    return types;
  }

  /**
   * With bulk insert, first save all types, then dependant values.
   */
  @Override
  public void insert(Map<TypeId, Type> map, User user) {
    typeDao.insert(map, user);

    for (Map.Entry<TypeId, Type> entry : map.entrySet()) {
      TypeId id = entry.getKey();
      Type type = entry.getValue();

      insertPermissions(id, type.getPermissions(), user);
      insertProperties(id, type.getProperties(), user);

      saveTextAttributes(id, type.getTextAttributes(), user);
      saveReferenceAttributes(id, type.getReferenceAttributes(), user);
    }
  }

  private void saveTextAttributes(TypeId id, List<TextAttribute> textAttributes, User user) {
    Set<TextAttributeId> textAttributeIds =
        textAttributes.stream().map(TextAttribute::identifier).collect(toSet());

    List<TextAttributeId> deletedAttributeIds =
        textAttributeRepository.getKeys(new TextAttributesByTypeId(id), user)
            .filter(oldAttrId -> !textAttributeIds.contains(oldAttrId)).collect(toList());

    int i = 0;
    for (TextAttribute textAttribute : textAttributes) {
      textAttribute.setDomain(id);
      textAttribute.setIndex(i++);
    }

    textAttributeRepository.delete(deletedAttributeIds, user);
    textAttributeRepository.save(textAttributes, user);
  }

  private void saveReferenceAttributes(TypeId id, List<ReferenceAttribute> refAttrs, User user) {
    Set<ReferenceAttributeId> refAttributeIds =
        refAttrs.stream().map(ReferenceAttribute::identifier).collect(toSet());

    List<ReferenceAttributeId> deletedAttributeIds =
        referenceAttributeRepository.getKeys(new ReferenceAttributesByTypeId(id), user)
            .filter(oldAttrId -> !refAttributeIds.contains(oldAttrId)).collect(toList());

    int i = 0;
    for (ReferenceAttribute refAttribute : refAttrs) {
      refAttribute.setDomain(id);
      refAttribute.setIndex(i++);
    }

    referenceAttributeRepository.delete(deletedAttributeIds, user);
    referenceAttributeRepository.save(refAttrs, user);
  }

  @Override
  public void insert(TypeId id, Type type, User user) {
    typeDao.insert(id, type, user);

    insertPermissions(id, type.getPermissions(), user);
    insertProperties(id, type.getProperties(), user);

    saveTextAttributes(id, type.getTextAttributes(), user);
    saveReferenceAttributes(id, type.getReferenceAttributes(), user);

  }

  private void insertPermissions(TypeId id, Multimap<String, Permission> permissions, User user) {
    typePermissionDao.insert(
        new RolePermissionsDtoToModel<>(id.getGraph(), id).apply(permissions), user);
  }

  private void insertProperties(TypeId id, Multimap<String, LangValue> properties, User user) {
    typePropertyDao.insert(new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  @Override
  public void update(TypeId id, Type type, User user) {
    typeDao.update(id, type, user);

    updatePermissions(id, type.getPermissions(), user);
    updateProperties(id, type.getProperties(), user);

    saveTextAttributes(id, type.getTextAttributes(), user);
    saveReferenceAttributes(id, type.getReferenceAttributes(), user);
  }

  private void updatePermissions(TypeId id, Multimap<String, Permission> permissions, User user) {
    Map<ObjectRolePermission<TypeId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(id.getGraph(), id).apply(permissions);
    Map<ObjectRolePermission<TypeId>, GrantedPermission> oldPermissionMap =
        typePermissionDao.getMap(new TypePermissionsByTypeId(id), user);

    MapDifference<ObjectRolePermission<TypeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    typePermissionDao.insert(diff.entriesOnlyOnLeft(), user);
    typePermissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(TypeId id, Multimap<String, LangValue> properties, User user) {
    Map<PropertyValueId<TypeId>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(id).apply(properties);
    Map<PropertyValueId<TypeId>, LangValue> oldProperties =
        typePropertyDao.getMap(new TypePropertiesByTypeId(id), user);

    MapDifference<PropertyValueId<TypeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    typePropertyDao.insert(diff.entriesOnlyOnLeft(), user);
    typePropertyDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    typePropertyDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(TypeId id, Map<String, Object> args, User user) {
    deletePermissions(id, user);
    deleteProperties(id, user);
    deleteTextAttributes(id, user);
    deleteReferenceAttributes(id, user);
    typeDao.delete(id, user);
  }

  private void deletePermissions(TypeId id, User user) {
    typePermissionDao
        .delete(typePermissionDao.getKeys(new TypePermissionsByTypeId(id), user), user);
  }

  private void deleteProperties(TypeId id, User user) {
    typePropertyDao.delete(typePropertyDao.getKeys(new TypePropertiesByTypeId(id), user), user);
  }

  private void deleteTextAttributes(TypeId id, User user) {
    textAttributeRepository.delete(textAttributeRepository.getKeys(
        new TextAttributesByTypeId(id), user).collect(toList()), user);
  }

  private void deleteReferenceAttributes(TypeId id, User user) {
    referenceAttributeRepository.delete(referenceAttributeRepository.getKeys(
        new ReferenceAttributesByTypeId(id), user).collect(toList()), user);
  }

  @Override
  public boolean exists(TypeId id, User user) {
    return typeDao.exists(id, user);
  }

  @Override
  public Stream<Type> get(Specification<TypeId, Type> specification, User user) {
    return typeDao.getValues(specification, user).stream()
        .map(cls -> populateValue(cls, user));
  }

  @Override
  public Stream<TypeId> getKeys(Specification<TypeId, Type> specification, User user) {
    return typeDao.getKeys(specification, user).stream();
  }

  @Override
  public Optional<Type> get(TypeId id, User user) {
    return typeDao.get(id, user).map(cls -> populateValue(cls, user));
  }

  private Type populateValue(Type type, User user) {
    TypeId id = type.identifier();
    type = new Type(type);

    type.setPermissions(new RolePermissionsModelToDto<TypeId>().apply(
        typePermissionDao.getMap(new TypePermissionsByTypeId(id), user)));

    type.setProperties(new PropertyValueModelToDto<TypeId>().apply(
        typePropertyDao.getMap(new TypePropertiesByTypeId(id), user)));

    type.setTextAttributes(textAttributeRepository.get(
        new TextAttributesByTypeId(id), user).collect(toList()));

    type.setReferenceAttributes(referenceAttributeRepository.get(
        new ReferenceAttributesByTypeId(id), user).collect(toList()));

    return type;
  }

}
