package fi.thl.termed.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.dao.Dao;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.permission.PermissionEvaluator;
import fi.thl.termed.service.Service;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequestMapping(value = "/api")
public class ResourceControllerSpringImpl extends ResourceControllerImpl
    implements ResourceController {

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  public ResourceControllerSpringImpl(
      Service<ResourceId, Resource> resourceService,
      Dao<UUID, Scheme> schemeDao,
      Dao<TextAttributeId, TextAttribute> textAttributeDao,
      PermissionEvaluator<UUID> schemePermissionEvaluator,
      PermissionEvaluator<ClassId> classPermissionEvaluator,
      PermissionEvaluator<TextAttributeId> textAttributeEvaluator) {
    super(resourceService, schemeDao, textAttributeDao, schemePermissionEvaluator,
          classPermissionEvaluator, textAttributeEvaluator);
  }

  @Override
  @RequestMapping(method = GET, value = "/resources", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Resource> get(
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {
    return super.get(orderBy, max, bypassIndex, currentUser);
  }

  @Override
  @RequestMapping(method = GET, value = "/resources", params = "query", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Resource> get(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {
    return super.get(query, orderBy, max, bypassIndex, currentUser);
  }

  @Override
  @RequestMapping(method = GET, value = "/schemes/{schemeId}/resources", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Resource> get(
      @PathVariable("schemeId") UUID schemeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {
    return super.get(schemeId, query, orderBy, max, bypassIndex, currentUser);
  }

  @Override
  @RequestMapping(method = GET, value = "/schemes/{schemeId}/classes/{typeId}/resources", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Resource> get(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "orderBy", required = false, defaultValue = "") List<String> orderBy,
      @RequestParam(value = "max", required = false, defaultValue = "50") int max,
      @RequestParam(value = "bypassIndex", required = false, defaultValue = "false") boolean bypassIndex,
      @AuthenticationPrincipal User currentUser) {
    return super.get(schemeId, typeId, query, orderBy, max, bypassIndex, currentUser);
  }

  @Override
  @RequestMapping(method = GET, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public Resource get(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User currentUser) {
    return super.get(schemeId, typeId, id, currentUser);
  }

  @Override
  @RequestMapping(method = POST, value = "/schemes/{schemeId}/classes/{typeId}/resources", params = "batch=true", consumes = "application/json;charset=UTF-8")
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @RequestBody List<Resource> resources,
      @AuthenticationPrincipal User currentUser) {
    super.post(schemeId, typeId, resources, currentUser);
  }

  @Override
  @RequestMapping(method = POST, value = "/schemes/{schemeId}/classes/{typeId}/resources", params = "batch!=true", consumes = "application/json;charset=UTF-8")
  @ResponseBody
  public Resource post(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    return super.post(schemeId, typeId, resource, currentUser);
  }

  @Override
  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources", params = "batch=true", consumes = "application/json;charset=UTF-8")
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("schemeId") UUID schemeId,
      @RequestBody List<Resource> resources,
      @AuthenticationPrincipal User currentUser) {
    super.post(schemeId, resources, currentUser);
  }

  @Override
  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources", params = "batch!=true", consumes = "application/json;charset=UTF-8")
  @ResponseBody
  public Resource post(
      @PathVariable("schemeId") UUID schemeId,
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    return super.post(schemeId, resource, currentUser);
  }

  @Override
  @RequestMapping(method = POST, value = "/resources", params = "batch=true", consumes = "application/json;charset=UTF-8")
  @ResponseStatus(NO_CONTENT)
  public void post(
      @RequestBody List<Resource> resources,
      @AuthenticationPrincipal User currentUser) {
    super.post(resources, currentUser);
  }

  @Override
  @RequestMapping(method = POST, value = "/resources", params = "batch!=true", consumes = "application/json;charset=UTF-8")
  @ResponseBody
  public Resource post(
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    return super.post(resource, currentUser);
  }

  @Override
  @RequestMapping(method = PUT, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}", consumes = "application/json;charset=UTF-8")
  @ResponseBody
  public Resource put(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    return super.put(schemeId, typeId, id, resource, currentUser);
  }

  @Override
  @RequestMapping(method = DELETE, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User currentUser) {
    super.delete(schemeId, typeId, id, currentUser);
  }

}
