package fi.thl.termed.web.external.graph.rdf;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;

@RestController
@RequestMapping("/api/graphs")
public class GraphRdfReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<String, Property> propertyService;

  @GetRdfMapping
  public Model get(@AuthenticationPrincipal User currentUser) {
    List<Graph> graphs = graphService.get(currentUser);
    List<Property> properties = propertyService.get(currentUser);
    return new JenaRdfModel(new GraphsToRdfModel(properties).apply(graphs)).getModel();
  }

}
