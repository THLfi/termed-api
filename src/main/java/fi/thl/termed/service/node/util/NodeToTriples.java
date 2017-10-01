package fi.thl.termed.service.node.util;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Optional.ofNullable;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.RDF;

public class NodeToTriples implements Function<Node, Stream<Triple>> {

  // caches
  private Map<TypeId, Type> types = new HashMap<>();
  private Map<TextAttributeId, TextAttribute> textAttributes = new HashMap<>();
  private Map<ReferenceAttributeId, ReferenceAttribute> referenceAttributes = new HashMap<>();

  private Function<NodeId, String> uriProvider;

  public NodeToTriples(List<Type> typeList, Function<NodeId, String> uriProvider) {
    typeList.forEach(type -> {
      types.put(type.identifier(), type);
      type.getTextAttributes().forEach(a -> textAttributes.put(a.identifier(), a));
      type.getReferenceAttributes().forEach(a -> referenceAttributes.put(a.identifier(), a));
    });

    this.uriProvider = uriProvider;
  }

  @Override
  public Stream<Triple> apply(Node node) {
    Stream.Builder<Triple> builder = Stream.builder();
    Type type = ofNullable(types.get(node.getType())).orElseThrow(IllegalStateException::new);

    org.apache.jena.graph.Node subject = createURI(uri(node));

    builder.accept(Triple.create(subject, RDF.type.asNode(), createURI(type.getUri().orElseThrow(
        () -> new RuntimeException("URI missing from Type: " + type.getId())))));

    for (Map.Entry<String, StrictLangValue> entry : node.getProperties().entries()) {
      TextAttributeId attrId = new TextAttributeId(node.getType(), entry.getKey());
      TextAttribute attr = ofNullable(textAttributes.get(attrId))
          .orElseThrow(IllegalStateException::new);
      StrictLangValue langValue = entry.getValue();
      builder.add(Triple.create(subject, createURI(attr.getUri()),
          createLiteral(langValue.getValue(), langValue.getLang())));
    }

    for (Map.Entry<String, NodeId> entry : node.getReferences().entries()) {
      ReferenceAttributeId attrId = new ReferenceAttributeId(node.getType(), entry.getKey());
      ReferenceAttribute attr = ofNullable(referenceAttributes.get(attrId))
          .orElseThrow(IllegalStateException::new);
      String valueUri = uriProvider.apply(entry.getValue());
      builder.add(Triple.create(subject, createURI(attr.getUri()), createURI(valueUri)));
    }

    return builder.build();
  }

  private String uri(Node node) {
    return isNullOrEmpty(node.getUri()) ? "urn:uuid:" + node.getId() : node.getUri();
  }

}
