package fi.thl.termed.service.node.util;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Multimaps.filterKeys;
import static fi.thl.termed.util.RegularExpressions.CODE;
import static fi.thl.termed.util.TableUtils.toMapped;
import static fi.thl.termed.util.TableUtils.toTable;
import static fi.thl.termed.util.UUIDs.fromString;
import static fi.thl.termed.util.UUIDs.nilUuid;
import static fi.thl.termed.util.csv.CsvUtils.readCsv;
import static fi.thl.termed.util.csv.CsvUtils.writeCsv;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Multimap;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.service.node.select.SelectAllProperties;
import fi.thl.termed.service.node.select.SelectAllReferences;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.service.node.select.SelectReference;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.jena.atlas.RuntimeIOException;
import org.joda.time.DateTime;

/**
 * Write stream of nodes to OuputStream or Writer as CSV. This operation is not actually streaming
 * as all nodes are first transformed to maps and then to table and finally written in one
 * operation.
 */
public final class NodesToCsv {

  private static final Pattern PROPERTY_KEY = Pattern.compile(
      "^(properties|p)\\.(" + CODE + ")(\\.(" + CODE + "))?$");
  private static final Pattern REFERENCE_KEY = Pattern.compile(
      "^(references|r)\\.(" + CODE + ")(\\.id)?$");
  private static final TypeId unknownTypeId = TypeId.of("", nilUuid());

  private NodesToCsv() {
  }

  public static Stream<Node> readAsCsv(CsvOptions csvOpts, InputStream in) {
    return toMapped(readCsv(csvOpts, in)).map(NodesToCsv::mapToNode);
  }

  private static Node mapToNode(Map<String, String> map) {
    UUID graphId = firstNonNull(UUIDs.fromString(map.get("type.graph.id")), nilUuid());
    String typeId = firstNonNull(map.get("type.id"), "");
    UUID nodeId = firstNonNull(UUIDs.fromString(map.get("id")), randomUUID());

    Node.Builder builder = Node.builder()
        .id(nodeId, typeId, graphId)
        .code(emptyToNull(map.get("code")))
        .uri(emptyToNull(map.get("uri")))
        .number(map.containsKey("number") ? Long.valueOf(map.get("number")) : null);

    map.forEach((k, vs) -> {
      Matcher m = PROPERTY_KEY.matcher(k);
      if (m.matches()) {
        fromInlineCsv(vs).forEach(
            v -> builder.addProperty(m.group(2), new StrictLangValue(nullToEmpty(m.group(4)), v)));
      }
    });

    map.forEach((k, vs) -> {
      Matcher m = REFERENCE_KEY.matcher(k);
      if (m.matches()) {
        fromInlineCsv(vs).forEach(
            v -> builder.addReference(m.group(2), NodeId.of(fromString(v), unknownTypeId)));
      }
    });

    return builder.build();
  }

  private static List<String> fromInlineCsv(String csvRow) {
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

  public static void writeAsCsv(Stream<Node> nodes, List<Select> selects, CsvOptions csvOpts,
      OutputStream out) {
    writeCsv(out, csvOpts,
        toTable(nodes.map(n -> nodeToMap(n, selects)).collect(toList())).stream());
  }

  private static Map<String, String> nodeToMap(Node node, List<Select> s) {
    Map<String, String> map = new LinkedHashMap<>();

    if (s.contains(new SelectAll()) || s.contains(new SelectId())) {
      map.put("id", node.getId().toString());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectType())) {
      map.put("type.id", node.getType().getId());
      map.put("type.graph.id", node.getTypeGraphId().toString());
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("code"))) {
      map.put("code", node.getCode().orElse(null));
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("uri"))) {
      map.put("uri", node.getUri().orElse(null));
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("number"))) {
      map.put("number", node.getNumber().toString());
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("createdBy"))) {
      map.put("createdBy", node.getCreatedBy());
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("createdDate"))) {
      map.put("createdDate", new DateTime(node.getCreatedDate()).toString());
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("lastModifiedBy"))) {
      map.put("lastModifiedBy", node.getLastModifiedBy());
    }
    if (s.contains(new SelectAll()) || s.contains(Select.field("lastModifiedDate"))) {
      map.put("lastModifiedDate", new DateTime(node.getLastModifiedDate()).toString());
    }

    Multimap<String, StrictLangValue> properties =
        s.contains(new SelectAll()) || s.contains(new SelectAllProperties()) ?
            node.getProperties() :
            filterKeys(node.getProperties(), key -> s.contains(new SelectProperty(key)));

    properties.asMap().forEach((attrId, langValues) ->
        langValues.stream()
            .collect(groupingBy(
                StrictLangValue::getLang,
                LinkedHashMap::new,
                mapping(StrictLangValue::getValue, toList())))
            .forEach((lang, values) ->
                map.put("properties." + attrId + (lang.isEmpty() ? "" : "." + lang),
                    toInlineCsv(values))));

    Multimap<String, NodeId> references =
        s.contains(new SelectAll()) || s.contains(new SelectAllReferences()) ?
            node.getReferences() :
            filterKeys(node.getReferences(), key -> s.contains(new SelectReference(key)));

    references.asMap().forEach((attrId, referenceIds) ->
        map.put("references." + attrId + ".id", toInlineCsv(referenceIds.stream()
            .map(NodeId::getId)
            .map(UUID::toString)
            .collect(toList()))));

    return map;
  }

  private static String toInlineCsv(List<String> row) {
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, '|', '\'', '\'', "");
    csvWriter.writeNext(row.toArray(new String[row.size()]), false);
    return writer.toString();
  }

}
