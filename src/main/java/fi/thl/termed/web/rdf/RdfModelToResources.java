package fi.thl.termed.web.rdf;

import com.google.common.base.Ascii;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.jena.vocabulary.RDF;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.util.URIs;

/**
 * Function to transform rdf model into list of resources conforming to provided scheme.
 */
public class RdfModelToResources implements Function<RdfModel, List<Resource>> {

  private List<Class> classes;

  public RdfModelToResources(List<Class> classes) {
    this.classes = classes;
  }

  @Override
  public List<Resource> apply(RdfModel rdfModel) {
    Map<String, Resource> resources = Maps.newLinkedHashMap();

    // init resources
    for (Class type : classes) {
      for (RdfResource r : rdfModel.find(RDF.type.getURI(), type.getUri())) {
        Resource resource = new Resource();
        resource.setCode(removeDiacritics(URIs.localName(r.getUri())));
        resource.setUri(r.getUri());
        resource.setScheme(type.getScheme());
        resource.setType(type);
        resources.put(r.getUri(), resource);
      }
    }

    // populate attributes
    for (Class type : classes) {
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
      List<Resource> values = objects.stream()
          .filter(resources::containsKey).map(resources::get)
          .filter(r -> new ClassId(r).equals(refAttribute.getRangeClassId()))
          .collect(Collectors.toList());
      resource.addReferences(refAttribute.getId(), values);
    }
  }

}
