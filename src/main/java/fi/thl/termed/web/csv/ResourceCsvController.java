package fi.thl.termed.web.csv;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.resource.specification.ResourcesBySchemeId;
import fi.thl.termed.util.TableUtils;
import fi.thl.termed.util.json.JsonUtils;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;

import static fi.thl.termed.util.specification.Query.Engine.LUCENE;

@RestController
@RequestMapping("/api/schemes/{schemeId}/resources")
public class ResourceCsvController {

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @Autowired
  private Gson gson;

  @PostMapping(consumes = "text/csv;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void post(@PathVariable("schemeId") UUID schemeId,
                   @AuthenticationPrincipal User currentUser,
                   HttpServletRequest request) throws IOException {
    List<String[]> rows = new CSVReader(request.getReader()).readAll();
    List<Resource> resources = Lists.newArrayList();
    for (Map<String, String> row : TableUtils.toMapped(rows)) {
      resources.add(gson.fromJson(JsonUtils.unflatten(row), Resource.class));
    }
    resources.forEach(r -> r.setScheme(new Scheme(schemeId)));
    resourceService.save(resources, currentUser);
  }

  @GetMapping(produces = "text/csv;charset=UTF-8")
  public void get(@PathVariable("schemeId") UUID schemeId,
                  @AuthenticationPrincipal User currentUser,
                  HttpServletResponse response) throws IOException {
    List<Resource> resources = resourceService.get(
        new Query<>(new ResourcesBySchemeId(schemeId), LUCENE), currentUser);
    List<Map<String, String>> rows = Lists.newArrayList();
    resources.forEach(r -> rows.add(JsonUtils.flatten(gson.toJsonTree(r, Resource.class))));
    new CSVWriter(response.getWriter()).writeAll(TableUtils.toTable(rows));
  }

}
