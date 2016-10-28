package fi.thl.termed.domain.transform;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;

public class PropertyValueDtoToModel<K extends Serializable>
    implements Function<Multimap<String, LangValue>, Map<PropertyValueId<K>, LangValue>> {

  private K subjectId;

  public PropertyValueDtoToModel(K subjectId) {
    this.subjectId = subjectId;
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
