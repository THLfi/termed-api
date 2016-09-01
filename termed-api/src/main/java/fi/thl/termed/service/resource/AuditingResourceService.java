package fi.thl.termed.service.resource;

import java.util.Date;
import java.util.List;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.ForwardingService;

/**
 * Adds created and update timestamps with authors
 */
public class AuditingResourceService extends ForwardingService<ResourceId, Resource> {

  private Dao<ResourceId, Resource> resourceDao;

  public AuditingResourceService(Service<ResourceId, Resource> delegate,
                                 Dao<ResourceId, Resource> resourceDao) {
    super(delegate);
    this.resourceDao = resourceDao;
  }

  @Override
  public void save(List<Resource> resources, User currentUser) {
    Date now = new Date();
    for (Resource resource : resources) {
      addAuditInfo(resource, currentUser, now);
    }
    super.save(resources, currentUser);
  }

  @Override
  public void save(Resource resource, User currentUser) {
    addAuditInfo(resource, currentUser, new Date());
    super.save(resource, currentUser);
  }

  private void addAuditInfo(Resource resource, User currentUser, Date now) {
    ResourceId resourceId = new ResourceId(resource);

    if (resourceDao.exists(resourceId)) {
      Resource oldResource = resourceDao.get(resourceId);
      resource.setCreatedBy(oldResource.getCreatedBy());
      resource.setCreatedDate(oldResource.getCreatedDate());
    } else {
      resource.setCreatedBy(currentUser.getUsername());
      resource.setCreatedDate(now);
    }

    resource.setLastModifiedBy(currentUser.getUsername());
    resource.setLastModifiedDate(now);
  }

}
