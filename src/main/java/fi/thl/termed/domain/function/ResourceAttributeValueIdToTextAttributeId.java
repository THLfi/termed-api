package fi.thl.termed.domain.function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.TextAttributeId;

public class ResourceAttributeValueIdToTextAttributeId
    implements java.util.function.Function<ResourceAttributeValueId, TextAttributeId> {

  @Override
  public TextAttributeId apply(ResourceAttributeValueId attributeValueId) {
    return new TextAttributeId(new ClassId(attributeValueId.getResourceId()),
                               attributeValueId.getAttributeId());
  }

}
