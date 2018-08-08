package fi.thl.termed.service.node.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static fi.thl.termed.util.RegularExpressions.URN_UUID;
import static fi.thl.termed.util.UUIDs.fromString;
import static fi.thl.termed.util.UUIDs.nameUUIDFromString;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Ascii;
import com.google.common.collect.Maps;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.Node.Builder;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
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
  private boolean importCodes;

  public RdfModelToNodes(List<Type> types, Function<NodeId, Optional<Node>> nodeProvider,
      boolean importCodes) {
    this.types = types.stream().filter(t -> t.getUri().isPresent()).collect(toList());
    this.nodeProvider = nodeProvider;
    this.importCodes = importCodes;
  }

  @Override
  public List<Node> apply(RdfModel rdfModel) {
    Map<String, Node.Builder> nodes = Maps.newLinkedHashMap();

    // init nodes
    for (Type type : types) {
      for (RdfResource r : rdfModel.find(RDF.type.getURI(), type.getUri().orElse(null))) {
        String uri = r.getUri();

        Node.Builder nodeBuilder;

        if (uri.matches(URN_UUID)) {
          nodeBuilder = Node.builder()
              .id(fromString(uri.substring("urn:uuid:".length())), type.identifier());
        } else {
          nodeBuilder = Node.builder()
              .id(nameUUIDFromString(uri), type.identifier())
              .uri(uri)
              .code(importCodes ? StringUtils.normalize(URIs.localName(uri)) : null);
        }

        nodes.put(r.getUri(), nodeBuilder);
      }
    }

    // populate attributes
    for (Type type : types) {
      for (RdfResource rdfResource : rdfModel.find(RDF.type.getURI(), type.getUri().orElse(null))) {
        Node.Builder nodeBuilder = nodes.get(rdfResource.getUri());

        setTextAttrValues(type, nodeBuilder, rdfResource);
        setRefAttrValues(type, nodeBuilder, rdfResource, nodes);
      }
    }

    return nodes.values().stream()
        .map(Builder::build)
        .collect(toImmutableList());
  }

  private void setTextAttrValues(Type type, Node.Builder node, RdfResource rdfResource) {
    for (TextAttribute textAttribute : type.getTextAttributes()) {
      for (LangValue langValues : rdfResource.getLiterals(textAttribute.getUri().orElse(null))) {
        node.addProperties(textAttribute.getId(),
            new StrictLangValue(
                Ascii.truncate(langValues.getLang(), 2, ""),
                langValues.getValue(),
                textAttribute.getRegex()));
      }
    }
  }

  private void setRefAttrValues(Type type, Node.Builder node, RdfResource rdfResource,
      Map<String, Node.Builder> nodes) {
    for (ReferenceAttribute refAttribute : type.getReferenceAttributes()) {
      for (String objectUri : rdfResource.getObjects(refAttribute.getUri().orElse(null))) {
        if (nodes.containsKey(objectUri)) {
          Node object = nodes.get(objectUri).build();
          if (object.getType().equals(refAttribute.getRange())) {
            node.addReferences(refAttribute.getId(), object.identifier());
          }
        } else {
          UUID objectId = objectUri.matches(URN_UUID) ?
              fromString(objectUri.substring("urn:uuid:".length())) :
              nameUUIDFromString(objectUri);
          Optional<Node> object = nodeProvider.apply(new NodeId(objectId, refAttribute.getRange()));
          object.ifPresent(o -> node.addReferences(refAttribute.getId(), o.identifier()));
        }
      }
    }
  }

}
