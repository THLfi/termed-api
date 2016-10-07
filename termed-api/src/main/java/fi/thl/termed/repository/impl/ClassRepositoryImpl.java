package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.GrantedPermission;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.RolePermissionsDtoToModel;
import fi.thl.termed.repository.transform.RolePermissionsModelToDto;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.sql.ClassPermissionsByClassId;
import fi.thl.termed.spesification.sql.ClassPropertiesByClassId;
import fi.thl.termed.spesification.sql.ReferenceAttributesByClassId;
import fi.thl.termed.spesification.sql.TextAttributesByClassId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Maps.difference;
import static fi.thl.termed.util.MapUtils.newLinkedHashMap;

public class ClassRepositoryImpl extends AbstractRepository<ClassId, Class> {

  private Dao<ClassId, Class> classDao;
  private Dao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionDao;
  private Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao;

  private AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository;
  private AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository;

  public ClassRepositoryImpl(
      Dao<ClassId, Class> classDao,
      Dao<ObjectRolePermission<ClassId>, GrantedPermission> classPermissionDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao,
      AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository,
      AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository) {
    this.classDao = classDao;
    this.classPermissionDao = classPermissionDao;
    this.classPropertyValueDao = classPropertyValueDao;
    this.textAttributeRepository = textAttributeRepository;
    this.referenceAttributeRepository = referenceAttributeRepository;
  }

  @Override
  protected ClassId extractKey(Class cls) {
    return new ClassId(cls);
  }

  /**
   * With bulk insert, first save all classes, then dependant values.
   */
  @Override
  protected void insert(Map<ClassId, Class> map, User user) {
    classDao.insert(map, user);

    for (Map.Entry<ClassId, Class> entry : map.entrySet()) {
      ClassId id = entry.getKey();
      Class cls = entry.getValue();

      insertPermissions(id, cls.getPermissions(), user);
      insertProperties(id, cls.getProperties(), user);
      insertTextAttributes(id, cls.getTextAttributes(), user);
      insertReferenceAttributes(id, cls.getReferenceAttributes(), user);
    }
  }

  @Override
  protected void insert(ClassId id, Class cls, User user) {
    classDao.insert(id, cls, user);
    insertPermissions(id, cls.getPermissions(), user);
    insertProperties(id, cls.getProperties(), user);
    insertTextAttributes(id, cls.getTextAttributes(), user);
    insertReferenceAttributes(id, cls.getReferenceAttributes(), user);
  }

  private void insertPermissions(ClassId id, Multimap<String, Permission> permissions, User user) {
    classPermissionDao.insert(
        RolePermissionsDtoToModel.create(id.getSchemeId(), id).apply(permissions), user);
  }

  private void insertProperties(ClassId id, Multimap<String, LangValue> properties, User user) {
    classPropertyValueDao.insert(PropertyValueDtoToModel.create(id).apply(properties), user);
  }

  private void insertTextAttributes(ClassId id, List<TextAttribute> attrs, User user) {
    textAttributeRepository.insert(newLinkedHashMap(transform(
        addTextAttrIndices(attrs), new TextAttributeToIdEntry(id))), user);
  }

