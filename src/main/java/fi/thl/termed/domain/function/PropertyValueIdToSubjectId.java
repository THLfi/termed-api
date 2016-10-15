package fi.thl.termed.domain.function;

import java.util.function.Function;

import java.io.Serializable;

import fi.thl.termed.domain.PropertyValueId;

public class PropertyValueIdToSubjectId<K extends Serializable> implements Function<PropertyValueId<K>, K> {

  @Override
  public K apply(PropertyValueId<K> propertyValueId) {
    return propertyValueId.getSubjectId();
  }

}
