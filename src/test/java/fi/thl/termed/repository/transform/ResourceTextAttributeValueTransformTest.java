package fi.thl.termed.repository.transform;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.UUIDs;

public class ResourceTextAttributeValueTransformTest {

  @Test
  public void shouldTransformTextAttributeValueDtoToModelAndBack() {
    ResourceId resourceId = new ResourceId(UUIDs.nameUUIDFromString("SchemeID"), "TypeId",
                                           UUIDs.nameUUIDFromString("ResourceId"));

    Multimap<String, StrictLangValue> propertyDto =
        ImmutableSetMultimap.<String, StrictLangValue>builder()
            .put("label", new StrictLangValue("en", "label_0"))
            .put("label", new StrictLangValue("sv", "label_1"))
            .put("label", new StrictLangValue("en", "label_2"))
            .put("desc", new StrictLangValue("en", "desc_0"))
            .put("desc", new StrictLangValue("en", "desc_1"))
            .put("desc", new StrictLangValue("en", "desc_2"))
            .put("label", new StrictLangValue("sv", "label_1"))
            .put("label", new StrictLangValue("sv", "label_3"))
            .build();

    Map<ResourceAttributeValueId, StrictLangValue> propertyModel =
        ImmutableMap.<ResourceAttributeValueId, StrictLangValue>builder()
            .put(new ResourceAttributeValueId(resourceId, "label", 0),
                 new StrictLangValue("en", "label_0"))
            .put(new ResourceAttributeValueId(resourceId, "label", 1),
                 new StrictLangValue("sv", "label_1"))
            .put(new ResourceAttributeValueId(resourceId, "label", 2),
                 new StrictLangValue("en", "label_2"))
            .put(new ResourceAttributeValueId(resourceId, "label", 3),
                 new StrictLangValue("sv", "label_3"))
            .put(new ResourceAttributeValueId(resourceId, "desc", 0),
                 new StrictLangValue("en", "desc_0"))
            .put(new ResourceAttributeValueId(resourceId, "desc", 1),
                 new StrictLangValue("en", "desc_1"))
            .put(new ResourceAttributeValueId(resourceId, "desc", 2),
                 new StrictLangValue("en", "desc_2"))
            .build();

    Map<ResourceAttributeValueId, StrictLangValue> actualPropertyModel =
        ResourceTextAttributeValueDtoToModel.create(resourceId).apply(propertyDto);
    Assert.assertEquals(propertyModel, actualPropertyModel);

    Multimap<String, StrictLangValue> actualPropertyDto =
        ResourceTextAttributeValueModelToDto.create().apply(propertyModel);
    Assert.assertEquals(propertyDto, actualPropertyDto);
  }

}
