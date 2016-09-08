package fi.thl.termed.web;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.permission.util.PermissionPredicate;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.util.OrSpecification;
import fi.thl.termed.spesification.util.FalseSpecification;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.util.TrueSpecification;
import fi.thl.termed.spesification.resource.ResourcesByClassId;
import fi.thl.termed.spesification.resource.ResourcesBySchemeId;
import fi.thl.termed.spesification.resource.ResourcesByTextAttributeValuePrefix;
import fi.thl.termed.spesification.sql.TextAttributesByClassId;
import fi.thl.termed.spesification.sql.TextAttributesBySchemeId;
import fi.thl.termed.util.ListUtils;

import static fi.thl.termed.spesification.SpecificationQuery.Engine.LUCENE;
import static fi.thl.termed.spesification.SpecificationQuery.Engine.SQL;

public class ResourceControllerImpl implements ResourceController {

  private Service<ResourceId, Resource> resourceService;

  private Dao<UUID, Scheme> schemeDao;
  private Dao<TextAttributeId, TextAttribute> textAttributeDao;

  private PermissionEvaluator<UUID> schemePermissionEvaluator;
  private PermissionEvaluator<ClassId> classPermissionEvaluator;
  private PermissionEvaluator<TextAttributeId> textAttributeEvaluator;

  public ResourceControllerImpl(
      Service<ResourceId, Resource> resourceService,
      Dao<UUID, Scheme> schemeDao,
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      PermissionEvaluator<UUID> schemePermissionEvaluator,
      PermissionEvaluator<ClassId> classPermissionEvaluator,
      PermissionEvaluator<TextAttributeId> textAttributeEvaluator) {
    this.resourceService = resourceService;
    this.schemeDao = schemeDao;
    this.textAttributeDao = textAttributeDao;
    this.schemePermissionEvaluator = schemePermissionEvaluator;
    this.classPermissionEvaluator = classPermissionEvaluator;
    this.textAttributeEvaluator = textAttributeEvaluator;
  }

  @Override
  public List<Resource> get(List<String> orderBy, int max, boolean bypassIndex, User currentUser) {
    Specification<ResourceId, Resource> specification = resourcesBy(schemeIds(currentUser));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @Override
  public List<Resource> get(String query, List<String> orderBy, int max, boolean bypassIndex,
                            User currentUser) {
    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(schemeIds(currentUser))
                        : resourcesBy(textAttributeIds(currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @Override
  public List<Resource> get(UUID schemeId, String query, List<String> orderBy, int max,
                            boolean bypassIndex, User currentUser) {
    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(schemeId(schemeId, currentUser))
                        : resourcesBy(textAttributeIds(schemeId, currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @Override
  public List<Resource> get(UUID schemeId, String typeId, String query, List<String> orderBy,
                            int max, boolean bypassIndex, User currentUser) {
    ClassId classId = new ClassId(schemeId, typeId);
    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classId(classId, currentUser))
                        : resourcesBy(textAttributeIds(classId, currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  private SpecificationQuery<ResourceId, Resource> query(
      Specification<ResourceId, Resource> specification, List<String> orderBy, int max,
      boolean bypassIndex) {
    return new SpecificationQuery<ResourceId, Resource>(
        specification, orderBy, max, bypassIndex ? SQL : LUCENE);
  }

  private Specification<ResourceId, Resource> resourcesBy(UUID schemeId) {
    return schemeId != null ? new ResourcesBySchemeId(schemeId)
                            : new FalseSpecification<ResourceId, Resource>();
  }

  private Specification<ResourceId, Resource> resourcesBy(ClassId classId) {
    return classId != null ? new ResourcesByClassId(classId)
                           : new FalseSpecification<ResourceId, Resource>();
  }

  private Specification<ResourceId, Resource> resourcesBy(List<UUID> schemeIds) {
    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    for (UUID schemeId : schemeIds) {
      specifications.add(new ResourcesBySchemeId(schemeId));
    }
    return new OrSpecification<ResourceId, Resource>(specifications);
  }

  private Specification<ResourceId, Resource> resourcesBy(List<TextAttributeId> textAttributeIds,
                                                          List<String> prefixQueries) {
    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    for (TextAttributeId attributeId : textAttributeIds) {
      for (String prefixQuery : prefixQueries) {
        specifications.add(new ResourcesByTextAttributeValuePrefix(attributeId, prefixQuery));
      }
    }
    return new OrSpecification<ResourceId, Resource>(specifications);
  }

  private List<UUID> schemeIds(User user) {
    return ListUtils.filter(
        schemeDao.getKeys(new TrueSpecification<UUID, Scheme>()),
        new PermissionPredicate<UUID>(schemePermissionEvaluator, user, Permission.READ));
  }

  private UUID schemeId(UUID schemeId, User user) {
    return schemePermissionEvaluator.hasPermission(user, schemeId, Permission.READ)
           ? schemeId : null;
  }

  private ClassId classId(ClassId classId, User user) {
    return classPermissionEvaluator.hasPermission(user, classId, Permission.READ)
           ? classId : null;
  }

  private List<TextAttributeId> textAttributeIds(User user) {
    return ListUtils.filter(
        textAttributeDao.getKeys(new TrueSpecification<TextAttributeId, TextAttribute>()),
        new PermissionPredicate<TextAttributeId>(textAttributeEvaluator, user, Permission.READ));
  }

  private List<TextAttributeId> textAttributeIds(UUID schemeId, User user) {
    return ListUtils.filter(
        textAttributeDao.getKeys(new TextAttributesBySchemeId(schemeId)),
        new PermissionPredicate<TextAttributeId>(textAttributeEvaluator, user, Permission.READ));
  }

  private List<TextAttributeId> textAttributeIds(ClassId classId, User user) {
    return ListUtils.filter(
        textAttributeDao.getKeys(new TextAttributesByClassId(classId)),
        new PermissionPredicate<TextAttributeId>(textAttributeEvaluator, user, Permission.READ));
  }

  private List<String> tokenize(String query) {
    return Arrays.asList(query.split("\\s"));
  }

  @Override
  public Resource get(UUID schemeId, String typeId, UUID id, User currentUser) {
    return resourceService.get(new ResourceId(schemeId, typeId, id), currentUser);
  }

  @Override
  public void post(UUID schemeId, String typeId, List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
      resource.setType(new Class(typeId));
    }
    resourceService.save(resources, currentUser);
  }

  @Override
  public Resource post(UUID schemeId, String typeId, Resource resource, User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(typeId));
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @Override
  public void post(UUID schemeId, List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
    }
    resourceService.save(resources, currentUser);
  }

  @Override
  public Resource post(UUID schemeId, Resource resource, User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @Override
  public void post(List<Resource> resources, User currentUser) {
    resourceService.save(resources, currentUser);
  }

  @Override
  public Resource post(Resource resource, User currentUser) {
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @Override
  public Resource put(UUID schemeId, String typeId, UUID id, Resource resource, User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(typeId));
    resource.setId(id);
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @Override
  public void delete(UUID schemeId, String typeId, UUID id, User currentUser) {
    resourceService.delete(new ResourceId(schemeId, typeId, id), currentUser);
  }

}
