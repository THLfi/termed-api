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
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.repository.dao.ReferenceAttributeDao;
import fi.thl.termed.repository.dao.ReferenceAttributePropertyValueDao;
import fi.thl.termed.repository.spesification.ReferenceAttributePropertyValueSpecificationBySubjectId;
import fi.thl.termed.repository.spesification.Specification;
import fi.thl.termed.repository.transform.PropertyValueDtoToModel;
import fi.thl.termed.repository.transform.PropertyValueModelToDto;
import fi.thl.termed.util.LangValue;
import fi.thl.termed.util.MapUtils;

@Repository
public class ReferenceAttributeRepositoryImpl extends ReferenceAttributeRepository {

  @Autowired
  private ReferenceAttributeDao referenceAttributeDao;

  @Autowired
  private ReferenceAttributePropertyValueDao propertyValueDao;

  private Function<ReferenceAttribute, ReferenceAttribute> addReferenceAttributeProperties =
      new AddReferenceAttributeProperties();

  private ReferenceAttributeId getReferenceAttributeId(ReferenceAttribute referenceAttribute) {
    return new ReferenceAttributeId(new ClassId(referenceAttribute.getDomainSchemeId(),
                                                referenceAttribute.getDomainId()),
                                    new ClassId(referenceAttribute.getRangeSchemeId(),
                                                referenceAttribute.getRangeId()),
                                    referenceAttribute.getId());
  }

  @Override
  public void save(ReferenceAttribute referenceAttribute) {
    save(getReferenceAttributeId(referenceAttribute), referenceAttribute);
  }

  @Override
  protected void insert(ReferenceAttributeId id, ReferenceAttribute referenceAttribute) {
    referenceAttributeDao.insert(id, referenceAttribute);
    propertyValueDao.insert(PropertyValueDtoToModel
                                .create(id).apply(referenceAttribute.getProperties()));
  }

  @Override
  protected void update(ReferenceAttributeId id,
                        ReferenceAttribute newReferenceAttribute,
                        ReferenceAttribute oldReferenceAttribute) {

    referenceAttributeDao.update(id, newReferenceAttribute);
    updateProperties(id, newReferenceAttribute.getProperties(),
                     oldReferenceAttribute.getProperties());
  }

  private void updateProperties(ReferenceAttributeId attributeId,
                                Multimap<String, LangValue> newPropertyMultimap,
                                Multimap<String, LangValue> oldPropertyMultimap) {

    Map<PropertyValueId<ReferenceAttributeId>, LangValue> newProperties =
        PropertyValueDtoToModel.create(attributeId).apply(newPropertyMultimap);
    Map<PropertyValueId<ReferenceAttributeId>, LangValue> oldProperties =
        PropertyValueDtoToModel.create(attributeId).apply(oldPropertyMultimap);

    MapDifference<PropertyValueId<ReferenceAttributeId>, LangValue> diff =
        Maps.difference(newProperties, oldProperties);

    propertyValueDao.insert(diff.entriesOnlyOnLeft());
    propertyValueDao.update(MapUtils.leftValues(diff.entriesDiffering()));
    propertyValueDao.delete(diff.entriesOnlyOnRight().keySet());
  }

  @Override
  protected void delete(ReferenceAttributeId id, ReferenceAttribute value) {
    delete(id);
  }

  @Override
  public void delete(ReferenceAttributeId id) {
    referenceAttributeDao.delete(id);
  }

  @Override
  public boolean exists(ReferenceAttributeId id) {
    return referenceAttributeDao.exists(id);
  }

  @Override
  public List<ReferenceAttribute> get() {
    return Lists.transform(referenceAttributeDao.getValues(), addReferenceAttributeProperties);
  }

  @Override
  public List<ReferenceAttribute> get(
      Specification<ReferenceAttributeId, ReferenceAttribute> specification) {
    return Lists
        .transform(referenceAttributeDao.getValues(specification), addReferenceAttributeProperties);
  }

  @Override
  public ReferenceAttribute get(ReferenceAttributeId id) {
    return addReferenceAttributeProperties.apply(referenceAttributeDao.get(id));
  }

  /**
   * Load and add properties to a reference attribute.
   */
  private class AddReferenceAttributeProperties
      implements Function<ReferenceAttribute, ReferenceAttribute> {

    @Override
    public ReferenceAttribute apply(ReferenceAttribute referenceAttribute) {
      referenceAttribute.setProperties(
          PropertyValueModelToDto.<ReferenceAttributeId>create().apply(propertyValueDao.getMap(
              new ReferenceAttributePropertyValueSpecificationBySubjectId(
                  getReferenceAttributeId(referenceAttribute)))));
      return referenceAttribute;
    }
  }

}
