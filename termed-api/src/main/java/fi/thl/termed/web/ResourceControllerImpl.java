package fi.thl.termed.web;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.resource.ResourcesByClassId;
import fi.thl.termed.spesification.resource.ResourcesByTextAttributeValuePrefix;
import fi.thl.termed.spesification.sql.ClassesBySchemeId;
import fi.thl.termed.spesification.sql.TextAttributesByClassId;
import fi.thl.termed.spesification.sql.TextAttributesBySchemeId;
import fi.thl.termed.spesification.util.FalseSpecification;
import fi.thl.termed.spesification.util.OrSpecification;
import fi.thl.termed.spesification.util.TrueSpecification;

import static fi.thl.termed.spesification.SpecificationQuery.Engine.LUCENE;
import static fi.thl.termed.spesification.SpecificationQuery.Engine.SQL;

public class ResourceControllerImpl implements ResourceController {

  private Service<ResourceId, Resource> resourceService;

  private Dao<ClassId, Class> classDao;
  private Dao<TextAttributeId, TextAttribute> textAttributeDao;

  public ResourceControllerImpl(
      Service<ResourceId, Resource> resourceService,
      Dao<ClassId, Class> classDao,
      Dao<TextAttributeId, TextAttribute> textAttributeDao) {
    this.resourceService = resourceService;
    this.classDao = classDao;
    this.textAttributeDao = textAttributeDao;
  }

  @Override
  public List<Resource> get(String query, List<String> orderBy, int max, boolean bypassIndex,
                            User currentUser) {
    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classIds(currentUser))
                        : resourcesBy(textAttributeIds(currentUser), tokenize(query));
    return resourceService.get(query(specification, orderBy, max, bypassIndex), currentUser);
  }

  @Override
  public List<Resource> get(UUID schemeId, String query, List<String> orderBy, int max,
                            boolean bypassIndex, User currentUser) {
    Specification<ResourceId, Resource> specification =
        query.isEmpty() ? resourcesBy(classIds(schemeId, currentUser))
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

  private Specification<ResourceId, Resource> resourcesBy(ClassId classId) {
    return classId != null ? new ResourcesByClassId(classId)
                           : new FalseSpecification<ResourceId, Resource>();
  }

  private Specification<ResourceId, Resource> resourcesBy(List<ClassId> classIds) {
    List<Specification<ResourceId, Resource>> specifications = Lists.newArrayList();
    for (ClassId classId : classIds) {
      specifications.add(new ResourcesByClassId(classId));
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

  private List<ClassId> classIds(User user) {
    return classDao.getKeys(new TrueSpecification<ClassId, Class>(), user);
  }

  private List<ClassId> classIds(UUID schemeId, User user) {
    return classDao.getKeys(new ClassesBySchemeId(schemeId), user);
  }

  private ClassId classId(ClassId classId, User user) {
    return classDao.exists(classId, user) ? classId : null;
  }

  private List<TextAttributeId> textAttributeIds(User user) {
    return textAttributeDao.getKeys(new TrueSpecification<TextAttributeId, TextAttribute>(), user);
  }

  private List<TextAttributeId> textAttributeIds(UUID schemeId, User user) {
    return textAttributeDao.getKeys(new TextAttributesBySchemeId(schemeId), user);
  }

  private List<TextAttributeId> textAttributeIds(ClassId classId, User user) {
    return textAttributeDao.getKeys(new TextAttributesByClassId(classId), user);
  }

  private List<String> tokenize(String query) {
    return Arrays.asList(query.split("\\s"));
  }

  @Override
  public Resource get(UUID schemeId, String typeId, UUID id, User currentUser) {
    return resourceService.get(new ResourceId(schemeId, typeId, id), currentUser).orNull();
  }

  @Override
  public void post(UUID schemeId, String typeId, List<Resource> resources, User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
      resource.setType(new Class(new Scheme(schemeId), typeId));
    }
    resourceService.save(resources, currentUser);
  }

  @Override
  public Resource post(UUID schemeId, String typeId, Resource resource, User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(new Scheme(schemeId), typeId));
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser).orNull();
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
    return resourceService.get(new ResourceId(resource), currentUser).orNull();
  }

  @Override
  public void post(List<Resource> resources, User currentUser) {
    resourceService.save(resources, currentUser);
  }

  @Override
  public Resource post(Resource resource, User currentUser) {
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser).orNull();
  }

  @Override
  public Resource put(UUID schemeId, String typeId, UUID id, Resource resource, User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(new Scheme(schemeId), typeId));
    resource.setId(id);
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser).orNull();
  }

  @Override
  public void delete(UUID schemeId, String typeId, UUID id, User currentUser) {
    resourceService.delete(new ResourceId(schemeId, typeId, id), currentUser);
  }

}
