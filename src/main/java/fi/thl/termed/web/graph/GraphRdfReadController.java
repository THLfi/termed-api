package fi.thl.termed.web.graph;

import static fi.thl.termed.domain.DefaultUris.propertyUri;
import static fi.thl.termed.domain.DefaultUris.uri;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.rdf.RdfResource;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetRdfMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graphs")
public class GraphRdfReadController {

  @Autowired
  private Service<String, Property> propertyService;

  @Autowired
  private Service<GraphId, Graph> graphService;

  @Autowired
  private Map<String, String> defaultNamespacePrefixes;

  @Value("${fi.thl.termed.defaultNamespace:}")
  private String ns;

  @GetRdfMapping
  public Model getAllGraphs(@AuthenticationPrincipal User user) {
    try (
        Stream<Property> props = propertyService.values(new Query<>(new MatchAll<>()), user);
        Stream<Graph> graphs = graphService.values(new Query<>(new MatchAll<>()), user)) {
      return toModel(props.collect(toList()), graphs.collect(toList()));
    }
  }

  @GetRdfMapping("/{graphId}")
  public Model getGraphById(@PathVariable("graphId") UUID graphId,
      @AuthenticationPrincipal User user) {
    try (Stream<Property> props = propertyService.values(new Query<>(new MatchAll<>()), user)) {
      return toModel(props.collect(toList()), singletonList(
          graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new)));
    }
  }

  private Model toModel(List<Property> properties, List<Graph> graphs) {
    Map<String, Property> propertyMap = properties.stream()
        .collect(Collectors.toMap(Property::identifier, p -> p));
    List<RdfResource> rdfResources = new ArrayList<>();

    for (Graph graph : graphs) {
      RdfResource rdfResource = new RdfResource(graph.getUri().orElse(uri(ns, graph.identifier())));

      rdfResource.addLiteral(propertyUri(ns, "id"), "", graph.getId().toString());
      graph.getCode().ifPresent(code -> rdfResource.addLiteral(propertyUri(ns, "code"), "", code));
      graph.getUri().ifPresent(uri -> rdfResource.addLiteral(propertyUri(ns, "uri"), "", uri));

      for (Map.Entry<String, LangValue> entry : graph.getProperties().entries()) {
        LangValue langValue = entry.getValue();
        String attributeUri = propertyMap.get(entry.getKey()).getUri()
            .orElse(propertyUri(ns, entry.getKey()));
        rdfResource.addLiteral(attributeUri, langValue.getLang(), langValue.getValue());
      }

      rdfResources.add(rdfResource);
    }

    Model model = new JenaRdfModel(ModelFactory.createDefaultModel()).save(rdfResources).getModel();
    model.setNsPrefixes(defaultNamespacePrefixes);
    return model;
  }

}
