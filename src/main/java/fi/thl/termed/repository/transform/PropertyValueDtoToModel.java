package fi.thl.termed.repository.transform;

import java.util.function.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Map;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.LangValue;

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

    for (String propertyId : input.keySet()) {
      int index = 0;

      for (LangValue value : Sets.newLinkedHashSet(input.get(propertyId))) {
        if (!Strings.isNullOrEmpty(value.getValue())) {
          values.put(new PropertyValueId<K>(subjectId, propertyId, index++), value);
        }
      }
    }

    return values;
  }

}
