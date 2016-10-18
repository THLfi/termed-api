package fi.thl.termed.web.rdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.jena.rdf.model.ModelFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Property;
import fi.thl.termed.domain.Scheme;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

public class SchemesToRdfModel implements Function<List<Scheme>, RdfModel> {

  private static final String TERMED_NS = "http://termed.thl.fi/api/";
  private static final String TERMED_PROPERTIES_NS = TERMED_NS + "properties/";
  private static final String TERMED_SCHEMES_NS = TERMED_NS + "schemes/";

  private Map<String, Property> properties = Maps.newHashMap();

  public SchemesToRdfModel(List<Property> propertyList) {
    propertyList.forEach(p -> properties.put(p.getId(), p));
  }

  @Override
  public RdfModel apply(List<Scheme> schemes) {
    List<RdfResource> rdfResources = Lists.newArrayList();

    for (Scheme scheme : schemes) {
      RdfResource rdfResource = new RdfResource(getSchemeUri(scheme));

      for (Map.Entry<String, LangValue> entry : scheme.getProperties().entries()) {
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

  private String getSchemeUri(Scheme scheme) {
    return firstNonNull(emptyToNull(scheme.getUri()),
                        TERMED_SCHEMES_NS + scheme.getId());
  }

}
