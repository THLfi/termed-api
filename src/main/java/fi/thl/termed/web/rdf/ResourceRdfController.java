package fi.thl.termed.web.rdf;

import com.hp.hpl.jena.rdf.model.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import fi.thl.termed.domain.User;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping(value = "/api/schemes/{schemeId}/resources")
public class ResourceRdfController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @RequestMapping(method = POST, consumes = {"application/n-triples;charset=UTF-8",
                                             "application/rdf+xml;charset=UTF-8",
                                             "text/turtle;charset=UTF-8",
                                             "text/n3;charset=UTF-8"})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void importModel(@PathVariable("schemeId") UUID schemeId,
                          @RequestBody Model model,
                          @AuthenticationPrincipal User currentUser) {
    log.info("Importing RDF-model");
  }

  @RequestMapping(method = GET, produces = {"application/n-triples;charset=UTF-8",
                                            "application/rdf+xml;charset=UTF-8",
                                            "text/turtle;charset=UTF-8",
                                            "text/n3;charset=UTF-8"})
  public Model exportModel(@PathVariable("schemeId") UUID schemeId,
                           @AuthenticationPrincipal User currentUser) {
    log.info("Exporting RDF-model");
    return null;
  }

}
