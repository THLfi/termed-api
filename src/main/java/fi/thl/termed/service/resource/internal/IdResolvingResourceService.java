package fi.thl.termed.service.resource.internal;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.resource.specification.ResourceByCode;
import fi.thl.termed.service.resource.specification.ResourceByUri;
import fi.thl.termed.util.ErrorCode;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

/**
 * Make sure that resource has an identifier
 */
public class IdResolvingResourceService extends ForwardingService<ResourceId, Resource> {

  private Dao<ResourceId, Resource> resourceDao;

  public IdResolvingResourceService(Service<ResourceId, Resource> delegate,
                                    Dao<ResourceId, Resource> resourceDao) {
    super(delegate);
    this.resourceDao = resourceDao;
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resolveId(resource);
    }
    return super.save(resources, currentUser);
  }

  @Override
  public ResourceId save(Resource resource, User currentUser) {
    resolveId(resource);
    return super.save(resource, currentUser);
  }

  private void resolveId(Resource resource) {
    Preconditions.checkNotNull(resource.getSchemeId(), ErrorCode.RESOURCE_SCHEME_ID_MISSING);
    Preconditions.checkNotNull(resource.getTypeId(), ErrorCode.RESOURCE_TYPE_ID_MISSING);

    UUID schemeId = resource.getSchemeId();
    String typeId = resource.getTypeId();
    UUID id = resource.getId();

    String code = resource.getCode();
    String uri = resource.getUri();

    if (id == null && code != null) {
      id = resolveIdForCode(schemeId, typeId, code);
    }
    if (id == null && uri != null) {
      id = resolveIdForUri(schemeId, uri);
    }
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

  private UUID resolveIdForCode(UUID schemeId, String typeId, String code) {
    if (code != null) {
      List<ResourceId> ids = resourceDao.getKeys(
          new ResourceByCode(schemeId, typeId, code),
          new User("resourceResolver", "", AppRole.ADMIN));
      return !ids.isEmpty() ? ids.get(0).getId() : null;
    }
    return null;
  }

  private UUID resolveIdForUri(UUID schemeId, String uri) {
    if (uri != null) {
      List<ResourceId> ids = resourceDao.getKeys(
          new ResourceByUri(schemeId, uri),
          new User("resourceResolver", "", AppRole.ADMIN));
      return !ids.isEmpty() ? ids.get(0).getId() : null;
    }
    return null;
  }

}
