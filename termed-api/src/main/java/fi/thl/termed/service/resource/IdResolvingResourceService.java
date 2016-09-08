package fi.thl.termed.service.resource;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;
import fi.thl.termed.spesification.resource.ResourceByCode;
import fi.thl.termed.spesification.resource.ResourceByUri;
import fi.thl.termed.util.ErrorCode;
import fi.thl.termed.util.UUIDs;

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
  public void save(List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resolveId(resource);
    }
    super.save(resources, currentUser);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    resolveId(resource);
    super.save(resource, currentUser);
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
      List<ResourceId> ids = resourceDao.getKeys(new ResourceByCode(schemeId, typeId, code));
      return !ids.isEmpty() ? ids.get(0).getId() : null;
    }
    return null;
  }

  private UUID resolveIdForUri(UUID schemeId, String uri) {
    if (uri != null) {
      List<ResourceId> ids = resourceDao.getKeys(new ResourceByUri(schemeId, uri));
      return !ids.isEmpty() ? ids.get(0).getId() : null;
    }
    return null;
  }

}