  private void insertReferenceAttributes(ClassId id, List<ReferenceAttribute> attrs, User user) {
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
  protected void update(ClassId id, Class newClass, Class oldClass, User user) {
    classDao.update(id, newClass, user);

    updatePermissions(id, newClass.getPermissions(), oldClass.getPermissions(), user);
    updateProperties(id, newClass.getProperties(), oldClass.getProperties(), user);
    updateTextAttributes(id, addTextAttrIndices(newClass.getTextAttributes()),
                         oldClass.getTextAttributes(), user);
    updateReferenceAttributes(id, addRefAttrIndices(newClass.getReferenceAttributes()),
                              oldClass.getReferenceAttributes(), user);
  }

  private void updatePermissions(ClassId id,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions,
                                 User user) {

    Map<ObjectRolePermission<ClassId>, GrantedPermission> newPermissionMap =
        RolePermissionsDtoToModel.create(id.getSchemeId(), id).apply(newPermissions);
    Map<ObjectRolePermission<ClassId>, GrantedPermission> oldPermissionMap =
        RolePermissionsDtoToModel.create(id.getSchemeId(), id).apply(oldPermissions);

    MapDifference<ObjectRolePermission<ClassId>, GrantedPermission> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    classPermissionDao.insert(diff.entriesOnlyOnLeft(), user);
    classPermissionDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateProperties(ClassId id,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<ClassId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(id).apply(newPropertyMultimap);
    Map<PropertyValueId<ClassId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(id).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<ClassId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    classPropertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    classPropertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()), user);
    classPropertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  private void updateTextAttributes(ClassId id,
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

  private void updateReferenceAttributes(ClassId id,
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
  public void delete(ClassId id, User user) {
    delete(id, get(id, user).get(), user);
  }

  @Override
  protected void delete(ClassId id, Class cls, User user) {
    deletePermissions(id, cls.getPermissions(), user);
    deleteProperties(id, cls.getProperties(), user);
    deleteTextAttributes(id, cls.getTextAttributes(), user);
    deleteReferenceAttributes(id, cls.getReferenceAttributes(), user);
    classDao.delete(id, user);
  }

  private void deletePermissions(ClassId id, Multimap<String, Permission> permissions, User user) {
    classPermissionDao.delete(ImmutableList.copyOf(
        RolePermissionsDtoToModel.create(id.getSchemeId(), id).apply(permissions).keySet()), user);
  }

  private void deleteProperties(ClassId id, Multimap<String, LangValue> properties, User user) {
    classPropertyValueDao.delete(ImmutableList.copyOf(
        PropertyValueDtoToModel.create(id).apply(properties).keySet()), user);
  }

  private void deleteTextAttributes(ClassId id, List<TextAttribute> textAttributes, User user) {
    textAttributeRepository.delete(ImmutableMap.copyOf(
        Lists.transform(textAttributes, new TextAttributeToIdEntry(id))), user);
  }

  private void deleteReferenceAttributes(ClassId id, List<ReferenceAttribute> referenceAttributes,
                                         User user) {
    referenceAttributeRepository.delete(ImmutableMap.copyOf(
        Lists.transform(referenceAttributes, new ReferenceAttributeToIdEntry(id))), user);
  }

  @Override
  public boolean exists(ClassId id, User user) {
    return classDao.exists(id, user);
  }

  @Override
  public List<Class> get(SpecificationQuery<ClassId, Class> specification, User user) {
    return Lists.transform(classDao.getValues(specification.getSpecification(), user),
                           populateClassFunction(user));
  }

  @Override
  public Optional<Class> get(ClassId id, User user) {
    Optional<Class> o = classDao.get(id, user);
    return o.isPresent() ? Optional.of(populateClassFunction(user).apply(o.get()))
                         : Optional.<Class>absent();
  }

  private Function<Class, Class> populateClassFunction(User user) {
    return FunctionUtils.pipe(
        new CreateCopy(),
        new AddClassPermissions(user),
        new AddClassProperties(user),
        new AddClassTextAttributes(user),
        new AddClassReferenceAttributes(user));
  }

  private class CreateCopy implements Function<Class, Class> {

    public Class apply(Class cls) {
      return new Class(cls);
    }

  }

  /**
   * Load and add permissions to a class.
   */
  private class AddClassPermissions implements Function<Class, Class> {

    private User user;

    public AddClassPermissions(User user) {
      this.user = user;
    }

    @Override
    public Class apply(Class cls) {
      cls.setPermissions(RolePermissionsModelToDto.<ClassId>create().apply(
          classPermissionDao.getMap(new ClassPermissionsByClassId(
              new ClassId(cls.getSchemeId(), cls.getId())), user)));
      return cls;
    }
  }

  /**
   * Load and add properties to a class.
   */
  private class AddClassProperties implements Function<Class, Class> {

    private User user;

    public AddClassProperties(User user) {
      this.user = user;
    }

    @Override
    public Class apply(Class cls) {
      cls.setProperties(
          PropertyValueModelToDto.<ClassId>create().apply(classPropertyValueDao.getMap(
              new ClassPropertiesByClassId(
                  new ClassId(cls.getSchemeId(), cls.getId())), user)));
      return cls;
    }
  }

  /**
   * Load and add text attributes to a class.
   */
  private class AddClassTextAttributes implements Function<Class, Class> {

    private User user;

    public AddClassTextAttributes(User user) {
      this.user = user;
    }

    @Override
    public Class apply(Class cls) {
      cls.setTextAttributes(textAttributeRepository.get(
          new SpecificationQuery<TextAttributeId, TextAttribute>(
              new TextAttributesByClassId(new ClassId(cls.getSchemeId(), cls.getId()))), user));
      return cls;
    }
  }

  /**
   * Load and add reference attributes to a class.
   */
  private class AddClassReferenceAttributes implements Function<Class, Class> {

    private User user;

    public AddClassReferenceAttributes(User user) {
      this.user = user;
    }

    @Override
    public Class apply(Class cls) {
      cls.setReferenceAttributes(referenceAttributeRepository.get(
          new SpecificationQuery<ReferenceAttributeId, ReferenceAttribute>(
              new ReferenceAttributesByClassId(
                  new ClassId(cls.getSchemeId(), cls.getId()))), user));
      return cls;
    }
  }

  /**
   * TextAttribute -> (TextAttributeId, TextAttribute)
   */
  private class TextAttributeToIdEntry implements Function<TextAttribute,
      Map.Entry<TextAttributeId, TextAttribute>> {

    private ClassId domainId;

    public TextAttributeToIdEntry(ClassId domainId) {
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

    private ClassId domainId;

    public ReferenceAttributeToIdEntry(ClassId domainId) {
      this.domainId = domainId;
    }

    @Override
    public Map.Entry<ReferenceAttributeId, ReferenceAttribute> apply(ReferenceAttribute input) {
      return MapUtils.simpleEntry(new ReferenceAttributeId(domainId, input.getId()), input);
    }
  }

}
