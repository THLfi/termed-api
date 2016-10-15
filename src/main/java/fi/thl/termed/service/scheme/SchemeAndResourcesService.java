package fi.thl.termed.service.scheme;

import java.util.Optional;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeAndResources;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Specification;
import fi.thl.termed.util.specification.SpecificationQuery;
import fi.thl.termed.spesification.resource.ResourcesByClassId;
import fi.thl.termed.spesification.sql.ClassesBySchemeId;
import fi.thl.termed.util.specification.OrSpecification;

public class SchemeAndResourcesService implements Service<UUID, SchemeAndResources> {

  private Service<ResourceId, Resource> resourceService;
  private Service<UUID, Scheme> schemeService;
  private Dao<ClassId, Class> classDao;

  public SchemeAndResourcesService(
      Service<ResourceId, Resource> resourceService,
      Service<UUID, Scheme> schemeService,
      Dao<ClassId, Class> classDao) {
    this.resourceService = resourceService;
    this.schemeService = schemeService;
    this.classDao = classDao;
  }

  @Override
  public List<UUID> save(List<SchemeAndResources> values, User currentUser) {
    List<Scheme> schemes = Lists.newArrayList();
    List<Resource> resources = Lists.newArrayList();

    for (SchemeAndResources schemeAndResources : values) {
      schemes.add(schemeAndResources.getScheme());
      resources.addAll(schemeAndResources.getResources());
    }

    List<UUID> schemeIds = schemeService.save(schemes, currentUser);
    resourceService.save(resources, currentUser);

    return schemeIds;
  }

  @Override
  public UUID save(SchemeAndResources value, User currentUser) {
    UUID schemeId = schemeService.save(value.getScheme(), currentUser);
    resourceService.save(value.getResources(), currentUser);
    return schemeId;
  }

  @Override
  public void delete(UUID id, User currentUser) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<SchemeAndResources> get(SpecificationQuery<UUID, SchemeAndResources> specification,
                                      User currentUser) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<SchemeAndResources> get(List<UUID> ids, User currentUser) {
    List<SchemeAndResources> schemeAndResourcesList = Lists.newArrayList();
    for (UUID id : ids) {
      schemeAndResourcesList.add(get(id, currentUser).get());
    }
    return schemeAndResourcesList;
  }

  @Override
  public Optional<SchemeAndResources> get(UUID id, User currentUser) {
    Optional<Scheme> scheme = schemeService.get(id, currentUser);

    if (scheme.isPresent()) {
      return Optional.of(new SchemeAndResources(
          scheme.get(),
          resourceService.get(schemeResourcesQuery(id, currentUser), currentUser)));
    }

    return Optional.empty();
  }

  private SpecificationQuery<ResourceId, Resource> schemeResourcesQuery(UUID schemeId, User user) {
    // get a list of all scheme classes readable by the user
    List<ClassId> classIds = classDao.getKeys(new ClassesBySchemeId(schemeId), user);

    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    for (ClassId classId : classIds) {
      specifications.add(new ResourcesByClassId(classId));
    }

    return new SpecificationQuery<ResourceId, Resource>(
        new OrSpecification<ResourceId, Resource>(specifications),
        Collections.singletonList("id"), Integer.MAX_VALUE, SpecificationQuery.Engine.LUCENE);
  }

}
