package fi.thl.termed.web.external.node.transform;

import com.google.common.collect.Lists;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.jena.JenaRdfModel;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;

public class NodesToRdfModel implements Function<List<Node>, RdfModel> {

  private static final String TERMED_NS = "http://termed.thl.fi/graphs/";

  // caches
  private Map<TypeId, Type> types = new HashMap<>();
  private Map<TextAttributeId, TextAttribute> textAttributes = new HashMap<>();
  private Map<ReferenceAttributeId, ReferenceAttribute> referenceAttributes = new HashMap<>();
  private Function<NodeId, Optional<Node>> nodeLoader;

  public NodesToRdfModel(List<Type> typeList,
                         Function<NodeId, Optional<Node>> nodeLoader) {
    loadTypeCache(typeList);
    this.nodeLoader = nodeLoader;
  }

  private void loadTypeCache(List<Type> typeList) {
    for (Type c : typeList) {
      types.put(new TypeId(c), c);
      loadTextAttributeCache(c);
      loadReferenceAttributeCache(c);
    }
  }

  private void loadTextAttributeCache(Type c) {
    for (TextAttribute textAttr : c.getTextAttributes()) {
      textAttributes.put(new TextAttributeId(textAttr), textAttr);
    }
  }

  private void loadReferenceAttributeCache(Type c) {
    for (ReferenceAttribute refAttr : c.getReferenceAttributes()) {
      referenceAttributes.put(new ReferenceAttributeId(refAttr), refAttr);
    }
  }

  @Override
  public RdfModel apply(List<Node> nodeList) {
    List<RdfResource> rdfResources = Lists.newArrayList();

    for (Node node : nodeList) {
      RdfResource rdfResource = new RdfResource(getNodeUri(new NodeId(node)));
      rdfResource.addObject(RDF.type.getURI(), getTypeUri(new TypeId(node)));

      for (Map.Entry<String, StrictLangValue> entry : node.getProperties().entries()) {
        StrictLangValue langValue = entry.getValue();
        String attributeUri = getTextAttributeUri(
            new TextAttributeId(new TypeId(node), entry.getKey()));
        rdfResource.addLiteral(attributeUri, langValue.getLang(), langValue.getValue());
      }

      for (Map.Entry<String, NodeId> entry : node.getReferences().entries()) {
        NodeId value = entry.getValue();
        String attributeUri = getReferenceAttributeUri(
            new ReferenceAttributeId(new TypeId(node), entry.getKey()));
        rdfResource.addObject(attributeUri, getNodeUri(value));
      }

      rdfResources.add(rdfResource);
    }

    return new JenaRdfModel(ModelFactory.createDefaultModel()).save(rdfResources);
  }

  private String getTypeUri(TypeId typeId) {
    Type cls = types.get(typeId);
    return firstNonNull(emptyToNull(cls.getUri()),
                        TERMED_NS + typeId.getGraphId() +
                        "/types/" + typeId.getId());
  }

  private String getTextAttributeUri(TextAttributeId attributeId) {
    TextAttribute attribute = textAttributes.get(attributeId);
    TypeId domainId = attributeId.getDomainId();
    return firstNonNull(emptyToNull(attribute.getUri()),
                        TERMED_NS + domainId.getGraphId() +
                        "/types/" + domainId.getId() +
                        "/textAttributes/" + attributeId.getId());
  }

  private String getReferenceAttributeUri(ReferenceAttributeId attributeId) {
    ReferenceAttribute attribute = referenceAttributes.get(attributeId);
    TypeId domainId = attributeId.getDomainId();
    return firstNonNull(emptyToNull(attribute.getUri()),
                        TERMED_NS + domainId.getGraphId() +
                        "/types/" + domainId.getId() +
                        "/referenceAttributes/" + attributeId.getId());
  }

  private String getNodeUri(NodeId nodeId) {
    Optional<Node> node = nodeLoader.apply(nodeId);
    return firstNonNull(emptyToNull(node.isPresent() ? node.get().getUri() : ""),
                        TERMED_NS + nodeId.getTypeGraphId() +
                        "/types/" + nodeId.getTypeId() +
                        "/nodes/" + nodeId.getId());
  }

}
