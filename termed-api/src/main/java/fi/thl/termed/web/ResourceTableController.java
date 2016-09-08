package fi.thl.termed.web;

import com.google.common.collect.ImmutableMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import org.springframework.http.HttpStatus;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.spesification.SpecificationQuery;
import fi.thl.termed.spesification.resource.AllResources;

import static fi.thl.termed.spesification.SpecificationQuery.Engine.LUCENE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/api/schemes/{schemeId}/resources")
public class ResourceTableController {

  private Exchange<ResourceId, Resource, List<String[]>> exchange;

  public ResourceTableController(Exchange<ResourceId, Resource, List<String[]>> exchange) {
    this.exchange = exchange;
  }

  @RequestMapping(method = POST, consumes = "text/csv;charset=UTF-8")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void importModel(@PathVariable("schemeId") UUID schemeId,
                          @AuthenticationPrincipal User currentUser,
                          HttpServletRequest request) throws IOException {
    List<String[]> rows = new CSVReader(request.getReader()).readAll();
    exchange.save(rows, ImmutableMap.<String, Object>of("schemeId", schemeId), currentUser);
  }

  @RequestMapping(method = GET, produces = "text/csv;charset=UTF-8")
  public void exportModel(@PathVariable("schemeId") UUID schemeId,
                          @AuthenticationPrincipal User currentUser,
                          HttpServletResponse response) throws IOException {
    List<String[]> rows =
        exchange.get(new SpecificationQuery<ResourceId, Resource>(new AllResources(), LUCENE),
                     ImmutableMap.<String, Object>of("schemeId", schemeId),
                     currentUser);
    new CSVWriter(response.getWriter()).writeAll(rows);
  }

}
