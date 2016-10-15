package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueDtoToModel
    implements Function<Multimap<String, Resource>, Map<ResourceAttributeValueId, Resource>> {

  private ResourceId resourceId;

  public ReferenceAttributeValueDtoToModel(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  public static ReferenceAttributeValueDtoToModel create(ResourceId resourceId) {
    return new ReferenceAttributeValueDtoToModel(resourceId);
  }

  @Override
  public Map<ResourceAttributeValueId, Resource> apply(Multimap<String, Resource> input) {

    Map<ResourceAttributeValueId, Resource> result = Maps.newLinkedHashMap();

    for (String attributeId : input.keySet()) {
      int index = 0;

      for (Resource value : Sets.newLinkedHashSet(input.get(attributeId))) {
        result.put(new ResourceAttributeValueId(resourceId, attributeId, index++),
                   value);
      }
    }

    return result;
  }

}
