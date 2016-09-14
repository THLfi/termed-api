package fi.thl.termed.domain.function;

import com.google.common.base.Function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.ResourceAttributeValueId;

public class ResourceAttributeValueIdToReferenceAttributeId
    implements Function<ResourceAttributeValueId, ReferenceAttributeId> {

  @Override
  public ReferenceAttributeId apply(ResourceAttributeValueId attributeValueId) {
    return new ReferenceAttributeId(new ClassId(attributeValueId.getResourceId()),
                                    attributeValueId.getAttributeId());
  }

}
