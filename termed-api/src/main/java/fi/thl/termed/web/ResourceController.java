package fi.thl.termed.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.ResourceKey;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.spesification.sql.ResourcesBySchemeId;
import fi.thl.termed.spesification.sql.ResourcesBySchemeIdAndTypeId;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class ResourceController {

  @SuppressWarnings("all")
  private Logger log = LoggerFactory.getLogger(getClass());

  private Service<ResourceId, Resource> resourceService;

  public ResourceController(Service<ResourceId, Resource> resourceService) {
    this.resourceService = resourceService;
  }

  @RequestMapping(method = GET, value = "/resources")
  @ResponseBody
  public List<Resource> get(@ModelAttribute Query query,
                            @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                            @AuthenticationPrincipal User currentUser) {
    return !bypassIndex ? resourceService.get(query, currentUser)
                        : resourceService.get(currentUser);
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/resources")
  @ResponseBody
  public List<Resource> getBySchemeId(@PathVariable("schemeId") UUID schemeId,
                                      @ModelAttribute Query query,
                                      @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                      @AuthenticationPrincipal User currentUser) {
    query.setQuery("+(" + emptyToMatchAll(query.getQuery()) + ") +scheme.id:" + schemeId);
    return !bypassIndex ? resourceService.get(query, currentUser)
                        : resourceService.get(new ResourcesBySchemeId(schemeId), currentUser);
  }

  @RequestMapping(method = GET, value = "/resources", params = "schemeId")
  @ResponseBody
  public List<Resource> getBySchemeIdAlt(@RequestParam("schemeId") UUID schemeId,
                                         @ModelAttribute Query query,
                                         @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                         @AuthenticationPrincipal User currentUser) {
    return getBySchemeId(schemeId, query, bypassIndex, currentUser);
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/classes/{typeId}/resources")
  @ResponseBody
  public List<Resource> getBySchemeAndTypeId(@PathVariable("schemeId") UUID schemeId,
                                             @PathVariable("typeId") String typeId,
                                             @ModelAttribute Query query,
                                             @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                             @AuthenticationPrincipal User currentUser) {
    query.setQuery("+(" + emptyToMatchAll(query.getQuery()) + ") " +
                   "+scheme.id:" + schemeId + " +type.id:" + typeId);
    return !bypassIndex ? resourceService.get(query, currentUser)
                        : resourceService.get(new ResourcesBySchemeIdAndTypeId(schemeId, typeId),
                                              currentUser);
  }

  @RequestMapping(method = GET, value = "/resources", params = {"schemeId", "typeId"})
  @ResponseBody
  public List<Resource> getBySchemeAndTypeIdAlt(@RequestParam("schemeId") UUID schemeId,
                                                @RequestParam("typeId") String typeId,
                                                @ModelAttribute Query query,
                                                @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                                @AuthenticationPrincipal User currentUser) {
    return getBySchemeAndTypeId(schemeId, typeId, query, bypassIndex, currentUser);
  }

  private String emptyToMatchAll(String query) {
    return query.isEmpty() ? "*:*" : query;
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}")
  @ResponseBody
  public Resource getById(@ModelAttribute ResourceKey key,
                          @AuthenticationPrincipal User currentUser) {
    return resourceService.get(new ResourceId(key), currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/classes/{typeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void save(@PathVariable("schemeId") UUID schemeId,
                   @PathVariable("typeId") String typeId,
                   @RequestBody List<Resource> resources,
                   @AuthenticationPrincipal User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
      resource.setType(new Class(typeId));
    }
    resourceService.save(resources, currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/classes/{typeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch!=true")
  @ResponseBody
  public Resource save(@PathVariable("schemeId") UUID schemeId,
                       @PathVariable("typeId") String typeId,
                       @RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(typeId));
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void save(@PathVariable("schemeId") UUID schemeId,
                   @RequestBody List<Resource> resources,
                   @AuthenticationPrincipal User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
    }
    resourceService.save(resources, currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch!=true")
  @ResponseBody
  public Resource save(@PathVariable("schemeId") UUID schemeId,
                       @RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);

  }

  @RequestMapping(method = POST, value = "/resources",
      consumes = "application/json;charset=UTF-8", params = "batch=true")
  @ResponseStatus(NO_CONTENT)
  public void save(@RequestBody List<Resource> resources,
                   @AuthenticationPrincipal User currentUser) {
    resourceService.save(resources, currentUser);
  }

  @RequestMapping(method = POST, value = "/resources",
      consumes = "application/json;charset=UTF-8", params = "batch!=true")
  @ResponseBody
  public Resource save(@RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @RequestMapping(method = PUT, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}",
      consumes = "application/json;charset=UTF-8")
  @ResponseBody
  public Resource save(@ModelAttribute ResourceKey key,
                       @RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(key.getSchemeId()));
    resource.setType(new Class(key.getTypeId()));
    resource.setId(key.getId());
    resourceService.save(resource, currentUser);
    return resourceService.get(new ResourceId(resource), currentUser);
  }

  @RequestMapping(method = DELETE, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(@ModelAttribute ResourceKey key,
                     @AuthenticationPrincipal User currentUser) {
    resourceService.delete(new ResourceId(key), currentUser);
  }

}
