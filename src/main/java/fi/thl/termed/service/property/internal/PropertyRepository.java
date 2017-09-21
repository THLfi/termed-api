package fi.thl.termed.service.property.internal;

import static com.google.common.collect.ImmutableList.copyOf;
import static fi.thl.termed.util.collect.MapUtils.leftValues;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.domain.transform.PropertyValueModelToDto;
import fi.thl.termed.util.collect.Arg;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.specification.Specification;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class PropertyRepository extends AbstractRepository<String, Property> {

  private Dao<String, Property> propertyDao;
  private Dao<PropertyValueId<String>, LangValue> propertyValueDao;

  public PropertyRepository(Dao<String, Property> propertyDao,
      Dao<PropertyValueId<String>, LangValue> propertyValueDao) {
    this.propertyDao = propertyDao;
    this.propertyValueDao = propertyValueDao;
  }

  @Override
  public void insert(String id, Property property, SaveMode mode, WriteOptions opts, User user) {
    propertyDao.insert(id, property, user);
    insertProperties(id, property.getProperties(), user);
  }

  private void insertProperties(String id, Multimap<String, LangValue> properties, User user) {
    propertyValueDao.insert(new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  @Override
  public void update(String id, Property property, SaveMode mode, WriteOptions opts, User user) {
    propertyDao.update(id, property, user);
    updateProperties(id, property.getProperties(), user);
  }

  private void updateProperties(String propertyId, Multimap<String, LangValue> propertyMultimap,
      User user) {

    Map<PropertyValueId<String>, LangValue> newProperties =
        new PropertyValueDtoToModel<>(propertyId).apply(propertyMultimap);
    Map<PropertyValueId<String>, LangValue> oldProperties =
        propertyValueDao.getMap(new PropertyPropertiesByPropertyId(propertyId), user);

    MapDifference<PropertyValueId<String>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyValueDao.update(leftValues(diff.entriesDiffering()), user);
    propertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(String id, WriteOptions opts, User user) {
    deleteProperties(id, user);
    propertyDao.delete(id, user);
  }

  private void deleteProperties(String id, User user) {
    propertyValueDao.delete(propertyValueDao.getKeys(
        new PropertyPropertiesByPropertyId(id), user), user);
  }

  @Override
  public boolean exists(String id, User user, Arg... args) {
    return propertyDao.exists(id, user);
  }

  @Override
  public Stream<Property> get(Specification<String, Property> spec, User user, Arg... args) {
    return propertyDao.getValues(spec, user).stream()
        .map(property -> populateValue(property, user));
  }

  @Override
  public Stream<String> getKeys(Specification<String, Property> spec, User user, Arg... args) {
    return propertyDao.getKeys(spec, user).stream();
  }

  @Override
  public Optional<Property> get(String id, User user, Arg... args) {
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
