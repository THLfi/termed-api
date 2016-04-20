package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.util.Map;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.StrictLangValue;

public class ResourceTextAttributeValueDtoToModel
    implements
    Function<Multimap<String, StrictLangValue>, Map<ResourceAttributeValueId, StrictLangValue>> {

  private ResourceId resourceId;

  public ResourceTextAttributeValueDtoToModel(ResourceId resourceId) {
    this.resourceId = resourceId;
  }

  public static ResourceTextAttributeValueDtoToModel create(ResourceId resourceId) {
    return new ResourceTextAttributeValueDtoToModel(resourceId);
  }

  @Override
  public Map<ResourceAttributeValueId, StrictLangValue> apply(
      Multimap<String, StrictLangValue> input) {

    Map<ResourceAttributeValueId, StrictLangValue> values = Maps.newLinkedHashMap();

    int index = 0;

    for (Map.Entry<String, StrictLangValue> attrLangValues : input.entries()) {
      StrictLangValue value = attrLangValues.getValue();
      if (!value.getValue().isEmpty()) {
        values.put(new ResourceAttributeValueId(resourceId, attrLangValues.getKey(), index++),
                   value.getRegex() != null ?
                   new StrictLangValue(value.getLang(), value.getValue(), value.getRegex()) :
                   new StrictLangValue(value.getLang(), value.getValue()));
      }
    }

    return values;
  }

}
