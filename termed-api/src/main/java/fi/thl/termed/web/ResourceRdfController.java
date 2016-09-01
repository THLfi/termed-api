package fi.thl.termed.web;

import com.google.common.collect.ImmutableMap;

import com.hp.hpl.jena.rdf.model.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

import fi.thl.termed.domain.Query;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.User;
import fi.thl.termed.exchange.Exchange;
import fi.thl.termed.util.rdf.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModel;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RequestMapping(value = "/api/schemes/{schemeId}/resources")
public class ResourceRdfController {

  private Logger log = LoggerFactory.getLogger(getClass());

  private Exchange<ResourceId, Resource, RdfModel> rdfExchange;

  public ResourceRdfController(Exchange<ResourceId, Resource, RdfModel> rdfExchange) {
    this.rdfExchange = rdfExchange;
  }

  @RequestMapping(method = POST, consumes = {"application/n-triples;charset=UTF-8",
                                             "application/rdf+xml;charset=UTF-8",
                                             "text/turtle;charset=UTF-8",
                                             "text/n3;charset=UTF-8"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void importModel(@PathVariable("schemeId") UUID schemeId,
                          @RequestBody Model model,
                          @AuthenticationPrincipal User currentUser) {
    log.info("Importing RDF-model");
    rdfExchange.save(new JenaRdfModel(model),
                     ImmutableMap.<String, Object>of("schemeId", schemeId),
                     currentUser);
  }

  @RequestMapping(method = GET, produces = {"application/n-triples;charset=UTF-8",
                                            "application/rdf+xml;charset=UTF-8",
                                            "text/turtle;charset=UTF-8",
                                            "text/n3;charset=UTF-8"})
  @ResponseBody
  public Model exportModel(@PathVariable("schemeId") UUID schemeId,
                           @AuthenticationPrincipal User currentUser) {
    log.info("Exporting RDF-model");
    RdfModel rdfModel = rdfExchange.get(new Query("+scheme.id:" + schemeId, -1),
                                        ImmutableMap.<String, Object>of("schemeId", schemeId),
                                        currentUser);
    return new JenaRdfModel(rdfModel).getModel();
  }

}
