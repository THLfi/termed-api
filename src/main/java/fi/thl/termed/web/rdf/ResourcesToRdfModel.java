package fi.thl.termed.web.rdf;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

public class ResourcesToRdfModel implements Function<List<Resource>, RdfModel> {

  private static final String TERMED_NS = "http://termed.thl.fi/schemes/";

  // caches
  private Map<ClassId, Class> classes = Maps.newHashMap();
  private Map<TextAttributeId, TextAttribute> textAttributes = Maps.newHashMap();
  private Map<ReferenceAttributeId, ReferenceAttribute> referenceAttributes = Maps.newHashMap();

  public ResourcesToRdfModel(List<Class> classList) {
    loadClassCache(classList);
  }

  private void loadClassCache(List<Class> classList) {
    for (Class c : classList) {
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

    return new JenaRdfModel(ModelFactory.createDefaultModel()).save(rdfResources);
  }

  private String getClassUri(ClassId classId) {
    Class cls = classes.get(classId);
    return firstNonNull(emptyToNull(cls.getUri()),
                        TERMED_NS + classId.getSchemeId() +
                        "/classes/" + classId.getId());
  }

  private String getTextAttributeUri(TextAttributeId attributeId) {
    TextAttribute attribute = textAttributes.get(attributeId);
    ClassId domainId = attributeId.getDomainId();
    return firstNonNull(emptyToNull(attribute.getUri()),
                        TERMED_NS + domainId.getSchemeId() +
                        "/classes/" + domainId.getId() +
                        "/textAttributes/" + attributeId.getId());
  }

  private String getReferenceAttributeUri(ReferenceAttributeId attributeId) {
    ReferenceAttribute attribute = referenceAttributes.get(attributeId);
    ClassId domainId = attributeId.getDomainId();
    return firstNonNull(emptyToNull(attribute.getUri()),
                        TERMED_NS + domainId.getSchemeId() +
                        "/classes/" + domainId.getId() +
                        "/referenceAttributes/" + attributeId.getId());
  }

  private String getResourceUri(Resource resource) {
    ClassId typeId = new ClassId(resource);
    return firstNonNull(emptyToNull(resource.getUri()),
                        TERMED_NS + typeId.getSchemeId() +
                        "/classes/" + typeId.getId() +
                        "/resources/" + resource.getId());
  }

}
