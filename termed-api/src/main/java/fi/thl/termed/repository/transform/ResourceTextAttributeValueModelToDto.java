package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;

import javax.annotation.Nullable;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.util.LangValue;

public class ResourceTextAttributeValueModelToDto
    implements Function<Map<ResourceAttributeValueId, LangValue>, Multimap<String, LangValue>> {

  public static ResourceTextAttributeValueModelToDto create() {
    return new ResourceTextAttributeValueModelToDto();
  }

  @Nullable
  public Multimap<String, LangValue> apply(Map<ResourceAttributeValueId, LangValue> input) {
    Multimap<String, LangValue> map = LinkedHashMultimap.create();

    for (Map.Entry<ResourceAttributeValueId, LangValue> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getValue());
    }

    return map;
  }

}
