package fi.thl.termed.domain.function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;

public class ResourceToClassId implements java.util.function.Function<Resource, ClassId> {

  @Override
  public ClassId apply(Resource resource) {
    return new ClassId(resource);
  }

}
