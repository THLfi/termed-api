package fi.thl.termed.repository;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.repository.dao.PropertyDao;
import fi.thl.termed.repository.dao.PropertyPropertyValueDao;
import fi.thl.termed.repository.spesification.PropertyPropertyValueSpecificationBySubjectId;
import fi.thl.termed.repository.spesification.Specification;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

@Repository
public class PropertyRepositoryImpl extends PropertyRepository {

  @Autowired
  private PropertyDao propertyDao;

  @Autowired
  private PropertyPropertyValueDao propertyValueDao;

  private Function<Property, Property> addProperties = new AddPropertyProperties();

  @Override
  public void save(Property property) {
    save(property.getId(), property);
  }

  @Override
  protected void insert(String id, Property property) {
    propertyDao.insert(id, property);
    propertyValueDao.insert(PropertyValueDtoToModel.create(id).apply(property.getProperties()));
  }

  @Override
  protected void update(String id, Property newProperty, Property oldProperty) {
    propertyDao.update(id, newProperty);
    updateProperties(id, newProperty.getProperties(), oldProperty.getProperties());
  }

  private void updateProperties(String propertyId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<String>, LangValue> newProperties =
        PropertyValueDtoToModel.create(propertyId).apply(newPropertyMultimap);
    Map<PropertyValueId<String>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(propertyId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<String>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft());
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  @Override
  protected void delete(String id, Property value) {
    delete(id);
  }

  @Override
  public void delete(String id) {
    propertyDao.delete(id);
  }

  @Override
  public boolean exists(String id) {
    return propertyDao.exists(id);
  }

  @Override
  public List<Property> get() {
    return Lists.transform(propertyDao.getValues(), addProperties);
  }

  @Override
  public List<Property> get(Specification<String, Property> specification) {
    return Lists.transform(propertyDao.getValues(specification), addProperties);
  }

  @Override
  public Property get(String id) {
    return addProperties.apply(propertyDao.get(id));
  }

  /**
   * Load and add properties to a property.
   */
  private class AddPropertyProperties implements Function<Property, Property> {

    @Override
    public Property apply(Property property) {
      property.setProperties(
          PropertyValueModelToDto.<String>create().apply(propertyValueDao.getMap(
              new PropertyPropertyValueSpecificationBySubjectId(property.getId()))));
      return property;
    }
  }

}
