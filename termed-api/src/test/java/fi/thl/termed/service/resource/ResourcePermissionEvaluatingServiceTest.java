package fi.thl.termed.service.resource;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.dao.MemoryBasedDao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.common.ObjectPermissionEvaluator;
import fi.thl.termed.permission.common.PermitAllPermissionEvaluator;
import fi.thl.termed.permission.resource.DelegatingResourcePermissionEvaluator;
import fi.thl.termed.repository.impl.DaoRepository;
import fi.thl.termed.service.Service;
import fi.thl.termed.service.common.RepositoryService;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.util.TrueSpecification;
import fi.thl.termed.util.UUIDs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResourcePermissionEvaluatingServiceTest {

  // helper function to extract resource id from resource
  private Function<Resource, ResourceId> getResourceId = new Function<Resource, ResourceId>() {
    public ResourceId apply(Resource input) {
      return new ResourceId(input);
    }
  };

  private SpecificationQuery<ResourceId, Resource> getAll =
      new SpecificationQuery<ResourceId, Resource>(
          new TrueSpecification<ResourceId, Resource>());

  // test "database"
  private Map<ResourceId, Resource> resources;
  private SetMultimap<ResourceId, Permission> resourcePermissions;

  // test data
  private Scheme scheme = new Scheme(UUIDs.nameUUIDFromString("ExampleScheme"));
  private Class type = new Class(scheme, "ExampleType");

  private Resource r1 = new Resource(scheme, type, UUIDs.nameUUIDFromString("r1"));
  private Resource r2 = new Resource(scheme, type, UUIDs.nameUUIDFromString("r2"));
  private Resource r3 = new Resource(scheme, type, UUIDs.nameUUIDFromString("r3"));
  private Resource r4 = new Resource(scheme, type, UUIDs.nameUUIDFromString("r4"));
  private Resource r5 = new Resource(scheme, type, UUIDs.nameUUIDFromString("r5"));

  // target of the test
  private ResourcePermissionEvaluatingService authorizedService;

  @Before
  public void setUp() {
    this.resources = Maps.newLinkedHashMap();
    this.resourcePermissions = HashMultimap.create();

    populateWithTestData(resources, resourcePermissions);

    this.authorizedService =
        buildMemoryBasedAuthorizedResourceService(resources, resourcePermissions);
  }

  private void populateWithTestData(Map<ResourceId, Resource> resources,
                                    SetMultimap<ResourceId, Permission> resourcePermissions) {

    resources.put(getResourceId.apply(r1), r1);
    resources.put(getResourceId.apply(r2), r2);
    resources.put(getResourceId.apply(r3), r3);
    resources.put(getResourceId.apply(r4), r4);
    resources.put(getResourceId.apply(r4), r4);
    resources.put(getResourceId.apply(r5), r5);

    resourcePermissions.put(getResourceId.apply(r1), Permission.READ);
    resourcePermissions.put(getResourceId.apply(r2), Permission.INSERT);
    resourcePermissions.put(getResourceId.apply(r3), Permission.UPDATE);
    resourcePermissions.put(getResourceId.apply(r4), Permission.DELETE);
    resourcePermissions.put(getResourceId.apply(r5), Permission.READ);
    resourcePermissions.put(getResourceId.apply(r5), Permission.INSERT);
    resourcePermissions.put(getResourceId.apply(r5), Permission.UPDATE);
    resourcePermissions.put(getResourceId.apply(r5), Permission.DELETE);
  }

  private ResourcePermissionEvaluatingService buildMemoryBasedAuthorizedResourceService(
      Map<ResourceId, Resource> resources,
      SetMultimap<ResourceId, Permission> resourcePermissions) {

    Dao<ResourceId, Resource> baseDao =
        new MemoryBasedDao<ResourceId, Resource>(resources);

    PermissionEvaluator<ResourceId> idEvaluator =
        new ObjectPermissionEvaluator<ResourceId>(resourcePermissions);
    PermissionEvaluator<Resource> valueEvaluator =
        new DelegatingResourcePermissionEvaluator(idEvaluator);
    PermissionEvaluator<Specification<ResourceId, Resource>> specificationEvaluator =
    new PermitAllPermissionEvaluator<Specification<ResourceId, Resource>>();

    Service<ResourceId, Resource> baseService =
        new RepositoryService<ResourceId, Resource>(
            new DaoRepository<ResourceId, Resource>(baseDao, getResourceId));

    return new ResourcePermissionEvaluatingService(
        baseService, idEvaluator, valueEvaluator, specificationEvaluator, baseDao);
  }

  @Test
  public void shouldFilterForRead() {
    assertEquals(ImmutableSet.of(r1, r5),
                 ImmutableSet.copyOf(authorizedService.get(getAll, null)));
  }

  @Test
  public void shouldFilterForReadAfterChangingPermissions() {
    assertEquals(ImmutableSet.of(r1, r5), ImmutableSet.copyOf(authorizedService.get(getAll, null)));
    resourcePermissions.remove(getResourceId.apply(r1), Permission.READ);
    assertEquals(ImmutableSet.of(r5), ImmutableSet.copyOf(authorizedService.get(getAll, null)));
  }

  @Test(expected = AccessDeniedException.class)
  public void shouldFailToUpdateReadOnlyResource() {
    authorizedService.save(r1, null);
  }

  @Test(expected = AccessDeniedException.class)
  public void shouldFailToUpdateInsertOnlyResource() {
    authorizedService.save(r2, null);
  }

  @Test(expected = AccessDeniedException.class)
  public void shouldFailToUpdateDeleteOnlyResource() {
    authorizedService.save(r4, null);
  }

  @Test(expected = AccessDeniedException.class)
  public void shouldFailToDeleteReadOnlyResource() {
    authorizedService.delete(getResourceId.apply(r1), null);
  }

  @Test(expected = AccessDeniedException.class)
  public void shouldFailToDeleteInsertOnlyResource() {
    authorizedService.delete(getResourceId.apply(r2), null);
  }

  @Test(expected = AccessDeniedException.class)
  public void shouldFailToDeleteUpdateOnlyResource() {
    authorizedService.delete(getResourceId.apply(r3), null);
  }

  @Test
  public void shouldDeletePermittedResource() {
    ResourceId deletableResourceId = getResourceId.apply(r4);

    assertTrue(resources.containsKey(deletableResourceId));
    authorizedService.delete(deletableResourceId, null);
    assertFalse(resources.containsKey(deletableResourceId));
  }

}