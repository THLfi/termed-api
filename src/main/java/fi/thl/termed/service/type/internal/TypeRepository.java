package fi.thl.termed.service.type.internal;

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
import java.util.stream.Collectors;

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
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.difference;
import static fi.thl.termed.util.collect.MapUtils.newLinkedHashMap;

public class TypeRepository extends AbstractRepository<TypeId, Type> {

  private Dao<TypeId, Type> typeDao;
  private Dao<ObjectRolePermission<TypeId>, GrantedPermission> typePermissionDao;
  private Dao<PropertyValueId<TypeId>, LangValue> typePropertyDao;

  private AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository;
  private AbstractRepository<ReferenceAttributeId, ReferenceAttribute>
      referenceAttributeRepository;

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

  /**
   * With bulk insert, first save all types, then dependant values.
   */
  @Override
  public void insert(Map<TypeId, Type> map, User user) {
    typeDao.insert(map, user);

    for (Map.Entry<TypeId, Type> entry : map.entrySet()) {
      TypeId id = entry.getKey();
      Type cls = entry.getValue();

      insertPermissions(id, cls.getPermissions(), user);
      insertProperties(id, cls.getProperties(), user);
      insertTextAttributes(id, cls.getTextAttributes(), user);
      insertReferenceAttributes(id, cls.getReferenceAttributes(), user);
    }
  }

  @Override
  public void insert(TypeId id, Type cls, User user) {
    typeDao.insert(id, cls, user);
    insertPermissions(id, cls.getPermissions(), user);
    insertProperties(id, cls.getProperties(), user);
    insertTextAttributes(id, cls.getTextAttributes(), user);
    insertReferenceAttributes(id, cls.getReferenceAttributes(), user);
  }

  private void insertPermissions(TypeId id, Multimap<String, Permission> permissions, User user) {
    typePermissionDao.insert(
        new RolePermissionsDtoToModel<>(id.getGraph(), id).apply(permissions), user);
  }

