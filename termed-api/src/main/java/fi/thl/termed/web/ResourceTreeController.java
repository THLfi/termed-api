package fi.thl.termed.web;

import com.google.common.collect.ImmutableMap;

import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.Exporter;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RequestMapping(value = "/api/schemes/{schemeId}/trees")
public class ResourceTreeController {

  private Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter;

  public ResourceTreeController(
      Exporter<ResourceId, Resource, List<Resource>> resourceTreeExporter) {
    this.resourceTreeExporter = resourceTreeExporter;
  }

  @RequestMapping(method = GET, produces = "application/json;charset=UTF-8")
  @ResponseBody
  public List<Resource> getTrees(
      @PathVariable("schemeId") UUID schemeId,
      @RequestParam(value = "attributeId", defaultValue = "narrower") String attributeId,
      @RequestParam(value = "referrers", defaultValue = "false") boolean referrers,
      @AuthenticationPrincipal User user) {
    Query rootsQuery = new Query(format("+scheme.id:%s -%s%s.id:[* TO *]",
                                        schemeId, referrers ? "" : "referrers.", attributeId), -1);
    Map<String, Object> args = ImmutableMap.<String, Object>of(
        "attributeId", attributeId,
        "referrers", referrers);
    return resourceTreeExporter.get(rootsQuery, args, user);
  }

}
