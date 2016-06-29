package fi.thl.termed.repository.transform;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Map;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.StrictLangValue;

import static com.google.common.base.MoreObjects.firstNonNull;

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

    for (String attributeId : input.keySet()) {
      int index = 0;

      for (StrictLangValue value : Sets.newLinkedHashSet(input.get(attributeId))) {
        if (!Strings.isNullOrEmpty(value.getValue())) {
          values.put(new ResourceAttributeValueId(resourceId, attributeId, index++),
                     new StrictLangValue(value.getLang(),
                                         value.getValue(),
                                         firstNonNull(value.getRegex(),
                                                      RegularExpressions.ALL)));
        }
      }
    }

    return values;
  }

}
