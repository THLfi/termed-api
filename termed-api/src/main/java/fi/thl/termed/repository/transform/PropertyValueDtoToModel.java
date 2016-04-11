package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.LangValue;

public class PropertyValueDtoToModel<K extends Serializable>
    implements Function<Multimap<String, LangValue>, Map<PropertyValueId<K>, LangValue>> {

  private K subjectId;

  public PropertyValueDtoToModel(K subjectId) {
    this.subjectId = subjectId;
  }

  public static <K extends Serializable> PropertyValueDtoToModel<K> create(K subjectId) {
    return new PropertyValueDtoToModel<K>(subjectId);
  }

  @Override
  public Map<PropertyValueId<K>, LangValue> apply(Multimap<String, LangValue> input) {
    Map<PropertyValueId<K>, LangValue> values = Maps.newLinkedHashMap();

    int index = 0;

    for (Map.Entry<String, LangValue> propertyLangValues : input.entries()) {
      values.put(new PropertyValueId<K>(subjectId, propertyLangValues.getKey(), index++),
                 propertyLangValues.getValue());
    }

    return values;
  }

}
