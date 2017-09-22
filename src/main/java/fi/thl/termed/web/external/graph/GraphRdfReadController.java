package fi.thl.termed.web.external.graph;

import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.web.external.graph.transform.GraphsToRdfModel;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs")
public class GraphRdfReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Service<String, Property> propertyService;

  @GetRdfMapping
  public Model get(@AuthenticationPrincipal User currentUser) {
    List<Graph> graphs = graphService.getValues(currentUser).collect(toList());
    List<Property> properties = propertyService.getValues(currentUser).collect(toList());
    return new JenaRdfModel(new GraphsToRdfModel(properties).apply(graphs)).getModel();
  }

}
