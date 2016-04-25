package fi.thl.termed.web;

import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.JsTree;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceKey;
import fi.thl.termed.domain.ResourceTree;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.ResourceGraphService;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(value = "/api", produces = "application/json;charset=UTF-8")
public class ResourceGraphController {

  @javax.annotation.Resource
  private ResourceGraphService resourceGraphService;

  @RequestMapping(method = GET,
      value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}/paths/{attributeId}")
  public List<List<Resource>> findPaths(@ModelAttribute ResourceKey key,
                                        @PathVariable("attributeId") String attributeId,
                                        @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
                                        @AuthenticationPrincipal User user) {
    return !referrers ? resourceGraphService.findPaths(key, attributeId, user)
                      : resourceGraphService.findReferrerPaths(key, attributeId, user);
  }

  @RequestMapping(method = GET,
      value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}/trees/{attributeId}")
  public ResourceTree getTree(@ModelAttribute ResourceKey key,
                              @PathVariable("attributeId") String attributeId,
                              @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
                              @AuthenticationPrincipal User user) {
    return !referrers ? resourceGraphService.getTree(key, attributeId, user)
                      : resourceGraphService.getReferrerTree(key, attributeId, user);
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/trees/{attributeId}")
  public List<ResourceTree> getTrees(@PathVariable("schemeId") UUID schemeId,
                                     @PathVariable("attributeId") String attributeId,
                                     @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
                                     @AuthenticationPrincipal User user) {
    return !referrers ? resourceGraphService.getTrees(schemeId, attributeId, user)
                      : resourceGraphService.getReferrerTrees(schemeId, attributeId, user);
  }

  @RequestMapping(method = GET, value = "/schemes/{schemeId}/trees/{attributeId}", params = "jstree=true")
  public List<JsTree> getJsTrees(@PathVariable("schemeId") UUID schemeId,
                                 @PathVariable("attributeId") String attributeId,
                                 @RequestParam(value = "lang", defaultValue = "fi") String lang,
                                 @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
                                 @AuthenticationPrincipal User user) {
    return !referrers ? resourceGraphService.getJsTrees(schemeId, attributeId, lang, user)
                      : resourceGraphService.getJsReferrerTrees(schemeId, attributeId, lang, user);
  }

  @RequestMapping(method = GET,
      value = "/schemes/{schemeId}/classes/{typeId}/resources/{id}/context/{attributeId}")
  public List<ResourceTree> getContextTrees(@ModelAttribute ResourceKey key,
                                            @PathVariable("attributeId") String attributeId,
                                            @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
                                            @AuthenticationPrincipal User user) {
    return !referrers ? resourceGraphService.getContextTrees(key, attributeId, user)
                      : resourceGraphService.getContextReferrerTrees(key, attributeId, user);
  }

  @RequestMapping(method = GET,
      value = "/schemes/{schemeId}/classes/{typeId}/resources/{resourceId}/context/{attributeId}", params = "jstree=true")
  public List<JsTree> getContextJsTrees(@PathVariable("schemeId") UUID schemeId,
                                        @PathVariable("typeId") String typeId,
                                        @PathVariable("resourceId") UUID resourceId,
                                        @PathVariable("attributeId") String attrId,
                                        @RequestParam(value = "lang", defaultValue = "fi") String lang,
                                        @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
                                        @AuthenticationPrincipal User user) {
    ResourceKey key = new ResourceKey(schemeId, typeId, resourceId);
    return !referrers ? resourceGraphService.getContextJsTrees(key, attrId, lang, user)
                      : resourceGraphService.getContextJsReferrerTrees(key, attrId, lang, user);
  }

}
