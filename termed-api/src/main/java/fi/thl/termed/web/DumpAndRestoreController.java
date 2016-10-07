package fi.thl.termed.web;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeAndResources;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.Specification;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.SpecificationQuery.Engine;
import fi.thl.termed.spesification.resource.ResourcesByClassId;
import fi.thl.termed.spesification.sql.ClassesBySchemeId;
import fi.thl.termed.spesification.util.OrSpecification;
import fi.thl.termed.spesification.util.TrueSpecification;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequestMapping(value = "/api")
public class DumpAndRestoreController {

  private Service<ResourceId, Resource> resourceService;
  private Service<UUID, Scheme> schemeService;
  private Dao<ClassId, Class> classDao;

  public DumpAndRestoreController(
      Service<ResourceId, Resource> resourceService,
      Service<UUID, Scheme> schemeService,
      Dao<ClassId, Class> classDao) {
    this.resourceService = resourceService;
    this.schemeService = schemeService;
    this.classDao = classDao;
  }

  @RequestMapping(method = GET, value = "/dump", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<SchemeAndResources> get(@AuthenticationPrincipal User user) {
    List<SchemeAndResources> all = Lists.newArrayList();

    List<Scheme> schemes = schemeService.get(
        new SpecificationQuery<UUID, Scheme>(new TrueSpecification<UUID, Scheme>()), user);

    for (Scheme scheme : schemes) {
      List<Resource> resources = resourceService.get(
          schemeResourcesQuery(scheme.getId(), user), user);
      all.add(new SchemeAndResources(scheme, resources));
    }

    return all;
  }

  @RequestMapping(method = GET, value = "/dump/{schemeId}", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public SchemeAndResources get(@PathVariable UUID schemeId,
                                @AuthenticationPrincipal User user) {
    Optional<Scheme> scheme = schemeService.get(schemeId, user);

    if (scheme.isPresent()) {
      List<Resource> resources = resourceService.get(schemeResourcesQuery(schemeId, user), user);
      return new SchemeAndResources(scheme.get(), resources);
    }

    return null;
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
        Collections.singletonList("id"), Integer.MAX_VALUE, Engine.LUCENE);
  }

  @RequestMapping(method = PUT, value = "/restore/{schemeId}", consumes = "application/json;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@PathVariable UUID schemeId,
                   @RequestBody SchemeAndResources schemeAndResources,
                   @AuthenticationPrincipal User user) {

    Scheme scheme = schemeAndResources.getScheme();
    scheme.setId(schemeId);
    schemeService.save(scheme, user);

    List<Resource> resources = schemeAndResources.getResources();
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
    }
    resourceService.save(resources, user);
  }

  @RequestMapping(method = POST, value = "/restore", params = "batch!=true",
      consumes = "application/json;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@RequestBody SchemeAndResources schemeAndResources,
                   @AuthenticationPrincipal User user) {
    schemeService.save(schemeAndResources.getScheme(), user);
    resourceService.save(schemeAndResources.getResources(), user);
  }

  @RequestMapping(method = POST, value = "/restore", params = "batch=true",
      consumes = "application/json;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@RequestBody List<SchemeAndResources> all,
                   @AuthenticationPrincipal User user) {
    for (SchemeAndResources schemeAndResources : all) {
      schemeService.save(schemeAndResources.getScheme(), user);
      resourceService.save(schemeAndResources.getResources(), user);
    }
  }

}
