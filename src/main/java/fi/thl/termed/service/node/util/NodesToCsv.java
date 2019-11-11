package fi.thl.termed.service.node.util;

import static com.google.common.collect.Multimaps.filterKeys;
import static fi.thl.termed.util.TableUtils.toTable;
import static fi.thl.termed.util.csv.CsvUtils.writeCsv;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opencsv.CSVWriter;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.service.node.select.SelectAllProperties;
import fi.thl.termed.service.node.select.SelectAllReferences;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.service.node.select.SelectReference;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.collect.MapUtils;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Selects;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Write stream of nodes to OutputStream or Writer as CSV. This operation is not actually streaming
 * as all nodes are first transformed to maps and then to table and finally written in one
 * operation.
 */
public final class NodesToCsv {

  private boolean useLabeledReferences;
  private String labelProperty;
  private String labelPropertyLang;
  private Function<NodeId, Optional<Node>> nodeLoader;

  public NodesToCsv() {
    this(false, "prefLabel", "", (nodeId) -> Optional.empty());
  }

  public NodesToCsv(
      boolean useLabeledReferences,
      String labelProperty,
      String labelPropertyLang,
      Function<NodeId, Optional<Node>> nodeLoader) {
    this.useLabeledReferences = useLabeledReferences;
    this.labelProperty = labelProperty;
    this.labelPropertyLang = labelPropertyLang;
    this.nodeLoader = nodeLoader;
  }

  public void writeAsCsv(Stream<Node> nodes, List<Select> selects, CsvOptions csvOpts,
      OutputStream out) {
    Set<Select> selectsSet = ImmutableSet.copyOf(selects);
    writeCsv(out, csvOpts,
        toTable(nodes.map(n -> nodeToRowMap(n, selectsSet)).collect(toList())).stream());
  }

  private Map<String, String> nodeToRowMap(Node node, Set<Select> s) {
    Map<String, String> row = new LinkedHashMap<>();

    row.putAll(identifiersToMap(node, s));
    row.putAll(auditInfoToMap(node, s));
    row.putAll(propertiesToMap(node.getProperties(), s));

    if (useLabeledReferences) {
      row.putAll(referencesToLabeledMap(node.getReferences(), s));
    } else {
      row.putAll(referencesToMap(node.getReferences(), s));
    }

    return row;
  }

  private Map<String, String> identifiersToMap(Node node, Set<Select> s) {
    Map<String, String> map = new LinkedHashMap<>();

    if (s.contains(new SelectAll()) || s.contains(new SelectId())) {
      map.put("id", node.getId().toString());
    }
    if (s.contains(new SelectAll()) || s.contains(new SelectType())) {
      map.put("type.id", node.getType().getId());
      map.put("type.graph.id", UUIDs.toString(node.getTypeGraphId()));
    }
    if (s.contains(new SelectAll()) || s.contains(Selects.field("code"))) {
      map.put("code", node.getCode().orElse(null));
    }
    if (s.contains(new SelectAll()) || s.contains(Selects.field("uri"))) {
      map.put("uri", node.getUri().orElse(null));
    }
    if (s.contains(new SelectAll()) || s.contains(Selects.field("number"))) {
      map.put("number", node.getNumber() != null ? node.getNumber().toString() : "");
    }

    return map;
  }

  private Map<String, String> auditInfoToMap(Node node, Set<Select> s) {
    Map<String, String> map = new LinkedHashMap<>();

    if (s.contains(new SelectAll()) || s.contains(Selects.field("createdBy"))) {
      map.put("createdBy", node.getCreatedBy());
    }
    if (s.contains(new SelectAll()) || s.contains(Selects.field("createdDate"))) {
      map.put("createdDate",
          node.getCreatedDate() != null ? node.getCreatedDate().toString() : null);
    }
    if (s.contains(new SelectAll()) || s.contains(Selects.field("lastModifiedBy"))) {
      map.put("lastModifiedBy", node.getLastModifiedBy());
    }
    if (s.contains(new SelectAll()) || s.contains(Selects.field("lastModifiedDate"))) {
      map.put("lastModifiedDate",
          node.getLastModifiedDate() != null ? node.getLastModifiedDate().toString() : null);
    }

    return map;
  }

  private Map<String, String> propertiesToMap(
      Multimap<String, StrictLangValue> properties,
      Set<Select> s) {

    Multimap<String, StrictLangValue> selectedProperties =
        filterKeys(properties, key -> s.contains(new SelectAll())
            || s.contains(new SelectAllProperties())
            || s.contains(new SelectProperty(key)));

    return selectedProperties.asMap().entrySet().stream()
        .flatMap(entry -> {
          String attrId = entry.getKey();
          Collection<StrictLangValue> langValues = entry.getValue();

          return langValues.stream()
              .collect(groupingBy(
                  StrictLangValue::getLang,
                  LinkedHashMap::new,
                  mapping(StrictLangValue::getValue, toList())))
              .entrySet().stream()
              .map(e -> {
                String lang = e.getKey();
                String key = "properties." + attrId + (lang.isEmpty() ? "" : "." + lang);
                String value = toInlineCsv(e.getValue());
                return MapUtils.entry(key, value);
              });
        })
        .collect(MapUtils.toImmutableMap());
  }

  private Map<String, String> referencesToMap(
      Multimap<String, NodeId> references,
      Set<Select> s) {

    Multimap<String, NodeId> selectedReferences =
        filterKeys(references, key -> s.contains(new SelectAll())
            || s.contains(new SelectAllReferences())
            || s.contains(new SelectReference(key)));

    return selectedReferences.asMap().entrySet().stream()
        .map(entry -> {
          String attrId = entry.getKey();
          Collection<NodeId> referenceIds = entry.getValue();

          String key = "references." + attrId + ".id";
          String value = toInlineCsv(referenceIds.stream()
              .map(NodeId::getId)
              .map(UUIDs::toString)
              .collect(toList()));

          return MapUtils.entry(key, value);
        })
        .collect(MapUtils.toImmutableMap());
  }

  private Map<String, String> referencesToLabeledMap(
      Multimap<String, NodeId> references,
      Set<Select> s) {

    Multimap<String, NodeId> selectedReferences =
        filterKeys(references, key -> s.contains(new SelectAll())
            || s.contains(new SelectAllReferences())
            || s.contains(new SelectReference(key)));

    return selectedReferences.asMap().entrySet().stream()
        .map(entry -> {
          String attrId = entry.getKey();
          Collection<NodeId> referenceIds = entry.getValue();

          String key = "r." + attrId + ".p." + labelProperty +
              (labelPropertyLang.isEmpty() ? "" : "." + labelPropertyLang);
          String value = toInlineCsv(referenceIds.stream()
              .map(this::mapNodeIdToLabel)
              .collect(toList()));

          return MapUtils.entry(key, value);
        })
        .collect(MapUtils.toImmutableMap());
  }

  private String mapNodeIdToLabel(NodeId nodeId) {
    return nodeLoader.apply(nodeId)
        .flatMap(node -> node.getProperties()
            .get(labelProperty).stream()
            .filter(v -> labelPropertyLang.isEmpty() || v.getLang().equals(labelPropertyLang))
            .map(StrictLangValue::getValue)
            .findFirst())
        .orElse("<WARNING: LABEL MISSING>");
  }

  private String toInlineCsv(List<String> row) {
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, '|', '\'', '\'', "");
    csvWriter.writeNext(row.toArray(new String[]{}), false);
    return writer.toString();
  }

}
