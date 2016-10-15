package fi.thl.termed.exchange.rdf;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDF;

import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.rdf.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;

public class ResourcesToRdfModel implements java.util.function.Function<List<Resource>, RdfModel> {

  private static final String TERMED_NS = "http://termed.thl.fi/schemes/";

  private Scheme scheme;

  // caches
  private Map<ClassId, Class> classes = Maps.newHashMap();
  private Map<TextAttributeId, TextAttribute> textAttributes = Maps.newHashMap();
  private Map<ReferenceAttributeId, ReferenceAttribute> referenceAttributes = Maps.newHashMap();

  public ResourcesToRdfModel(Scheme scheme) {
    this.scheme = scheme;
    loadClassCache(scheme);
  }

  private void loadClassCache(Scheme scheme) {
    for (Class c : scheme.getClasses()) {
      classes.put(new ClassId(c), c);
      loadTextAttributeCache(c);
      loadReferenceAttributeCache(c);
    }
  }

  private void loadTextAttributeCache(Class c) {
    for (TextAttribute textAttr : c.getTextAttributes()) {
      textAttributes.put(new TextAttributeId(textAttr), textAttr);
    }
  }

  private void loadReferenceAttributeCache(Class c) {
    for (ReferenceAttribute refAttr : c.getReferenceAttributes()) {
      referenceAttributes.put(new ReferenceAttributeId(refAttr), refAttr);
    }
  }

  @Override
  public RdfModel apply(List<Resource> resources) {
    RdfModel model = new JenaRdfModel(ModelFactory.createDefaultModel());
    List<RdfResource> rdfResources = Lists.newArrayList();

    for (Resource resource : resources) {
      RdfResource rdfResource = new RdfResource(getResourceUri(resource));
      rdfResource.addObject(RDF.type.getURI(), getClassUri(new ClassId(resource)));

      for (Map.Entry<String, StrictLangValue> entry : resource.getProperties().entries()) {
        StrictLangValue langValue = entry.getValue();
        String attributeUri = getTextAttributeUri(
            new TextAttributeId(new ClassId(resource), entry.getKey()));
        rdfResource.addLiteral(attributeUri, langValue.getLang(), langValue.getValue());
      }

      for (Map.Entry<String, Resource> entry : resource.getReferences().entries()) {
        Resource value = entry.getValue();
        String attributeUri = getReferenceAttributeUri(
            new ReferenceAttributeId(new ClassId(resource), entry.getKey()));
        rdfResource.addObject(attributeUri, getResourceUri(value));
      }

      rdfResources.add(rdfResource);
    }

    model.save(rdfResources);
    return model;
  }

  private String getSchemeUri(Scheme scheme) {
    return coalesce(scheme.getUri(),
                    TERMED_NS + coalesce(scheme.getCode(),
                                         scheme.getId().toString()));
  }

  private String getClassUri(ClassId classId) {
    Class cls = classes.get(classId);
    return coalesce(cls.getUri(),
                    getSchemeUri(scheme) + "/classes/" + cls.getId());
  }

  private String getTextAttributeUri(TextAttributeId attributeId) {
    TextAttribute attribute = textAttributes.get(attributeId);
    return coalesce(attribute.getUri(),
                    getSchemeUri(scheme) +
                    "/classes/" + attribute.getDomainId() +
                    "/textAttributes/" + attribute.getId());
  }

  private String getReferenceAttributeUri(ReferenceAttributeId attributeId) {
    ReferenceAttribute attribute = referenceAttributes.get(attributeId);
    return coalesce(attribute.getUri(),
                    getSchemeUri(scheme) +
                    "/classes/" + attribute.getDomainId() +
                    "/referenceAttributes/" + attribute.getId());
  }

  private String getResourceUri(Resource resource) {
    return coalesce(resource.getUri(),
                    getSchemeUri(resource.getScheme()) +
                    "/classes/" + resource.getTypeId() +
                    "/resources/" + coalesce(resource.getCode(), resource.getId().toString()));
  }

  private String coalesce(String s1, String s2) {
    return !Strings.isNullOrEmpty(s1) ? s1 : s2;
  }

}
