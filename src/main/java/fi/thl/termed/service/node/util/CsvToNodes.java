package fi.thl.termed.service.node.util;

import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.RegularExpressions.IETF_LANGUAGE_TAG;
import static fi.thl.termed.util.UUIDs.fromString;
import static fi.thl.termed.util.UUIDs.nilUuid;
import static fi.thl.termed.util.query.AndSpecification.and;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.google.common.base.MoreObjects;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByPropertyString;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.util.TableUtils;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.csv.CsvUtils;
import fi.thl.termed.util.query.Specification;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.jena.atlas.RuntimeIOException;

/**
 * Import nodes from CSV
 */
public final class CsvToNodes {

  private static final Pattern PROPERTY_KEY = Pattern.compile(
      "^(properties|p)\\.(" + CODE + ")(\\.(" + IETF_LANGUAGE_TAG + "))?$");
  private static final Pattern REFERENCE_KEY = Pattern.compile(
      "^(references|r)\\.(" + CODE + ")(\\.id)?$");
  private static final Pattern REFERENCE_PROPERTY_KEY = Pattern.compile(
      "^(references|r)\\.(" + CODE + ")\\."
          + "(properties|p)\\.(" + CODE + ")(\\.(" + IETF_LANGUAGE_TAG + "))?$");

  private static final TypeId unknownTypeId = TypeId.of("", nilUuid());

  private Map<TypeId, Map<String, TypeId>> typeReferenceAttributeRangeIndex;
  private Function<Specification<NodeId, Node>, Optional<NodeId>> referenceIdResolver;

  public CsvToNodes() {
    this(emptyList(), (s) -> Optional.empty());
  }

  public CsvToNodes(
      List<Type> types,
      Function<Specification<NodeId, Node>, Optional<NodeId>> referenceIdResolver) {
    this.typeReferenceAttributeRangeIndex = types.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .collect(
            groupingBy(ReferenceAttribute::getDomain,
                toMap(ReferenceAttribute::getId, ReferenceAttribute::getRange)));
    this.referenceIdResolver = referenceIdResolver;
  }

  public Stream<Node> parseNodesFromCsv(GraphId graphId, CsvOptions csvOpts, InputStream in) {
    return TableUtils.toMapped(CsvUtils.readCsv(csvOpts, in)).map(row -> mapToNode(graphId, row));
  }

  public Stream<Node> parseNodesFromCsv(TypeId typeId, CsvOptions csvOpts, InputStream in) {
    return TableUtils.toMapped(CsvUtils.readCsv(csvOpts, in)).map(row -> mapToNode(typeId, row));
  }

  public Stream<Node> parseNodesFromCsv(CsvOptions csvOpts, InputStream in) {
    return TableUtils.toMapped(CsvUtils.readCsv(csvOpts, in)).map(this::mapToNode);
  }

  private Node mapToNode(GraphId graphId, Map<String, String> row) {
    NodeId nodeId = NodeId.of(
        MoreObjects.firstNonNull(UUIDs.fromString(row.get("id")), UUID.randomUUID()),
        requireNonNull(row.get("type.id")),
        graphId.getId());
    return mapToNode(nodeId, row);
  }

  private Node mapToNode(TypeId typeId, Map<String, String> row) {
    NodeId nodeId = NodeId.of(
        MoreObjects.firstNonNull(UUIDs.fromString(row.get("id")), UUID.randomUUID()),
        typeId);
    return mapToNode(nodeId, row);
  }

  private Node mapToNode(Map<String, String> row) {
    NodeId nodeId = NodeId.of(
        MoreObjects.firstNonNull(UUIDs.fromString(row.get("id")), UUID.randomUUID()),
        requireNonNull(row.get("type.id")),
        requireNonNull(UUIDs.fromString(row.get("type.graph.id"))));
    return mapToNode(nodeId, row);
  }

  private Node mapToNode(NodeId nodeId, Map<String, String> row) {
    Node.Builder builder = Node.builder()
        .id(nodeId)
        .code(emptyToNull(row.get("code")))
        .uri(emptyToNull(row.get("uri")))
        .number(emptyToNull(row.get("number")) != null ? Long.valueOf(row.get("number")) : null);

    row.forEach((k, vs) -> {
      Matcher m = PROPERTY_KEY.matcher(k);
      if (m.matches()) {
        String attrId = m.group(2);
        String lang = nullToEmpty(m.group(4));
        fromInlineCsv(vs)
            .forEach(v -> builder.addProperty(attrId, new StrictLangValue(lang, v)));
      }
    });

    row.forEach((k, vs) -> {
      Matcher m = REFERENCE_KEY.matcher(k);
      if (m.matches()) {
        String attrId = m.group(2);
        fromInlineCsv(vs)
            .forEach(v -> builder.addReference(attrId, NodeId.of(fromString(v), unknownTypeId)));
      }
    });

    // if there are cols matching REFERENCE_PROPERTY_KEY, try to resolve them to ref IDs.
    row.forEach((k, vs) -> {
      Matcher m = REFERENCE_PROPERTY_KEY.matcher(k);
      if (m.matches()) {
        String refAttrId = m.group(2);
        String textAttrId = m.group(4);
        String lang = nullToEmpty(m.group(6));

        TypeId attributeRangeId =
            typeReferenceAttributeRangeIndex
                .getOrDefault(nodeId.getType(), emptyMap())
                .get(refAttrId);

        if (attributeRangeId != null) {
          fromInlineCsv(vs).forEach(v -> {
            Specification<NodeId, Node> refValueSpec = and(
                new NodesByGraphId(attributeRangeId.getGraphId()),
                new NodesByTypeId(attributeRangeId.getId()),
                new NodesByPropertyString(textAttrId, lang, v));

            Optional<NodeId> resolvedReferenceId = referenceIdResolver.apply(refValueSpec);

            resolvedReferenceId.ifPresent(refId -> builder.addReference(refAttrId, refId));
          });
        }
      }
    });

    return builder.build();
  }

  private List<String> fromInlineCsv(String csvRow) {
    StringReader reader = new StringReader(csvRow);
    CSVReader csvReader = new CSVReaderBuilder(reader)
        .withCSVParser(
            new CSVParserBuilder()
                .withSeparator('|')
                .withQuoteChar('\'')
                .withEscapeChar('\\')
                .withStrictQuotes(false)
                .build())
        .build();

    try {
      return csvReader.peek() != null ? Arrays.asList(csvReader.readNext()) : emptyList();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

}
