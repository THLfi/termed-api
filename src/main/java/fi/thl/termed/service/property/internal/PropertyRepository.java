package fi.thl.termed.service.property.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.specification.Query;

import static com.google.common.collect.ImmutableList.copyOf;
import static fi.thl.termed.util.collect.MapUtils.leftValues;

public class PropertyRepository extends AbstractRepository<String, Property> {

  private Dao<String, Property> propertyDao;
  private Dao<PropertyValueId<String>, LangValue> propertyValueDao;

  public PropertyRepository(Dao<String, Property> propertyDao,
                            Dao<PropertyValueId<String>, LangValue> propertyValueDao) {
    this.propertyDao = propertyDao;
    this.propertyValueDao = propertyValueDao;
  }

  @Override
  public void insert(String id, Property property, User user) {
    propertyDao.insert(id, property, user);
    insertProperties(id, property.getProperties(), user);
  }

  private void insertProperties(String id, Multimap<String, LangValue> properties, User user) {
    propertyValueDao.insert(new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  @Override
  public void update(String id, Property newProperty, Property oldProperty, User user) {
    propertyDao.update(id, newProperty, user);
    updateProperties(id, newProperty.getProperties(), oldProperty.getProperties(), user);
  }

  private void updateProperties(String propertyId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<String>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(propertyId).apply(newPropertyMultimap);
    Map<PropertyValueId<String>, LangValue> oldProperties =
        new PropertyValueDtoToModel<>(propertyId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<String>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyValueDao.update(leftValues(diff.entriesDiffering()), user);
    propertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(String id, Property value, User user) {
    deleteProperties(id, value.getProperties(), user);
    propertyDao.delete(id, user);
  }

  private void deleteProperties(String id, Multimap<String, LangValue> properties, User user) {
    propertyValueDao.delete(ImmutableList.copyOf(
        new PropertyValueDtoToModel<>(id).apply(properties).keySet()), user);
  }

  @Override
  public boolean exists(String id, User user) {
    return propertyDao.exists(id, user);
  }

  @Override
  public List<Property> get(Query<String, Property> specification, User user) {
    return propertyDao.getValues(specification.getSpecification(), user).stream()
        .map(property -> populateValue(property, user))
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getKeys(Query<String, Property> specification, User user) {
    return propertyDao.getKeys(specification.getSpecification(), user);
  }

  @Override
  public Optional<Property> get(String id, User user) {
    return propertyDao.get(id, user).map(property -> populateValue(property, user));
  }

  private Property populateValue(Property property, User user) {
    property = new Property(property);

    property.setProperties(
        new PropertyValueModelToDto<String>().apply(propertyValueDao.getMap(
            new PropertyPropertiesByPropertyId(property.getId()), user)));

    return property;
  }

}
