package fi.thl.termed.exchange.rdf;

import com.google.common.base.Ascii;

import java.util.Objects;
import java.util.function.Function;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.hp.hpl.jena.vocabulary.RDF;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;
import fi.thl.termed.util.rdf.URIs;

import static com.google.common.base.Functions.forMap;
import static com.google.common.base.Predicates.in;
import static com.google.common.collect.Lists.transform;
import static fi.thl.termed.util.collect.ListUtils.filter;

/**
 * Function to transform rdf model into list of resources conforming to provided scheme.
 */
public class RdfModelToResources implements Function<RdfModel, List<Resource>> {

  private Scheme scheme;

  public RdfModelToResources(Scheme scheme) {
    this.scheme = scheme;
  }

  @Override
  public List<Resource> apply(RdfModel rdfModel) {
    Map<String, Resource> resources = Maps.newLinkedHashMap();

    // init resources
    for (Class type : scheme.getClasses()) {
      for (RdfResource r : rdfModel.find(RDF.type.getURI(), type.getUri())) {
        Resource resource = new Resource();
        resource.setCode(removeDiacritics(URIs.localName(r.getUri())));
        resource.setUri(r.getUri());
        resource.setScheme(new Scheme(scheme.getId()));
        resource.setType(new Class(new ClassId(type)));
        resources.put(r.getUri(), resource);
      }
    }

    // populate attributes
    for (Class type : scheme.getClasses()) {
      for (RdfResource rdfResource : rdfModel.find(RDF.type.getURI(), type.getUri())) {
        Resource resource = resources.get(rdfResource.getUri());
        setTextAttrValues(type, resource, rdfResource);
        setRefAttrValues(type, resource, rdfResource, resources);
      }
    }

    return Lists.newArrayList(resources.values());
  }

  private String removeDiacritics(String str) {
    return Normalizer
        .normalize(str, Normalizer.Form.NFD)
        .replaceAll("[^\\p{ASCII}]", "");
  }

  private void setTextAttrValues(Class type, Resource resource, RdfResource rdfResource) {
    for (TextAttribute textAttribute : type.getTextAttributes()) {
      for (LangValue langValues : rdfResource.getLiterals(textAttribute.getUri())) {
        resource.addProperty(textAttribute.getId(),
                             Ascii.truncate(langValues.getLang(), 2, ""),
                             langValues.getValue(),
                             textAttribute.getRegex());
      }
    }
  }

  private void setRefAttrValues(Class type, Resource resource,
                                RdfResource rdfResource, Map<String, Resource> resources) {
    for (ReferenceAttribute refAttribute : type.getReferenceAttributes()) {
      List<String> objects = Lists.newArrayList(rdfResource.getObjects(refAttribute.getUri()));
      List<Resource> values =
          filter(
              transform(
                  filter(objects,
                         in(resources.keySet())),
                  forMap(resources)),
              resourceTypeMatches(refAttribute.getRange()));
      resource.addReferences(refAttribute.getId(), values);
    }
  }

  private Predicate<Resource> resourceTypeMatches(final Class requiredType) {
    return new Predicate<Resource>() {
      public boolean apply(Resource input) {
        Class type = input.getType();
        return Objects.equals(type.getId(), requiredType.getId()) &&
               Objects.equals(type.getSchemeId(), requiredType.getSchemeId());
      }
    };
  }

}
