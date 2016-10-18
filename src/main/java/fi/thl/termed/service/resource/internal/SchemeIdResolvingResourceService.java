package fi.thl.termed.service.resource.internal;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.scheme.specification.SchemeByCode;
import fi.thl.termed.service.scheme.specification.SchemeByUri;
import fi.thl.termed.util.ErrorCode;
import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static com.google.common.collect.Iterables.getFirst;

/**
 * Make sure that resource has a scheme id
 */
public class SchemeIdResolvingResourceService extends ForwardingService<ResourceId, Resource> {

  private Dao<UUID, Scheme> schemeDao;

  public SchemeIdResolvingResourceService(Service<ResourceId, Resource> delegate,
                                          Dao<UUID, Scheme> schemeDao) {
    super(delegate);
    this.schemeDao = schemeDao;
  }

  @Override
  public List<ResourceId> save(List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resolveSchemeId(resource.getScheme());
    }
    return super.save(resources, currentUser);
  }

  @Override
  public ResourceId save(Resource resource, User currentUser) {
    resolveSchemeId(resource.getScheme());
    return super.save(resource, currentUser);
  }

  private void resolveSchemeId(Scheme scheme) {
    UUID id = scheme.getId();

    if (id == null) {
      id = resolveSchemeIdForCode(scheme.getCode());
    }
    if (id == null) {
      id = resolveSchemeIdForUri(scheme.getUri());
    }

    Preconditions.checkNotNull(id, ErrorCode.RESOURCE_SCHEME_ID_MISSING);
    scheme.setId(id);
  }

  private UUID resolveSchemeIdForCode(String code) {
    return code != null ? getFirst(
        schemeDao.getKeys(new SchemeByCode(code),
                          new User("schemeResolver", "", AppRole.ADMIN)), null) : null;
  }

  private UUID resolveSchemeIdForUri(String uri) {
    return uri != null ? getFirst(
        schemeDao.getKeys(new SchemeByUri(uri),
                          new User("schemeResolver", "", AppRole.ADMIN)), null) : null;
  }

}
