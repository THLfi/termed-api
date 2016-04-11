package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.Map;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;

public class ReferenceAttributeValueDtoToModel
    implements Function<Multimap<String, Resource>, Map<ResourceAttributeValueId, ResourceId>> {

  private ResourceId resourceId;

  public ReferenceAttributeValueDtoToModel(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  public static ReferenceAttributeValueDtoToModel create(ResourceId resourceId) {
    return new ReferenceAttributeValueDtoToModel(resourceId);
  }

  @Override
  public Map<ResourceAttributeValueId, ResourceId> apply(Multimap<String, Resource> input) {

    Map<ResourceAttributeValueId, ResourceId> result = Maps.newLinkedHashMap();

    int i = 0;

    for (Map.Entry<String, Resource> entry : input.entries()) {
      result.put(new ResourceAttributeValueId(resourceId, entry.getKey(), i++),
                 new ResourceId(entry.getValue()));
    }

    return result;
  }

}
