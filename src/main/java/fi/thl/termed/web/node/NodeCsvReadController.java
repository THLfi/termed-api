package fi.thl.termed.web.node;

import static com.google.common.collect.Multimaps.filterKeys;
import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.query.OrSpecification.or;
import static fi.thl.termed.util.query.SpecificationUtils.simplify;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.opencsv.CSVWriter;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
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
import fi.thl.termed.service.node.select.Selects;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.TableUtils;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetCsvMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeCsvReadController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @GetCsvMapping("/nodes")
  public void get(
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);

    Specification<NodeId, Node> spec = simplify(or(toListAndClose(types.stream()
        .map(domain -> specifyByQuery(graphs, types, domain, join(" AND ", where))))));
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {

      response.setContentType("text/csv;charset=UTF-8");
      response.setCharacterEncoding(UTF_8.toString());

      try (OutputStream out = response.getOutputStream()) {
        Writer writer = new OutputStreamWriter(out, Charsets.UTF_8);
        CSVWriter csvWriter = new CSVWriter(writer, ',', '\"', '\\');

        List<Map<String, String>> rows = new ArrayList<>();
        nodes.forEach(n -> rows.add(nodeToMap(n, selects)));
        csvWriter.writeAll(TableUtils.toTable(rows));
        csvWriter.close();
      }
    }
  }

  @GetCsvMapping("/graphs/{graphId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    graphService.get(new GraphId(graphId), user).orElseThrow(NotFoundException::new);

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);

    Specification<NodeId, Node> spec = simplify(or(toListAndClose(
        typeService.getValueStream(new TypesByGraphId(graphId), user)
            .map(domain -> specifyByQuery(graphs, types, domain, join(" AND ", where))))));
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {

      response.setContentType("text/csv;charset=UTF-8");
      response.setCharacterEncoding(UTF_8.toString());

      try (OutputStream out = response.getOutputStream()) {
        Writer writer = new OutputStreamWriter(out, Charsets.UTF_8);
        CSVWriter csvWriter = new CSVWriter(writer, ',', '\"', '\\');

        List<Map<String, String>> rows = new ArrayList<>();
        nodes.forEach(n -> rows.add(nodeToMap(n, selects)));
        csvWriter.writeAll(TableUtils.toTable(rows));
        csvWriter.close();
      }
    }
  }

  @GetCsvMapping("/graphs/{graphId}/types/{typeId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    List<Graph> graphs = graphService.getValues(user);
    List<Type> types = typeService.getValues(user);
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, join(" AND ", where));
    Set<Select> selects = Selects.parse(join(",", select));

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {

      response.setContentType("text/csv;charset=UTF-8");
      response.setCharacterEncoding(UTF_8.toString());

      try (OutputStream out = response.getOutputStream()) {
        Writer writer = new OutputStreamWriter(out, Charsets.UTF_8);
        CSVWriter csvWriter = new CSVWriter(writer, ',', '\"', '\"', "\n");

        List<Map<String, String>> rows = new ArrayList<>();
        nodes.forEach(n -> rows.add(nodeToMap(n, selects)));
        csvWriter.writeAll(TableUtils.toTable(rows), false);
        csvWriter.close();
      }
    }
  }

  private Map<String, String> nodeToMap(Node node, Set<Select> s) {
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

  private String toInlineCsv(List<String> row) {
    StringWriter writer = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(writer, '|', '\'', '\'', "");
    csvWriter.writeNext(row.toArray(new String[row.size()]), false);
    return writer.toString();
  }

}
