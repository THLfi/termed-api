package fi.thl.termed.repository.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;

public class PropertyValueModelToDto<K extends Serializable>
    implements Function<Map<PropertyValueId<K>, LangValue>, Multimap<String, LangValue>> {

  public static <K extends Serializable> PropertyValueModelToDto<K> create() {
    return new PropertyValueModelToDto<K>();
  }

  @Override
  public Multimap<String, LangValue> apply(Map<PropertyValueId<K>, LangValue> input) {
    Multimap<String, LangValue> map = LinkedHashMultimap.create();

    for (Map.Entry<PropertyValueId<K>, LangValue> entry : input.entrySet()) {
      map.put(entry.getKey().getPropertyId(), entry.getValue());
    }

    return map;
  }

}
