package fi.thl.termed.web.external.node.rdf;

import com.google.common.base.Ascii;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.jena.vocabulary.RDF;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.StringUtils;
import fi.thl.termed.util.URIs;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;

/**
 * Function to transform rdf model into list of nodes conforming to provided graph.
 */
public class RdfModelToNodes implements Function<RdfModel, List<Node>> {

  private List<Type> types;

  public RdfModelToNodes(List<Type> types) {
    this.types = types;
  }

  @Override
  public List<Node> apply(RdfModel rdfModel) {
    Map<String, Node> nodes = Maps.newLinkedHashMap();

    // init nodes
    for (Type type : types) {
      for (RdfResource r : rdfModel.find(RDF.type.getURI(), type.getUri())) {
        Node node = new Node();
        node.setCode(StringUtils.normalize(URIs.localName(r.getUri())));
        node.setUri(r.getUri());
        node.setType(new TypeId(type));
        node.setId(UUIDs.nameUUIDFromString(r.getUri()));
        nodes.put(r.getUri(), node);
      }
    }

    // populate attributes
    for (Type type : types) {
      for (RdfResource rdfResource : rdfModel.find(RDF.type.getURI(), type.getUri())) {
        Node node = nodes.get(rdfResource.getUri());
        setTextAttrValues(type, node, rdfResource);
        setRefAttrValues(type, node, rdfResource, nodes);
      }
    }

    return Lists.newArrayList(nodes.values());
  }

  private void setTextAttrValues(Type type, Node node, RdfResource rdfResource) {
    for (TextAttribute textAttribute : type.getTextAttributes()) {
      for (LangValue langValues : rdfResource.getLiterals(textAttribute.getUri())) {
        node.addProperty(textAttribute.getId(),
                         Ascii.truncate(langValues.getLang(), 2, ""),
                         langValues.getValue(),
                         textAttribute.getRegex());
      }
    }
  }

  private void setRefAttrValues(Type type, Node node,
                                RdfResource rdfResource, Map<String, Node> nodes) {
    for (ReferenceAttribute refAttribute : type.getReferenceAttributes()) {
      List<String> objects = Lists.newArrayList(rdfResource.getObjects(refAttribute.getUri()));
      List<NodeId> values = objects.stream()
          .filter(nodes::containsKey).map(nodes::get)
          .filter(r -> new TypeId(r).equals(refAttribute.getRange()))
          .map(NodeId::new)
          .collect(Collectors.toList());
      node.addReferences(refAttribute.getId(), values);
    }
  }

}
