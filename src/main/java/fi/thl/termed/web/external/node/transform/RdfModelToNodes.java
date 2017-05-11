package fi.thl.termed.web.external.node.transform;

import static fi.thl.termed.util.RegularExpressions.URN_UUID;
import static fi.thl.termed.util.UUIDs.fromString;
import static fi.thl.termed.util.UUIDs.nameUUIDFromString;

import com.google.common.base.Ascii;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.StringUtils;
import fi.thl.termed.util.URIs;
import fi.thl.termed.util.rdf.RdfModel;
import fi.thl.termed.util.rdf.RdfResource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.apache.jena.vocabulary.RDF;

/**
 * Function to transform rdf model into list of nodes conforming to provided graph.
 */
public class RdfModelToNodes implements Function<RdfModel, List<Node>> {

  private List<Type> types;
  private Function<NodeId, Optional<Node>> nodeProvider;

  public RdfModelToNodes(List<Type> types, Function<NodeId, Optional<Node>> nodeProvider) {
    this.types = types;
    this.nodeProvider = nodeProvider;
  }

  @Override
  public List<Node> apply(RdfModel rdfModel) {
    Map<String, Node> nodes = Maps.newLinkedHashMap();

    // init nodes
    for (Type type : types) {
      for (RdfResource r : rdfModel.find(RDF.type.getURI(), type.getUri())) {
        String uri = r.getUri();

        Node node = new Node();

        if (uri.matches(URN_UUID)) {
          node.setId(fromString(uri.substring("urn:uuid:".length())));
        } else {
          node.setId(nameUUIDFromString(uri));
          node.setUri(uri);
          node.setCode(StringUtils.normalize(URIs.localName(uri)));
        }

        node.setType(new TypeId(type));
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

  private void setRefAttrValues(Type type, Node node, RdfResource rdfResource,
      Map<String, Node> nodes) {
    for (ReferenceAttribute refAttribute : type.getReferenceAttributes()) {
      for (String objectUri : rdfResource.getObjects(refAttribute.getUri())) {
        if (nodes.containsKey(objectUri)) {
          Node object = nodes.get(objectUri);
          if (object.getType().equals(refAttribute.getRange())) {
            node.addReference(refAttribute.getId(), object.identifier());
          }
        } else {
          UUID objectId = objectUri.matches(URN_UUID) ?
              fromString(objectUri.substring("urn:uuid:".length())) :
              nameUUIDFromString(objectUri);
          Optional<Node> object = nodeProvider.apply(new NodeId(objectId, refAttribute.getRange()));
          object.ifPresent(o -> node.addReference(refAttribute.getId(), o.identifier()));
        }
      }
    }
  }

}