  private void insertProperties(TypeId id, Multimap<String, LangValue> properties, User user) {
    typePropertyDao.insert(new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  private void insertTextAttributes(TypeId id, List<TextAttribute> attrs, User user) {
    textAttributeRepository.insert(newLinkedHashMap(transform(
        addTextAttrIndices(attrs), new TextAttributeToIdEntry(id))), user);
  }

  private void insertReferenceAttributes(TypeId id, List<ReferenceAttribute> attrs, User user) {
    referenceAttributeRepository.insert(newLinkedHashMap(transform(
        addRefAttrIndices(attrs), new ReferenceAttributeToIdEntry(id))), user);
  }

  private List<TextAttribute> addTextAttrIndices(List<TextAttribute> textAttributes) {
    int i = 0;
    for (TextAttribute textAttribute : textAttributes) {
      textAttribute.setIndex(i++);
    }
    return textAttributes;
  }

  private List<ReferenceAttribute> addRefAttrIndices(List<ReferenceAttribute> refAttrs) {
    int i = 0;
    for (ReferenceAttribute referenceAttribute : refAttrs) {
      referenceAttribute.setIndex(i++);
    }
    return refAttrs;
  }

  @Override
  public void update(TypeId id, Type newType, Type oldType, User user) {
    typeDao.update(id, newType, user);

    updatePermissions(id, newType.getPermissions(), oldType.getPermissions(), user);
    updateProperties(id, newType.getProperties(), oldType.getProperties(), user);
    updateTextAttributes(id, addTextAttrIndices(newType.getTextAttributes()),
                         oldType.getTextAttributes(), user);
    updateReferenceAttributes(id, addRefAttrIndices(newType.getReferenceAttributes()),
                              oldType.getReferenceAttributes(), user);
  }

  private void updatePermissions(TypeId id,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {

    Map<ObjectRolePermission<TypeId>, GrantedPermission> newPermissionMap =
        new RolePermissionsDtoToModel<>(id.getGraph(), id).apply(newPermissions);
    Map<ObjectRolePermission<TypeId>, GrantedPermission> oldPermissionMap =
        new RolePermissionsDtoToModel<>(id.getGraph(), id).apply(oldPermissions);

    MapDifference<ObjectRolePermission<TypeId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    typePermissionDao.insert(diff.entriesOnlyOnLeft(), user);
    typePermissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(TypeId id,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<TypeId>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(id).apply(newPropertyMultimap);
    Map<PropertyValueId<TypeId>, LangValue> oldProperties =
        new PropertyValueDtoToModel<>(id).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<TypeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    typePropertyDao.insert(diff.entriesOnlyOnLeft(), user);
    typePropertyDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    typePropertyDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateTextAttributes(TypeId id,
                                    List<TextAttribute> newTextAttributes,
                                    List<TextAttribute> oldTextAttributes,
                                    User user) {

    Map<TextAttributeId, TextAttribute> newAttrs =
        newLinkedHashMap(transform(newTextAttributes, new TextAttributeToIdEntry(id)));
    Map<TextAttributeId, TextAttribute> oldAttrs =
        newLinkedHashMap(transform(oldTextAttributes, new TextAttributeToIdEntry(id)));

    MapDifference<TextAttributeId, TextAttribute> diff =
        difference(newAttrs, oldAttrs);

    textAttributeRepository.insert(diff.entriesOnlyOnLeft(), user);
    textAttributeRepository.update(diff.entriesDiffering(), user);
    textAttributeRepository.delete(diff.entriesOnlyOnRight(), user);
  }

  private void updateReferenceAttributes(TypeId id,
                                         List<ReferenceAttribute> newReferenceAttributes,
                                         List<ReferenceAttribute> oldReferenceAttributes,
                                         User user) {

    Map<ReferenceAttributeId, ReferenceAttribute> newAttrs =
        newLinkedHashMap(transform(newReferenceAttributes, new ReferenceAttributeToIdEntry(id)));
    Map<ReferenceAttributeId, ReferenceAttribute> oldAttrs =
        newLinkedHashMap(transform(oldReferenceAttributes, new ReferenceAttributeToIdEntry(id)));

    MapDifference<ReferenceAttributeId, ReferenceAttribute> diff =
        difference(newAttrs, oldAttrs);

    referenceAttributeRepository.insert(diff.entriesOnlyOnLeft(), user);
    referenceAttributeRepository.update(diff.entriesDiffering(), user);
    referenceAttributeRepository.delete(diff.entriesOnlyOnRight(), user);
  }

  @Override
  public void delete(TypeId id, User user) {
    delete(id, get(id, user).get(), user);
  }

  @Override
  public void delete(TypeId id, Type cls, User user) {
    deletePermissions(id, cls.getPermissions(), user);
    deleteProperties(id, cls.getProperties(), user);
    deleteTextAttributes(id, cls.getTextAttributes(), user);
    deleteReferenceAttributes(id, cls.getReferenceAttributes(), user);
    typeDao.delete(id, user);
  }

  private void deletePermissions(TypeId id, Multimap<String, Permission> permissions, User user) {
    typePermissionDao.delete(ImmutableList.copyOf(
        new RolePermissionsDtoToModel<>(id.getGraph(), id).apply(permissions).keySet()), user);
  }

  private void deleteProperties(TypeId id, Multimap<String, LangValue> properties, User user) {
    typePropertyDao.delete(ImmutableList.copyOf(
        new PropertyValueDtoToModel<>(id).apply(properties).keySet()), user);
  }

  private void deleteTextAttributes(TypeId id, List<TextAttribute> textAttributes, User user) {
    textAttributeRepository.delete(ImmutableMap.copyOf(
        Lists.transform(textAttributes, new TextAttributeToIdEntry(id))), user);
  }

  private void deleteReferenceAttributes(TypeId id, List<ReferenceAttribute> referenceAttributes,
                                         User user) {
    referenceAttributeRepository.delete(ImmutableMap.copyOf(
        Lists.transform(referenceAttributes, new ReferenceAttributeToIdEntry(id))), user);
  }

  @Override
  public boolean exists(TypeId id, User user) {
    return typeDao.exists(id, user);
  }

  @Override
  public List<Type> get(Query<TypeId, Type> specification, User user) {
    return typeDao.getValues(specification.getSpecification(), user).stream()
        .map(cls -> populateValue(cls, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<TypeId> getKeys(Query<TypeId, Type> specification, User user) {
    return typeDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<Type> get(TypeId id, User user) {
    return typeDao.get(id, user).map(cls -> populateValue(cls, user));
  }

  private Type populateValue(Type cls, User user) {
    cls = new Type(cls);

    cls.setPermissions(
        new RolePermissionsModelToDto<TypeId>().apply(
            typePermissionDao.getMap(new TypePermissionsByTypeId(
                new TypeId(cls.getId(), cls.getGraphId())), user)));

    cls.setProperties(
        new PropertyValueModelToDto<TypeId>().apply(
            typePropertyDao.getMap(new TypePropertiesByTypeId(
                new TypeId(cls.getId(), cls.getGraphId())), user)));

    cls.setTextAttributes(textAttributeRepository.get(
        new Query<>(
            new TextAttributesByTypeId(new TypeId(cls.getId(), cls.getGraphId()))), user));

    cls.setReferenceAttributes(referenceAttributeRepository.get(
        new Query<>(
            new ReferenceAttributesByTypeId(
                new TypeId(cls.getId(), cls.getGraphId()))), user));

    return cls;
  }

  /**
   * TextAttribute -> (TextAttributeId, TextAttribute)
   */
  private class TextAttributeToIdEntry implements Function<TextAttribute,
      Map.Entry<TextAttributeId, TextAttribute>> {

    private TypeId domainId;

    public TextAttributeToIdEntry(TypeId domainId) {
      this.domainId = domainId;
    }

    @Override
    public Map.Entry<TextAttributeId, TextAttribute> apply(TextAttribute input) {
      return MapUtils.simpleEntry(new TextAttributeId(domainId, input.getId()), input);
    }
  }

  /**
   * ReferenceAttribute -> (ReferenceAttributeId, ReferenceAttribute)
   */
  private class ReferenceAttributeToIdEntry implements Function<ReferenceAttribute,
      Map.Entry<ReferenceAttributeId, ReferenceAttribute>> {

    private TypeId domainId;

    public ReferenceAttributeToIdEntry(TypeId domainId) {
      this.domainId = domainId;
    }

    @Override
    public Map.Entry<ReferenceAttributeId, ReferenceAttribute> apply(ReferenceAttribute input) {
      return MapUtils.simpleEntry(new ReferenceAttributeId(domainId, input.getId()), input);
    }
  }

}
