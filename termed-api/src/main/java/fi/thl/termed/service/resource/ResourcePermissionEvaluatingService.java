package fi.thl.termed.service.resource;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.PermissionEvaluatingService;

/**
 * Extends basic PermissionEvaluatingService by differentiating update and insert permissions on
 * save. The default implementation simply requires both insert and update permissions on save.
 */
public class ResourcePermissionEvaluatingService
    extends PermissionEvaluatingService<ResourceId, Resource> {

  private Dao<ResourceId, Resource> resourceDao;

  public ResourcePermissionEvaluatingService(
      Service<ResourceId, Resource> delegate,
      PermissionEvaluator<ResourceId, Resource> evaluator,
      Dao<ResourceId, Resource> resourceDao) {
    super(delegate, evaluator);
    this.resourceDao = resourceDao;
  }

  @Override
  public void save(List<Resource> values, User currentUser) {
    for (Resource value : values) {
      Permission savePermission = resourceDao.exists(new ResourceId(value)) ? Permission.UPDATE
                                                                            : Permission.INSERT;
      if (!evaluator.hasPermission(currentUser, value, savePermission)) {
        throw new AccessDeniedException("Access is denied");
      }
    }

    super.save(values, currentUser);
  }

  @Override
  public void save(Resource value, User currentUser) {
    Permission savePermission = resourceDao.exists(new ResourceId(value)) ? Permission.UPDATE
                                                                          : Permission.INSERT;
    if (!evaluator.hasPermission(currentUser, value, savePermission)) {
      throw new AccessDeniedException("Access is denied");
    }

    super.save(value, currentUser);
  }

}
