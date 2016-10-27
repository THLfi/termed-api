package fi.thl.termed.web.rdf;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.specification.MatchAll;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;

@RestController
@RequestMapping("/api/schemes")
public class SchemesRdfReadController {

  @Autowired
  private Service<SchemeId, Scheme> schemeService;

  @Autowired
  private Service<String, Property> propertyService;

  @GetRdfMapping
  public Model get(@AuthenticationPrincipal User currentUser) {
    List<Scheme> schemes = schemeService.get(new Query<>(new MatchAll<>()), currentUser);
    List<Property> properties = propertyService.get(new Query<>(new MatchAll<>()), currentUser);
    return new JenaRdfModel(new SchemesToRdfModel(properties).apply(schemes)).getModel();
  }

}
