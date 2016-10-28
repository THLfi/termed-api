package fi.thl.termed.domain.transform;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueIdModelToReferrerDto
    implements Function<Map<ResourceAttributeValueId, ResourceId>, Multimap<String, ResourceId>> {

  @Override
  public Multimap<String, ResourceId> apply(Map<ResourceAttributeValueId, ResourceId> input) {
    Multimap<String, ResourceId> map = LinkedHashMultimap.create();

    for (Map.Entry<ResourceAttributeValueId, ResourceId> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getKey().getResourceId());
    }

    return map;
  }

}
