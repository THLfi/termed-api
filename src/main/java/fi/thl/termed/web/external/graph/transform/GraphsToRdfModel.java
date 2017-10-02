package fi.thl.termed.web.external.graph.transform;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.jena.rdf.model.ModelFactory;

public class GraphsToRdfModel implements Function<List<Graph>, RdfModel> {

  private static final String TERMED_NS = "http://termed.thl.fi/api/";
  private static final String TERMED_PROPERTIES_NS = TERMED_NS + "properties/";
  private static final String TERMED_GRAPHS_NS = TERMED_NS + "graphs/";

  private Map<String, Property> properties = Maps.newHashMap();

  public GraphsToRdfModel(List<Property> propertyList) {
    propertyList.forEach(p -> properties.put(p.getId(), p));
  }

  @Override
  public RdfModel apply(List<Graph> graphs) {
    List<RdfResource> rdfResources = Lists.newArrayList();

    for (Graph graph : graphs) {
      RdfResource rdfResource = new RdfResource(getGraphUri(graph));

      for (Map.Entry<String, LangValue> entry : graph.getProperties().entries()) {
        LangValue langValue = entry.getValue();
        String attributeUri = getPropertyUri(properties.get(entry.getKey()));
        rdfResource.addLiteral(attributeUri, langValue.getLang(), langValue.getValue());
      }

      rdfResources.add(rdfResource);
    }

    return new JenaRdfModel(ModelFactory.createDefaultModel()).save(rdfResources);
  }

  private String getPropertyUri(Property property) {
    return firstNonNull(emptyToNull(property.getUri()),
        TERMED_PROPERTIES_NS + property.getId());
  }

  private String getGraphUri(Graph graph) {
    return firstNonNull(emptyToNull(graph.getUri().orElse(null)),
        TERMED_GRAPHS_NS + graph.getId());
  }

}
