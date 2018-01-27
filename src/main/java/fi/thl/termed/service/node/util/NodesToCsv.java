package fi.thl.termed.service.node.util;

import static com.google.common.collect.Multimaps.filterKeys;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.opencsv.CSVWriter;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.service.node.select.SelectAllProperties;
import fi.thl.termed.service.node.select.SelectAllReferences;
import fi.thl.termed.service.node.select.SelectCode;
import fi.thl.termed.service.node.select.SelectCreatedBy;
import fi.thl.termed.service.node.select.SelectCreatedDate;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectLastModifiedBy;
import fi.thl.termed.service.node.select.SelectLastModifiedDate;
import fi.thl.termed.service.node.select.SelectNumber;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.service.node.select.SelectReference;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.service.node.select.SelectUri;
import fi.thl.termed.util.TableUtils;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.joda.time.DateTime;

/**
 * Write stream of nodes to OuputStream or Writer as CSV. This operation is not actually streaming
 * as all nodes are first transformed to maps and then to table and finally written in one operation.
 */
public final class NodesToCsv {

  private NodesToCsv() {
  }

  public static void writeAsCsv(Stream<Node> nodes, Set<Select> selects, OutputStream out) {
    writeAsCsv(nodes, selects, new OutputStreamWriter(out, Charsets.UTF_8));
  }

  public static void writeAsCsv(Stream<Node> nodes, Set<Select> selects, Writer writer) {
    CSVWriter csvWriter = new CSVWriter(writer, ',', '\"', '\"', "\n");
    List<Map<String, String>> rows = nodes.map(n -> nodeToMap(n, selects)).collect(toList());
    csvWriter.writeAll(TableUtils.toTable(rows), false);
  }

  private static Map<String, String> nodeToMap(Node node, Set<Select> s) {
    Map<String, String> map = new LinkedHashMap<>();

    if (s.contains(new SelectAll()) || s.contains(new SelectId())) {
      map.put("id", node.getId().toString());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectCode())) {
      map.put("code", node.getCode());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectUri())) {
      map.put("uri", node.getUri());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectNumber())) {
      map.put("number", node.getNumber().toString());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectCreatedBy())) {
      map.put("createdBy", node.getCreatedBy());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectCreatedDate())) {
      map.put("createdDate", new DateTime(node.getCreatedDate()).toString());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectLastModifiedBy())) {
      map.put("lastModifiedBy", node.getLastModifiedBy());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectLastModifiedDate())) {
      map.put("lastModifiedDate", new DateTime(node.getLastModifiedDate()).toString());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectType())) {
      map.put("type.id", node.getType().getId());
      map.put("type.graph.id", node.getType().getGraphId().toString());
    }

    Multimap<String, StrictLangValue> properties =
        s.contains(new SelectAll()) || s.contains(new SelectAllProperties()) ?
            node.getProperties() :
            filterKeys(node.getProperties(), key -> s.contains(new SelectProperty(key)));

    properties.asMap().forEach((propertyId, strictLangValues) -> {
      Map<String, List<String>> valuesByLang = strictLangValues.stream().collect(
          groupingBy(StrictLangValue::getLang, mapping(StrictLangValue::getValue, toList())));
      valuesByLang.forEach((lang, values) ->
          map.put(propertyId + (lang.isEmpty() ? "" : "." + lang), toInlineCsv(values)));
    });

    Multimap<String, NodeId> references =
        s.contains(new SelectAll()) || s.contains(new SelectAllReferences()) ?
            node.getReferences() :
            filterKeys(node.getReferences(), key -> s.contains(new SelectReference(key)));

    references.asMap().forEach((propertyId, referenceIds) ->
        map.put(propertyId + ".id", toInlineCsv(referenceIds.stream()
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
