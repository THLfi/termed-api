package fi.thl.termed.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.util.dao.Dao;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeAndResources;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.TrueSpecification;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

@RequestMapping(value = "/api")
public class DumpAndRestoreController {

  private Service<UUID, SchemeAndResources> schemeAndResourcesService;
  private Dao<UUID, Scheme> schemeDao;

  public DumpAndRestoreController(
      Service<UUID, SchemeAndResources> schemeAndResourcesService,
      Dao<UUID, Scheme> schemeDao) {
    this.schemeAndResourcesService = schemeAndResourcesService;
    this.schemeDao = schemeDao;
  }

  @RequestMapping(method = GET, value = "/dump", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<SchemeAndResources> get(@AuthenticationPrincipal User user) {
    return schemeAndResourcesService.get(
        schemeDao.getKeys(new TrueSpecification<UUID, Scheme>(), user), user);
  }

  @RequestMapping(method = GET, value = "/dump/{schemeId}", produces = "application/json;charset=UTF-8")
  @ResponseBody
  public SchemeAndResources get(@PathVariable UUID schemeId,
                                @AuthenticationPrincipal User user) {
    return schemeAndResourcesService.get(schemeId, user).orNull();
  }

  @RequestMapping(method = PUT, value = "/restore/{schemeId}", consumes = "application/json;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@PathVariable UUID schemeId,
                   @RequestBody SchemeAndResources schemeAndResources,
                   @AuthenticationPrincipal User user) {
    Scheme scheme = schemeAndResources.getScheme();
    scheme.setId(schemeId);

    List<Resource> resources = schemeAndResources.getResources();
    for (Resource resource : resources) {
      resource.setScheme(new Scheme(schemeId));
    }

    schemeAndResourcesService.save(schemeAndResources, user);
  }

  @RequestMapping(method = POST, value = "/restore", params = "batch!=true",
      consumes = "application/json;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@RequestBody SchemeAndResources schemeAndResources,
                   @AuthenticationPrincipal User user) {
    schemeAndResourcesService.save(schemeAndResources, user);
  }

  @RequestMapping(method = POST, value = "/restore", params = "batch=true",
      consumes = "application/json;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void save(@RequestBody List<SchemeAndResources> schemeAndResourcesList,
                   @AuthenticationPrincipal User user) {
    schemeAndResourcesService.save(schemeAndResourcesList, user);
  }

}
