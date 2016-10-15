package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import java.util.Map;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueModelToReferrerDto
    implements Function<Map<ResourceAttributeValueId, ResourceId>, Multimap<String, ResourceId>> {

  public static ReferenceAttributeValueModelToReferrerDto create() {
    return new ReferenceAttributeValueModelToReferrerDto();
  }

  @Override
  public Multimap<String, ResourceId> apply(Map<ResourceAttributeValueId, ResourceId> input) {
    Multimap<String, ResourceId> map = LinkedHashMultimap.create();

    for (Map.Entry<ResourceAttributeValueId, ResourceId> entry : input.entrySet()) {
      map.put(entry.getKey().getAttributeId(), entry.getKey().getResourceId());
    }

    return map;
  }

}
