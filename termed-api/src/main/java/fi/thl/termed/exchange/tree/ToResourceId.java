package fi.thl.termed.exchange.tree;

import com.google.common.base.Function;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;

/**
 * Helper function to transform Resource into ResourceId.
 */
public class ToResourceId implements Function<Resource, ResourceId> {

  @Override
  public ResourceId apply(Resource input) {
    return new ResourceId(input);
  }

}
