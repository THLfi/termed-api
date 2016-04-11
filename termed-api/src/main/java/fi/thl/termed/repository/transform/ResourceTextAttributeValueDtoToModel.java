package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.Map;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.LangValue;

public class ResourceTextAttributeValueDtoToModel
    implements Function<Multimap<String, LangValue>, Map<ResourceAttributeValueId, LangValue>> {

  private ResourceId resourceId;

  public ResourceTextAttributeValueDtoToModel(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  public static ResourceTextAttributeValueDtoToModel create(ResourceId resourceId) {
    return new ResourceTextAttributeValueDtoToModel(resourceId);
  }

  @Override
  public Map<ResourceAttributeValueId, LangValue> apply(Multimap<String, LangValue> input) {

    Map<ResourceAttributeValueId, LangValue> values = Maps.newLinkedHashMap();

    int index = 0;

    for (Map.Entry<String, LangValue> attrLangValues : input.entries()) {
      if (!attrLangValues.getValue().getValue().isEmpty()) {
        values.put(new ResourceAttributeValueId(resourceId, attrLangValues.getKey(), index++),
                   attrLangValues.getValue());
      }
    }

    return values;
  }

}
