package fi.thl.termed.web.rdf;

import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.class_.specification.ClassesBySchemeId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.PostRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;

import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("/api/schemes/{schemeId}/resources")
public class ResourceRdfWriteController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @Autowired
  private Service<ClassId, Class> classService;

  @Autowired
  private Service<ResourceId, Resource> resourceService;

  @PostRdfMapping(produces = {})
  @ResponseStatus(NO_CONTENT)
  private void post(@PathVariable("schemeId") UUID schemeId,
                    @AuthenticationPrincipal User currentUser,
                    @RequestBody Model model) {
    log.info("Importing RDF-model {} (user: {})", schemeId, currentUser.getUsername());
    schemeService.get(new SchemeId(schemeId), currentUser).orElseThrow(NotFoundException::new);
    List<Resource> resources = new RdfModelToResources(
        classService.get(new Query<>(new ClassesBySchemeId(schemeId)), currentUser))
        .apply(new JenaRdfModel(model));
    resourceService.save(resources, currentUser);
  }

}
