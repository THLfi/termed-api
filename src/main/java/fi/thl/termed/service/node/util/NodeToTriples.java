package fi.thl.termed.service.node.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.jena.graph.NodeFactory.createLiteral;
import static org.apache.jena.graph.NodeFactory.createURI;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

public class NodeToTriples implements Function<Node, Stream<Triple>> {

  private Function<TypeId, Optional<Type>> typeProvider;
  private Function<NodeId, Optional<Node>> nodeProvider;

  private Map<TextAttributeId, TextAttribute> textAttrCache = new HashMap<>();
  private Map<ReferenceAttributeId, ReferenceAttribute> refAttrCache = new HashMap<>();

  private boolean useUuidUris;

  public NodeToTriples(
      Function<TypeId, Optional<Type>> typeProvider,
      Function<NodeId, Optional<Node>> nodeProvider,
      boolean useUuidUris) {
    this.typeProvider = typeProvider;
    this.nodeProvider = nodeProvider;
    this.useUuidUris = useUuidUris;
  }

  private TextAttribute findAttribute(Type type, TextAttributeId attributeId) {
    return type.getTextAttributes().stream()
        .filter(a -> a.identifier().equals(attributeId)).findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  private ReferenceAttribute findAttribute(Type type, ReferenceAttributeId attributeId) {
    return type.getReferenceAttributes().stream()
        .filter(a -> a.identifier().equals(attributeId)).findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  @Override
  public Stream<Triple> apply(Node node) {
    Stream.Builder<Triple> builder = Stream.builder();
    Type type = typeProvider.apply(node.getType()).orElseThrow(IllegalStateException::new);

    org.apache.jena.graph.Node subject = createURI(uri(node));

    builder.accept(Triple.create(subject, RDF.type.asNode(), createURI(type.getUri())));

    for (Map.Entry<String, StrictLangValue> entry : node.getProperties().entries()) {
      TextAttributeId attrId = new TextAttributeId(node.getType(), entry.getKey());
      TextAttribute attr = textAttrCache.computeIfAbsent(attrId, id -> findAttribute(type, id));
      StrictLangValue langValue = entry.getValue();
      builder.add(Triple.create(subject, createURI(attr.getUri()),
          createLiteral(langValue.getValue(), langValue.getLang())));
    }

    for (Map.Entry<String, NodeId> entry : node.getReferences().entries()) {
      ReferenceAttributeId attrId = new ReferenceAttributeId(node.getType(), entry.getKey());
      ReferenceAttribute attr = refAttrCache.computeIfAbsent(attrId, id -> findAttribute(type, id));
      NodeId valueId = entry.getValue();
      String valueUri = useUuidUris ? "urn:uuid:" + valueId.getId() :
          nodeProvider.apply(entry.getValue()).orElseThrow(IllegalStateException::new).getUri();
      builder.add(Triple.create(subject, createURI(attr.getUri()), createURI(valueUri)));
    }

    return builder.build();
  }

  private String uri(Node node) {
    return useUuidUris || isNullOrEmpty(node.getUri()) ?
        "urn:uuid:" + node.getId() : node.getUri();
  }

}
