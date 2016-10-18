package fi.thl.termed.domain.transform;

import java.util.function.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueIdModelToDto
    implements Function<Map<ResourceAttributeValueId, ResourceId>, Multimap<String, ResourceId>> {

  public static ReferenceAttributeValueIdModelToDto create() {
    return new ReferenceAttributeValueIdModelToDto();
  }

  @Override
  public Multimap<String, ResourceId> apply(Map<ResourceAttributeValueId, ResourceId> input) {
    Multimap<String, ResourceId> map = LinkedHashMultimap.create();

    for (Map.Entry<ResourceAttributeValueId, ResourceId> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getValue());
    }

    return map;
  }

}
