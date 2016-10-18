package fi.thl.termed.domain.transform;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueIdDtoToModel
    implements Function<Multimap<String, Resource>, Map<ResourceAttributeValueId, ResourceId>> {

  private ResourceId resourceId;

  public ReferenceAttributeValueIdDtoToModel(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  public static ReferenceAttributeValueIdDtoToModel create(ResourceId resourceId) {
    return new ReferenceAttributeValueIdDtoToModel(resourceId);
  }

  @Override
  public Map<ResourceAttributeValueId, ResourceId> apply(Multimap<String, Resource> input) {

    Map<ResourceAttributeValueId, ResourceId> result = Maps.newLinkedHashMap();

    for (String attributeId : input.keySet()) {
      int index = 0;

      for (Resource value : Sets.newLinkedHashSet(input.get(attributeId))) {
        result.put(new ResourceAttributeValueId(resourceId, attributeId, index++),
                   new ResourceId(value));
      }
    }

    return result;
  }

}
