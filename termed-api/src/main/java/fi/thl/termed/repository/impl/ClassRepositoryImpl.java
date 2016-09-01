package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ObjectRolePermission;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.repository.transform.RolePermissionsDtoToModel;
import fi.thl.termed.repository.transform.RolePermissionsModelToDto;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.ClassPermissionsByClassId;
import fi.thl.termed.spesification.sql.ClassPropertiesByClassId;
import fi.thl.termed.spesification.sql.ReferenceAttributesByClassId;
import fi.thl.termed.spesification.sql.TextAttributesByClassId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

import static com.google.common.collect.Maps.difference;

public class ClassRepositoryImpl extends AbstractRepository<ClassId, Class> {

  private Dao<ClassId, Class> classDao;
  private Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao;
  private Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao;

  private AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository;
  private AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository;

  private Function<Class, Class> populateClass;

  public ClassRepositoryImpl(
      Dao<ClassId, Class> classDao,
      Dao<ObjectRolePermission<ClassId>, Void> classPermissionDao,
      Dao<PropertyValueId<ClassId>, LangValue> classPropertyValueDao,
      AbstractRepository<TextAttributeId, TextAttribute> textAttributeRepository,
      AbstractRepository<ReferenceAttributeId, ReferenceAttribute> referenceAttributeRepository) {
    this.classDao = classDao;
    this.classPermissionDao = classPermissionDao;
    this.classPropertyValueDao = classPropertyValueDao;
    this.textAttributeRepository = textAttributeRepository;
    this.referenceAttributeRepository = referenceAttributeRepository;
    this.populateClass = FunctionUtils.pipe(
        new AddClassPermissions(),
        new AddClassProperties(),
        new AddClassTextAttributes(),
        new AddClassReferenceAttributes());
  }

  @Override
  public void save(Class cls) {
    save(new ClassId(cls.getSchemeId(), cls.getId()), cls);
  }

  /**
   * With bulk insert, first save all classes, then dependant values.
   */
  @Override
  protected void insert(Map<ClassId, Class> map) {
    classDao.insert(map);

    for (Map.Entry<ClassId, Class> entry : map.entrySet()) {
      ClassId classId = entry.getKey();
      Class cls = entry.getValue();

      insertPermissions(classId, cls.getPermissions());
      insertProperties(classId, cls.getProperties());
      insertTextAttributes(classId, cls.getTextAttributes());
      insertReferenceAttributes(classId, cls.getReferenceAttributes());
    }
  }

  @Override
  protected void insert(ClassId classId, Class cls) {
    classDao.insert(classId, cls);
    insertPermissions(classId, cls.getPermissions());
    insertProperties(classId, cls.getProperties());
    insertTextAttributes(classId, cls.getTextAttributes());
    insertReferenceAttributes(classId, cls.getReferenceAttributes());
  }

  private void insertPermissions(ClassId classId, Multimap<String, Permission> permissions) {
    classPermissionDao.insert(RolePermissionsDtoToModel.create(classId).apply(permissions));
  }

  private void insertProperties(ClassId classId, Multimap<String, LangValue> propertyMultimap) {
    classPropertyValueDao.insert(PropertyValueDtoToModel.create(classId).apply(propertyMultimap));
  }

  private void insertTextAttributes(ClassId classId, List<TextAttribute> textAttributes) {
    textAttributeRepository.insert(MapUtils.newLinkedHashMap(Lists.transform(
        addTextAttrIndices(textAttributes), new TextAttributeToIdEntry(classId))));
  }

