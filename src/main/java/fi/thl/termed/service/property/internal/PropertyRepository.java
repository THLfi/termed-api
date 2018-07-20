package fi.thl.termed.service.property.internal;

import static fi.thl.termed.domain.Property.builderFromCopyOf;
import static fi.thl.termed.util.collect.MapUtils.leftValues;
import static fi.thl.termed.util.collect.MultimapUtils.toImmutableMultimap;
import static fi.thl.termed.util.collect.StreamUtils.zipIndex;
import static fi.thl.termed.util.collect.Tuple.entriesAsTuples;
import static fi.thl.termed.util.collect.Tuple.tuplesToMap;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.transform.PropertyValueDtoToModel;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.service.AbstractRepository;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.WriteOptions;
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
  public Stream<String> save(Stream<Property> values, SaveMode mode,
      WriteOptions opts, User user) {
    return super.save(addPropertyIndices(values), mode, opts, user);
  }

  private Stream<Property> addPropertyIndices(Stream<Property> values) {
    return zipIndex(values, (v, i) -> builderFromCopyOf(v).index(i).build());
  }

  @Override
  public void insert(String id, Property property, WriteOptions opts, User user) {
    propertyDao.insert(id, property, user);
    insertProperties(id, property.getProperties(), user);
  }

  private void insertProperties(String id, Multimap<String, LangValue> properties, User user) {
    propertyValueDao.insert(new PropertyValueDtoToModel<>(id).apply(properties), user);
  }

  @Override
  public void update(String id, Property property, WriteOptions opts, User user) {
    propertyDao.update(id, property, user);
    updateProperties(id, property.getProperties(), user);
  }

  private void updateProperties(String propertyId, Multimap<String, LangValue> propertyMultimap,
      User user) {

    Map<PropertyValueId<String>, LangValue> newProperties = tuplesToMap(
        new PropertyValueDtoToModel<>(propertyId).apply(propertyMultimap));
    Map<PropertyValueId<String>, LangValue> oldProperties = tuplesToMap(
        propertyValueDao.getEntries(new PropertyPropertiesByPropertyId(propertyId), user));

    MapDifference<PropertyValueId<String>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(entriesAsTuples(diff.entriesOnlyOnLeft()), user);
    propertyValueDao.update(entriesAsTuples(leftValues(diff.entriesDiffering())), user);
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet().stream(), user);
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
  public boolean exists(String id, User user) {
    return propertyDao.exists(id, user);
  }

  @Override
  public Stream<Property> values(Query<String, Property> query, User user) {
    return propertyDao.getValues(query.getWhere(), user).map(p -> populateValue(p, user));
  }

  @Override
  public Stream<String> keys(Query<String, Property> query, User user) {
    return propertyDao.getKeys(query.getWhere(), user);
  }

  @Override
  public Optional<Property> get(String id, User user, Select... selects) {
    return propertyDao.get(id, user).map(property -> populateValue(property, user));
  }

  private Property populateValue(Property property, User user) {
    try (Stream<Tuple2<PropertyValueId<String>, LangValue>> propertyStream =
        propertyValueDao.getEntries(new PropertyPropertiesByPropertyId(property.getId()), user)) {

      return builderFromCopyOf(property)
          .properties(propertyStream.collect(toImmutableMultimap(
              e -> e._1.getPropertyId(),
              e -> e._2)))
          .build();
    }
  }

}
