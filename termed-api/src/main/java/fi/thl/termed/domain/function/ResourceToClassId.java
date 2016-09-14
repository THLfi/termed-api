package fi.thl.termed.domain.function;

import com.google.common.base.Function;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;

public class ResourceToClassId implements Function<Resource, ClassId> {

  @Override
  public ClassId apply(Resource resource) {
    return new ClassId(resource);
  }

}
