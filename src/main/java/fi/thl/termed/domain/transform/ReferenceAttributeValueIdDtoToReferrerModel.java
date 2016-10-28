package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueIdDtoToReferrerModel
    implements Function<Multimap<String, ResourceId>, Map<ResourceAttributeValueId, ResourceId>> {

  private ResourceId resourceId;

  public ReferenceAttributeValueIdDtoToReferrerModel(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  @Override
  public Map<ResourceAttributeValueId, ResourceId> apply(Multimap<String, ResourceId> referrers) {

    Map<ResourceAttributeValueId, ResourceId> result = Maps.newLinkedHashMap();

    for (String attributeId : referrers.keySet()) {
      int index = 0;

      for (ResourceId value : Sets.newLinkedHashSet(referrers.get(attributeId))) {
        result.put(new ResourceAttributeValueId(value, attributeId, index++), resourceId);
      }
    }

    return result;
  }

}