  private void insertReferenceAttributes(ClassId classId, List<ReferenceAttribute> refAttributes) {
    referenceAttributeRepository.insert(MapUtils.newLinkedHashMap(Lists.transform(
        addRefAttrIndices(refAttributes), new ReferenceAttributeToIdEntry(classId))));
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
  protected void update(ClassId classId, Class newClass, Class oldClass) {
    classDao.update(classId, newClass);

    updatePermissions(classId, newClass.getPermissions(), oldClass.getPermissions());
    updateProperties(classId, newClass.getProperties(), oldClass.getProperties());
    updateTextAttributes(classId, addTextAttrIndices(newClass.getTextAttributes()),
                         oldClass.getTextAttributes());
    updateReferenceAttributes(classId, addRefAttrIndices(newClass.getReferenceAttributes()),
                              oldClass.getReferenceAttributes());
  }

  private void updatePermissions(ClassId classId,
                                 Multimap<String, Permission> newPermissions,
                                 Multimap<String, Permission> oldPermissions) {

    Map<ObjectRolePermission<ClassId>, Void> newPermissionMap =
        RolePermissionsDtoToModel.create(classId).apply(newPermissions);
    Map<ObjectRolePermission<ClassId>, Void> oldPermissionMap =
        RolePermissionsDtoToModel.create(classId).apply(oldPermissions);

    MapDifference<ObjectRolePermission<ClassId>, Void> diff =
        Maps.difference(newPermissionMap, oldPermissionMap);

    classPermissionDao.insert(diff.entriesOnlyOnLeft());
    classPermissionDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateProperties(ClassId classId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<ClassId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(classId).apply(newPropertyMultimap);
    Map<PropertyValueId<ClassId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(classId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<ClassId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    classPropertyValueDao.insert(diff.entriesOnlyOnLeft());
    classPropertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    classPropertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateTextAttributes(ClassId classId,
                                    List<TextAttribute> newTextAttributes,
                                    List<TextAttribute> oldTextAttributes) {

    Map<TextAttributeId, TextAttribute> newAttrs =
        MapUtils.newLinkedHashMap(
            Lists.transform(newTextAttributes, new TextAttributeToIdEntry(classId)));
    Map<TextAttributeId, TextAttribute> oldAttrs =
        MapUtils.newLinkedHashMap(
            Lists.transform(oldTextAttributes, new TextAttributeToIdEntry(classId)));

    MapDifference<TextAttributeId, TextAttribute> diff =
        difference(newAttrs, oldAttrs);

    textAttributeRepository.insert(diff.entriesOnlyOnLeft());
    textAttributeRepository.update(diff.entriesDiffering());
    textAttributeRepository.delete(diff.entriesOnlyOnRight());
  }

  private void updateReferenceAttributes(ClassId classId,
                                         List<ReferenceAttribute> newReferenceAttributes,
                                         List<ReferenceAttribute> oldReferenceAttributes) {

    Map<ReferenceAttributeId, ReferenceAttribute> newAttrs =
        MapUtils.newLinkedHashMap(
            Lists.transform(newReferenceAttributes, new ReferenceAttributeToIdEntry(classId)));
    Map<ReferenceAttributeId, ReferenceAttribute> oldAttrs =
        MapUtils.newLinkedHashMap(
            Lists.transform(oldReferenceAttributes, new ReferenceAttributeToIdEntry(classId)));

    MapDifference<ReferenceAttributeId, ReferenceAttribute> diff =
        difference(newAttrs, oldAttrs);

    referenceAttributeRepository.insert(diff.entriesOnlyOnLeft());
    referenceAttributeRepository.update(diff.entriesDiffering());
    referenceAttributeRepository.delete(diff.entriesOnlyOnRight());
  }

  @Override
  protected void delete(ClassId id, Class value) {
    delete(id);
  }

  @Override
  public void delete(ClassId id) {
    classDao.delete(id);
  }

  @Override
  public boolean exists(ClassId id) {
    return classDao.exists(id);
  }

  @Override
  public List<Class> get() {
    return Lists.transform(classDao.getValues(), new AddClassProperties());
  }

  @Override
  public List<Class> get(Specification<ClassId, Class> specification) {
    return Lists.transform(classDao.getValues(specification), populateClass);
  }

  @Override
  public Class get(ClassId id) {
    return populateClass.apply(classDao.get(id));
  }

  /**
   * Load and add permissions to a class.
   */
  private class AddClassPermissions implements Function<Class, Class> {

    @Override
    public Class apply(Class cls) {
      cls.setPermissions(RolePermissionsModelToDto.<ClassId>create().apply(
          classPermissionDao.getMap(new ClassPermissionsByClassId(
              new ClassId(cls.getSchemeId(), cls.getId())))));
      return cls;
    }
  }

  /**
   * Load and add properties to a class.
   */
  private class AddClassProperties implements Function<Class, Class> {

    @Override
    public Class apply(Class cls) {
      cls.setProperties(
          PropertyValueModelToDto.<ClassId>create().apply(classPropertyValueDao.getMap(
              new ClassPropertiesByClassId(
                  new ClassId(cls.getSchemeId(), cls.getId())))));
      return cls;
    }
  }

  /**
   * Load and add text attributes to a class.
   */
  private class AddClassTextAttributes implements Function<Class, Class> {

    @Override
    public Class apply(Class cls) {
      cls.setTextAttributes(textAttributeRepository.get(
          new TextAttributesByClassId(new ClassId(cls.getSchemeId(), cls.getId()))));
      return cls;
    }
  }

  /**
   * Load and add reference attributes to a class.
   */
  private class AddClassReferenceAttributes implements Function<Class, Class> {

    @Override
    public Class apply(Class cls) {
      cls.setReferenceAttributes(referenceAttributeRepository.get(
          new ReferenceAttributesByClassId(
              new ClassId(cls.getSchemeId(), cls.getId()))));
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
