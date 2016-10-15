package fi.thl.termed.repository.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;

public class ReferenceAttributeValueModelToDto
    implements Function<Map<ResourceAttributeValueId, Resource>, Multimap<String, Resource>> {

  public static ReferenceAttributeValueModelToDto create() {
    return new ReferenceAttributeValueModelToDto();
  }

  @Override
  public Multimap<String, Resource> apply(Map<ResourceAttributeValueId, Resource> input) {
    Multimap<String, Resource> map = LinkedHashMultimap.create();

    for (Map.Entry<ResourceAttributeValueId, Resource> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getValue());
    }

    return map;
  }

}
