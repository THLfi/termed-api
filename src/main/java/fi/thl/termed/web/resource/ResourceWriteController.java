package fi.thl.termed.web.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.annotation.PutJsonMapping;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping(value = "/api")
public class ResourceWriteController {

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @PostJsonMapping(path = "/schemes/{schemeId}/classes/{typeId}/resources", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @RequestBody List<Resource> resources,
      @AuthenticationPrincipal User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
      resource.setType(new Class(new Scheme(schemeId), typeId));
    }
    resourceService.save(resources, currentUser);
  }

  @PostJsonMapping(path = "/schemes/{schemeId}/classes/{typeId}/resources", params = "batch!=true", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Resource post(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(new Scheme(schemeId), typeId));
    ResourceId resourceId = resourceService.save(resource, currentUser);
    return resourceService.get(resourceId, currentUser).get();
  }

  @PostJsonMapping(path = "/schemes/{schemeId}/resources", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @PathVariable("schemeId") UUID schemeId,
      @RequestBody List<Resource> resources,
      @AuthenticationPrincipal User currentUser) {
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
    }
    resourceService.save(resources, currentUser);
  }

  @PostJsonMapping(path = "/schemes/{schemeId}/resources", params = "batch!=true", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Resource post(
      @PathVariable("schemeId") UUID schemeId,
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    ResourceId resourceId = resourceService.save(resource, currentUser);
    return resourceService.get(resourceId, currentUser).get();
  }

  @PostJsonMapping(path = "/resources", params = "batch=true", produces = {})
  @ResponseStatus(NO_CONTENT)
  public void post(
      @RequestBody List<Resource> resources,
      @AuthenticationPrincipal User currentUser) {
    resourceService.save(resources, currentUser);
  }

  @PostJsonMapping(path = "/resources", params = "batch!=true", produces = {})
  public Resource post(
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    ResourceId resourceId = resourceService.save(resource, currentUser);
    return resourceService.get(resourceId, currentUser).get();
  }

  @PutJsonMapping(path = "/schemes/{schemeId}/classes/{typeId}/resources/{id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public Resource put(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @RequestBody Resource resource,
      @AuthenticationPrincipal User currentUser) {
    resource.setScheme(new Scheme(schemeId));
    resource.setType(new Class(new Scheme(schemeId), typeId));
    resource.setId(id);
    ResourceId resourceId = resourceService.save(resource, currentUser);
    return resourceService.get(resourceId, currentUser).get();
  }

  @DeleteMapping("/schemes/{schemeId}/classes/{typeId}/resources/{id}")
  @ResponseStatus(NO_CONTENT)
  public void delete(
      @PathVariable("schemeId") UUID schemeId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User currentUser) {
    resourceService.delete(new ResourceId(schemeId, typeId, id), currentUser);
  }


}
