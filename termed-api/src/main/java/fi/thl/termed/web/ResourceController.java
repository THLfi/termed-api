package fi.thl.termed.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.ResourceKey;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.repository.spesification.ResourceSpecificationBySchemeAndTypeId;
import fi.thl.termed.repository.spesification.ResourceSpecificationBySchemeId;
import fi.thl.termed.service.ResourceService;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class ResourceController {

  @Autowired
  private ResourceService resourceService;

  @RequestMapping(method = GET, value = "/resources")
  public List<Resource> get(@ModelAttribute Query query,
                            @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                            @AuthenticationPrincipal User currentUser) {
    return !bypassIndex ? resourceService.get(query, currentUser)
                        : resourceService.get(currentUser);
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/resources")
  public List<Resource> getBySchemeId(@PathVariable("schemeId") UUID schemeId,
                                      @ModelAttribute Query query,
                                      @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                      @AuthenticationPrincipal User currentUser) {
    query.setQuery("+(" + emptyToMatchAll(query.getQuery()) + ") +scheme.id:" + schemeId);
    return !bypassIndex ? resourceService.get(query, currentUser)
                        : resourceService.get(new ResourceSpecificationBySchemeId(schemeId),
                                              currentUser);
  }

  @RequestMapping(method = GET, value = "/resources", params = "schemeId")
  public List<Resource> getBySchemeIdAlt(@RequestParam("schemeId") UUID schemeId,
                                         @ModelAttribute Query query,
                                         @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                         @AuthenticationPrincipal User currentUser) {
    return getBySchemeId(schemeId, query, bypassIndex, currentUser);
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/classes/{typeId}/resources")
  public List<Resource> getBySchemeAndTypeId(@PathVariable("schemeId") UUID schemeId,
                                             @PathVariable("typeId") String typeId,
                                             @ModelAttribute Query query,
                                             @RequestParam(value = "bypassIndex", defaultValue = "false") boolean bypassIndex,
                                             @AuthenticationPrincipal User currentUser) {
    query.setQuery("+(" + emptyToMatchAll(query.getQuery()) + ") " +
                   "+scheme.id:" + schemeId + " +type.id:" + typeId);
    return !bypassIndex ? resourceService.get(query, currentUser)
                        : resourceService.get(
                            new ResourceSpecificationBySchemeAndTypeId(schemeId, typeId),
                            currentUser);
  }

  @RequestMapping(method = GET, value = "/resources", params = {"schemeId", "typeId"})
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
  public Resource getById(@ModelAttribute ResourceKey key,
                          @AuthenticationPrincipal User currentUser) {
    return resourceService.get(new ResourceId(key), currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/classes/{typeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch=true")
  public int save(@PathVariable("schemeId") UUID schemeId,
                  @PathVariable("typeId") String typeId,
                  @RequestBody List<Resource> resources,
                  @AuthenticationPrincipal User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
      resource.setType(new Class(typeId));
    }
    return resourceService.save(resources, currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/classes/{typeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch!=true")
  public Resource save(@PathVariable("schemeId") UUID schemeId,
                       @PathVariable("typeId") String typeId,
                       @RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(typeId));
    return resourceService.save(resource, currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch=true")
  public int save(@PathVariable("schemeId") UUID schemeId,
                  @RequestBody List<Resource> resources,
                  @AuthenticationPrincipal User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
    }
    return resourceService.save(resources, currentUser);
  }

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources",
      consumes = "application/json;charset=UTF-8", params = "batch!=true")
  public Resource save(@PathVariable("schemeId") UUID schemeId,
                       @RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    return resourceService.save(resource, currentUser);
  }

  @RequestMapping(method = POST, value = "/resources",
      consumes = "application/json;charset=UTF-8", params = "batch=true")
  public int save(@RequestBody List<Resource> resources,
                  @AuthenticationPrincipal User currentUser) {
    return resourceService.save(resources, currentUser);
  }

  @RequestMapping(method = POST, value = "/resources",
      consumes = "application/json;charset=UTF-8", params = "batch!=true")
  public Resource save(@RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    return resourceService.save(resource, currentUser);
  }

  @RequestMapping(method = PUT, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}",
      consumes = "application/json;charset=UTF-8")
  public Resource save(@ModelAttribute ResourceKey key,
                       @RequestBody Resource resource,
                       @AuthenticationPrincipal User currentUser) {
    return resourceService.save(new ResourceId(key), resource, currentUser);
  }

  @RequestMapping(method = DELETE, value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}")
  public void delete(@ModelAttribute ResourceKey key,
                     @AuthenticationPrincipal User currentUser) {
    resourceService.delete(new ResourceId(key), currentUser);
  }

}
