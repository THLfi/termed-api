package fi.thl.termed.web;

import com.hp.hpl.jena.rdf.model.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import fi.thl.termed.domain.User;
import fi.thl.termed.service.ResourceRdfService;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api")
public class RdfController {

  @Autowired
  private ResourceRdfService rdfService;

  @RequestMapping(method = POST, value = "/schemes/{schemeId}/resources",
      consumes = {"application/n-triples;charset=UTF-8",
                  "application/rdf+xml;charset=UTF-8",
                  "text/turtle;charset=UTF-8",
                  "text/n3;charset=UTF-8"},
      produces = "text/turtle;charset=UTF-8")
  public Model save(@PathVariable("schemeId") UUID key,
                    @RequestBody Model value,
                    @AuthenticationPrincipal User currentUser) {
    return rdfService.save(key, value, currentUser);
  }

}
