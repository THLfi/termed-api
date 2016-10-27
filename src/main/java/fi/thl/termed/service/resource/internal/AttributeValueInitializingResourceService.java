package fi.thl.termed.service.resource.internal;

import java.util.List;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static com.google.common.base.MoreObjects.firstNonNull;

public class AttributeValueInitializingResourceService
    extends ForwardingService<ResourceId, Resource> {

  public AttributeValueInitializingResourceService(
      Service<ResourceId, Resource> delegate) {
    super(delegate);
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User currentUser) {
    resources.forEach(this::resolveAttributes);
    return super.save(resources, currentUser);
  }

  @Override
  public ResourceId save(Resource resource, User currentUser) {
    resolveAttributes(resource);
    return super.save(resource, currentUser);
  }

  private void resolveAttributes(Resource resource) {
    for (StrictLangValue value : resource.getProperties().values()) {
      value.setRegex(firstNonNull(value.getRegex(), RegularExpressions.ALL));
    }
    for (ResourceId value : resource.getReferences().values()) {
      value.setType(firstNonNull(value.getType(), resource.getType()));
    }
  }

}
