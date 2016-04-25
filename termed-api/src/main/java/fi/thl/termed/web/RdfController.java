package fi.thl.termed.web;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.Service;
import fi.thl.termed.util.rdf.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModelToResources;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api")
public class RdfController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @javax.annotation.Resource
  private Service<UUID, Scheme> schemeService;

  @javax.annotation.Resource
  private Service<ResourceId, Resource> resourceService;

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources",
      consumes = {"application/n-triples;charset=UTF-8",
                  "application/rdf+xml;charset=UTF-8",
                  "text/turtle;charset=UTF-8",
                  "text/n3;charset=UTF-8"},
      produces = "text/turtle;charset=UTF-8")
  public Model save(@PathVariable("schemeId") UUID schemeId,
                    @RequestBody Model model,
                    @AuthenticationPrincipal User currentUser) {

    Scheme scheme = schemeService.get(schemeId, currentUser);

    log.info("Parsing RDF-model");
    List<Resource> resources = new RdfModelToResources(scheme).apply(new JenaRdfModel(model));

    resourceService.save(resources, currentUser);

    // returns an empty model
    return ModelFactory.createDefaultModel();
  }

}
