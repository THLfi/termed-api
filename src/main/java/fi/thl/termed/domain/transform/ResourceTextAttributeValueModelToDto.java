package fi.thl.termed.domain.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.StrictLangValue;

public class ResourceTextAttributeValueModelToDto
    implements
    Function<Map<ResourceAttributeValueId, StrictLangValue>, Multimap<String, StrictLangValue>> {

  @Override
  public Multimap<String, StrictLangValue> apply(
      Map<ResourceAttributeValueId, StrictLangValue> input) {
    Multimap<String, StrictLangValue> map = LinkedHashMultimap.create();

    for (Map.Entry<ResourceAttributeValueId, StrictLangValue> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getValue());
    }

    return map;
  }

}
