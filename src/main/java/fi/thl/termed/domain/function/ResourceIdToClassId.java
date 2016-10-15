package fi.thl.termed.domain.function;

import java.util.function.Function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ResourceId;

public class ResourceIdToClassId implements Function<ResourceId, ClassId> {

  @Override
  public ClassId apply(ResourceId resourceId) {
    return new ClassId(resourceId);
  }

}
