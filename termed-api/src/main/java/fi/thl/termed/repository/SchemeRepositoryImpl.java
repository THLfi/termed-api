package fi.thl.termed.repository;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.repository.dao.SchemeDao;
import fi.thl.termed.repository.dao.SchemePropertyValueDao;
import fi.thl.termed.repository.spesification.ClassSpecificationBySchemeId;
import fi.thl.termed.repository.spesification.SchemePropertyValueSpecificationBySubjectId;
import fi.thl.termed.repository.spesification.Specification;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

import static com.google.common.collect.Maps.difference;

@Repository
public class SchemeRepositoryImpl extends SchemeRepository {

  @Autowired
  private SchemeDao schemeDao;

  @Autowired
  private SchemePropertyValueDao propertyValueDao;

  @Autowired
  private ClassRepository classRepository;

  private Function<Scheme, Scheme> addSchemeClasses = new AddSchemeClasses();
  private Function<Scheme, Scheme> addSchemeProperties = new AddSchemeProperties();

  @Override
  public void save(Scheme scheme) {
    save(scheme.getId(), scheme);
  }

  @Override
  protected void insert(UUID id, Scheme scheme) {
    schemeDao.insert(id, scheme);
    propertyValueDao.insert(
        PropertyValueDtoToModel.create(id).apply(scheme.getProperties()));
    classRepository.insert(
        MapUtils.newLinkedHashMap(Lists.transform(
            addClassIndices(scheme.getClasses()), new ClassToIdEntry(id))));
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
    updateProperties(id, newScheme.getProperties(), oldScheme.getProperties());
    updateClasses(id, addClassIndices(newScheme.getClasses()), oldScheme.getClasses());
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

    propertyValueDao.insert(diff.entriesOnlyOnLeft());
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  private void updateClasses(UUID schemeId, List<Class> newClasses, List<Class> oldClasses) {
    Map<ClassId, Class> newMappedClasses =
        MapUtils.newLinkedHashMap(Lists.transform(newClasses, new ClassToIdEntry(schemeId)));
    Map<ClassId, Class> oldMappedClasses =
        MapUtils.newLinkedHashMap(Lists.transform(oldClasses, new ClassToIdEntry(schemeId)));

    MapDifference<ClassId, Class> diff = difference(newMappedClasses, oldMappedClasses);

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
    return Lists.transform(schemeDao.getValues(), addSchemeProperties);
  }

  @Override
  public List<Scheme> get(Specification<UUID, Scheme> specification) {
    return Lists.transform(schemeDao.getValues(specification),
                           Functions.compose(addSchemeClasses, addSchemeProperties));
  }

  @Override
  public Scheme get(UUID id) {
    return addSchemeClasses.apply(addSchemeProperties.apply(schemeDao.get(id)));
  }

  /**
   * Load and add properties to a scheme.
   */
  private class AddSchemeProperties implements Function<Scheme, Scheme> {

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setProperties(
          PropertyValueModelToDto.<UUID>create().apply(propertyValueDao.getMap(
              new SchemePropertyValueSpecificationBySubjectId(scheme.getId()))));
      return scheme;
    }
  }

  /**
   * Load and add classes to a scheme.
   */
  private class AddSchemeClasses implements Function<Scheme, Scheme> {

    @Override
    public Scheme apply(Scheme scheme) {
      scheme.setClasses(classRepository.get(new ClassSpecificationBySchemeId(scheme.getId())));
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
