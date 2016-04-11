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

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.repository.dao.TextAttributeDao;
import fi.thl.termed.repository.dao.TextAttributePropertyValueDao;
import fi.thl.termed.repository.spesification.Specification;
import fi.thl.termed.repository.spesification.TextAttributePropertyValueSpecificationBySubjectId;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

@Repository
public class TextAttributeRepositoryImpl extends TextAttributeRepository {

  @Autowired
  private TextAttributeDao textAttributeDao;

  @Autowired
  private TextAttributePropertyValueDao propertyValueDao;

  private Function<TextAttribute, TextAttribute> addTextAttributeProperties =
      new AddTextAttributeProperties();

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
    propertyValueDao.insert(PropertyValueDtoToModel
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

    propertyValueDao.insert(diff.entriesOnlyOnLeft());
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
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
          PropertyValueModelToDto.<TextAttributeId>create().apply(propertyValueDao.getMap(
              new TextAttributePropertyValueSpecificationBySubjectId(
                  getTextAttributeId(textAttribute)))));
      return textAttribute;
    }
  }

}
