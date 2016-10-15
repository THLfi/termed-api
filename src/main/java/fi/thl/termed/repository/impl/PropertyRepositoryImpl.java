package fi.thl.termed.repository.impl;

import java.util.Optional;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.util.specification.SpecificationQuery;
import fi.thl.termed.spesification.sql.PropertyPropertiesByPropertyId;
import fi.thl.termed.util.FunctionUtils;
import fi.thl.termed.domain.LangValue;

import static com.google.common.collect.ImmutableList.copyOf;
import static fi.thl.termed.util.collect.MapUtils.leftValues;

public class PropertyRepositoryImpl extends AbstractRepository<String, Property> {

  private Dao<String, Property> propertyDao;
  private Dao<PropertyValueId<String>, LangValue> propertyValueDao;

  public PropertyRepositoryImpl(Dao<String, Property> propertyDao,
                                Dao<PropertyValueId<String>, LangValue> propertyValueDao) {
    this.propertyDao = propertyDao;
    this.propertyValueDao = propertyValueDao;
  }

  @Override
  protected String extractKey(Property property) {
    return property.getId();
  }

  @Override
  protected void insert(String id, Property property, User user) {
    propertyDao.insert(id, property, user);
    insertProperties(id, property.getProperties(), user);
  }

  private void insertProperties(String id, Multimap<String, LangValue> properties, User user) {
    propertyValueDao.insert(PropertyValueDtoToModel.create(id).apply(properties), user);
  }

  @Override
  protected void update(String id, Property newProperty, Property oldProperty, User user) {
    propertyDao.update(id, newProperty, user);
    updateProperties(id, newProperty.getProperties(), oldProperty.getProperties(), user);
  }

  private void updateProperties(String propertyId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap,
                                User user) {

    Map<PropertyValueId<String>, LangValue> newProperties =
        PropertyValueDtoToModel.create(propertyId).apply(newPropertyMultimap);
    Map<PropertyValueId<String>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(propertyId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<String>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft(), user);
    propertyValueDao.update(leftValues(diff.entriesDiffering()), user);
    propertyValueDao.delete(copyOf(diff.entriesOnlyOnRight().keySet()), user);
  }

  @Override
  public void delete(String id, User user) {
    delete(id, get(id, user).get(), user);
  }

  @Override
  protected void delete(String id, Property value, User user) {
    deleteProperties(id, value.getProperties(), user);
    propertyDao.delete(id, user);
  }

  private void deleteProperties(String id, Multimap<String, LangValue> properties, User user) {
    propertyValueDao.delete(ImmutableList.copyOf(
        PropertyValueDtoToModel.create(id).apply(properties).keySet()), user);
  }

  @Override
  public boolean exists(String id, User user) {
    return propertyDao.exists(id, user);
  }

  @Override
  public List<Property> get(SpecificationQuery<String, Property> specification, User user) {
    return Lists.transform(propertyDao.getValues(specification.getSpecification(), user),
                           FunctionUtils.pipe(new CreateCopy(), new AddPropertyProperties(user)));
  }

  @Override
  public java.util.Optional<Property> get(String id, User user) {
    Optional<Property> o = propertyDao.get(id, user);
    return o.isPresent() ? java.util.Optional.of(new AddPropertyProperties(user).apply(o.get()))
                         : java.util.Optional.<Property>empty();
  }

  private class CreateCopy implements Function<Property, Property> {

    public Property apply(Property property) {
      return new Property(property);
    }

  }

  /**
   * Load and add properties to a property.
   */
  private class AddPropertyProperties implements Function<Property, Property> {

    private User user;

    public AddPropertyProperties(User user) {
      this.user = user;
    }

    @Override
    public Property apply(Property property) {
      property.setProperties(
          PropertyValueModelToDto.<String>create().apply(propertyValueDao.getMap(
              new PropertyPropertiesByPropertyId(property.getId()), user)));
      return property;
    }

  }

}
