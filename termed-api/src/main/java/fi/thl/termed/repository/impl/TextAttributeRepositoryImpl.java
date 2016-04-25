package fi.thl.termed.repository.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.dao.Dao;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.sql.TextAttributeProperiesByAttributeId;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

public class TextAttributeRepositoryImpl
    extends AbstractRepository<TextAttributeId, TextAttribute> {

  private Dao<TextAttributeId, TextAttribute> textAttributeDao;
  private Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao;
  private Function<TextAttribute, TextAttribute> addTextAttributeProperties;

  public TextAttributeRepositoryImpl(
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      Dao<PropertyValueId<TextAttributeId>, LangValue> textAttributePropertyValueDao) {
    this.textAttributeDao = textAttributeDao;
    this.textAttributePropertyValueDao = textAttributePropertyValueDao;
    this.addTextAttributeProperties = new AddTextAttributeProperties();
  }

  private TextAttributeId getTextAttributeId(TextAttribute textAttribute) {
    return new TextAttributeId(new ClassId(textAttribute.getDomainSchemeId(),
                                           textAttribute.getDomainId()),
                               textAttribute.getId());
  }

  @Override
  public void save(TextAttribute textAttribute) {
    save(getTextAttributeId(textAttribute), textAttribute);
  }

  @Override
  protected void insert(TextAttributeId id, TextAttribute textAttribute) {
    textAttributeDao.insert(id, textAttribute);
    textAttributePropertyValueDao.insert(PropertyValueDtoToModel
                                             .create(id).apply(textAttribute.getProperties()));
  }

  @Override
  protected void update(TextAttributeId id,
                        TextAttribute newTextAttribute,
                        TextAttribute oldTextAttribute) {

    textAttributeDao.update(id, newTextAttribute);
    updateProperties(id, newTextAttribute.getProperties(), oldTextAttribute.getProperties());
  }

  private void updateProperties(TextAttributeId attributeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<TextAttributeId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<TextAttributeId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<TextAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    textAttributePropertyValueDao.insert(diff.entriesOnlyOnLeft());
    textAttributePropertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    textAttributePropertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  @Override
  protected void delete(TextAttributeId id, TextAttribute value) {
    delete(id);
  }

  @Override
  public void delete(TextAttributeId id) {
    textAttributeDao.delete(id);
  }

  @Override
  public boolean exists(TextAttributeId id) {
    return textAttributeDao.exists(id);
  }

  @Override
  public List<TextAttribute> get() {
    return Lists.transform(textAttributeDao.getValues(), addTextAttributeProperties);
  }

  @Override
  public List<TextAttribute> get(Specification<TextAttributeId, TextAttribute> specification) {
    return Lists.transform(textAttributeDao.getValues(specification), addTextAttributeProperties);
  }

  @Override
  public TextAttribute get(TextAttributeId id) {
    return addTextAttributeProperties.apply(textAttributeDao.get(id));
  }

  /**
   * Load and add properties to a text attribute.
   */
  private class AddTextAttributeProperties implements Function<TextAttribute, TextAttribute> {

    @Override
    public TextAttribute apply(TextAttribute textAttribute) {
      textAttribute.setProperties(
          PropertyValueModelToDto.<TextAttributeId>create()
              .apply(textAttributePropertyValueDao.getMap(
                  new TextAttributeProperiesByAttributeId(
                      getTextAttributeId(textAttribute)))));
      return textAttribute;
    }
  }

}
