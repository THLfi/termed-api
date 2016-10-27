package fi.thl.termed.service.resource.internal;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.domain.ErrorCode;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

/**
 * Make sure that resource has an identifier
 */
public class IdInitializingResourceService extends ForwardingService<ResourceId, Resource> {

  public IdInitializingResourceService(Service<ResourceId, Resource> delegate) {
    super(delegate);
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User currentUser) {
    resources.forEach(this::resolveId);
    return super.save(resources, currentUser);
  }

  @Override
  public ResourceId save(Resource resource, User currentUser) {
    resolveId(resource);
    return super.save(resource, currentUser);
  }

  private void resolveId(Resource resource) {
    Preconditions.checkNotNull(resource.getTypeSchemeId(), ErrorCode.RESOURCE_SCHEME_ID_MISSING);
    Preconditions.checkNotNull(resource.getTypeId(), ErrorCode.RESOURCE_TYPE_ID_MISSING);

    UUID schemeId = resource.getTypeSchemeId();
    String typeId = resource.getTypeId();
    UUID id = resource.getId();

    String code = resource.getCode();
    String uri = resource.getUri();

    if (id == null && code != null) {
      id = UUIDs.nameUUIDFromString(schemeId + "-" + typeId + "-" + code);
    }
    if (id == null && uri != null) {
      id = UUIDs.nameUUIDFromString(schemeId + "-" + uri);
    }
    if (id == null) {
      id = UUID.randomUUID();
    }

    resource.setId(id);
  }

}
