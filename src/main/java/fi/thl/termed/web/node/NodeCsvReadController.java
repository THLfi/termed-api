package fi.thl.termed.web.node;

import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;
import static fi.thl.termed.util.query.Queries.matchAll;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDate.now;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.select.NodeSelects;
import fi.thl.termed.service.node.sort.NodeSorts;
import fi.thl.termed.service.node.specification.NodeSpecifications;
import fi.thl.termed.service.node.util.NodesToCsv;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.csv.CsvDelimiter;
import fi.thl.termed.util.csv.CsvLineBreak;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.csv.CsvQuoteChar;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Sort;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetCsvMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
      @RequestParam(value = "delimiter", defaultValue = "COMMA") CsvDelimiter delimiter,
      @RequestParam(value = "quoteChar", defaultValue = "DOUBLE_QUOTE") CsvQuoteChar quoteChar,
      @RequestParam(value = "lineBreak", defaultValue = "LF") CsvLineBreak lineBreak,
      @RequestParam(value = "quoteAll", defaultValue = "false") boolean quoteAll,
      @RequestParam(value = "charset", defaultValue = "UTF-8") Charset charset,
      @RequestParam(name = "download", defaultValue = "true") boolean download,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    if (download) {
      String filename = now() + "-nodes.csv";
      response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      response.setContentType(MediaTypes.TEXT_CSV_VALUE);
    } else {
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
    }

    response.setCharacterEncoding(UTF_8.toString());

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));

    Specification<NodeId, Node> spec = NodeSpecifications
        .specifyByQuery(graphs, types, types, where);
    List<Select> selects = NodeSelects.parse(select);
    List<Select> qSelects = NodeSelects.qualify(types, types, selects);
    List<Sort> sorts = NodeSorts.parse(sort);

    try (Stream<Node> nodes = nodeService
        .values(new Query<>(qSelects, spec, sorts, max), user);
        OutputStream out = response.getOutputStream()) {
      CsvOptions csvOptions = CsvOptions.builder()
          .delimiter(delimiter)
          .quoteChar(quoteChar)
          .escapeChar(quoteChar.value())
          .recordSeparator(lineBreak)
          .quoteAll(quoteAll)
          .charset(charset).build();

      NodesToCsv.writeAsCsv(nodes, selects, csvOptions, out);
    }
  }

  @GetCsvMapping("/graphs/{graphId}/nodes")
  public void get(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "select", defaultValue = "*") List<String> select,
      @RequestParam(value = "where", defaultValue = EMPTY_LIST) List<String> where,
      @RequestParam(value = "sort", defaultValue = EMPTY_LIST) List<String> sort,
      @RequestParam(value = "max", defaultValue = "-1") Integer max,
      @RequestParam(value = "delimiter", defaultValue = "COMMA") CsvDelimiter delimiter,
      @RequestParam(value = "quoteChar", defaultValue = "DOUBLE_QUOTE") CsvQuoteChar quoteChar,
      @RequestParam(value = "lineBreak", defaultValue = "LF") CsvLineBreak lineBreak,
      @RequestParam(value = "quoteAll", defaultValue = "false") boolean quoteAll,
      @RequestParam(value = "charset", defaultValue = "UTF-8") Charset charset,
      @RequestParam(name = "download", defaultValue = "true") boolean download,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    Graph graph = graphService.get(GraphId.of(graphId), user)
        .orElseThrow(NotFoundException::new);

    if (download) {
      String filename = format("%s-%s.csv", now(),
          graph.getCode().orElse(graph.getId().toString()));
      response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      response.setContentType(MediaTypes.TEXT_CSV_VALUE);
    } else {
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
    }

    response.setCharacterEncoding(UTF_8.toString());

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));
    List<Type> domains = toImmutableListAndClose(
        typeService.values(Queries.query(TypesByGraphId.of(graphId)), user));

    Specification<NodeId, Node> spec = NodeSpecifications
        .specifyByQuery(graphs, types, domains, where);
    List<Select> selects = NodeSelects.parse(select);
    List<Select> qSelects = NodeSelects.qualify(types, domains, selects);
    List<Sort> sorts = NodeSorts.parse(sort);

    try (Stream<Node> nodes = nodeService
        .values(new Query<>(qSelects, spec, sorts, max), user);
        OutputStream out = response.getOutputStream()) {
      CsvOptions csvOptions = CsvOptions.builder()
          .delimiter(delimiter)
          .quoteChar(quoteChar)
          .escapeChar(quoteChar.value())
          .recordSeparator(lineBreak)
          .quoteAll(quoteAll)
          .charset(charset).build();

      NodesToCsv.writeAsCsv(nodes, selects, csvOptions, out);
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
      @RequestParam(value = "delimiter", defaultValue = "COMMA") CsvDelimiter delimiter,
      @RequestParam(value = "quoteChar", defaultValue = "DOUBLE_QUOTE") CsvQuoteChar quoteChar,
      @RequestParam(value = "lineBreak", defaultValue = "LF") CsvLineBreak lineBreak,
      @RequestParam(value = "quoteAll", defaultValue = "false") boolean quoteAll,
      @RequestParam(value = "charset", defaultValue = "UTF-8") Charset charset,
      @RequestParam(name = "download", defaultValue = "true") boolean download,
      @AuthenticationPrincipal User user,
      HttpServletResponse response) throws IOException {

    Graph graph = graphService.get(GraphId.of(graphId), user)
        .orElseThrow(NotFoundException::new);
    Type domain = typeService.get(TypeId.of(typeId, graphId), user)
        .orElseThrow(NotFoundException::new);

    if (download) {
      String filename = format("%s-%s-%s.csv", now(),
          graph.getCode().orElse(graph.getId().toString()), domain.getId());
      response.setHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
      response.setContentType(MediaTypes.TEXT_CSV_VALUE);
    } else {
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
    }

    response.setCharacterEncoding(UTF_8.toString());

    List<Graph> graphs = toImmutableListAndClose(graphService.values(matchAll(), user));
    List<Type> types = toImmutableListAndClose(typeService.values(matchAll(), user));

    Specification<NodeId, Node> spec = NodeSpecifications
        .specifyByQuery(graphs, types, domain, where);
    List<Select> selects = NodeSelects.parse(select);
    List<Select> qSelects = NodeSelects.qualify(types, of(domain), selects);
    List<Sort> sorts = NodeSorts.parse(sort);

    try (Stream<Node> nodes = nodeService
        .values(new Query<>(qSelects, spec, sorts, max), user);
        OutputStream out = response.getOutputStream()) {
      CsvOptions csvOptions = CsvOptions.builder()
          .delimiter(delimiter)
          .quoteChar(quoteChar)
          .escapeChar(quoteChar.value())
          .recordSeparator(lineBreak)
          .quoteAll(quoteAll)
          .charset(charset).build();

      NodesToCsv.writeAsCsv(nodes, selects, csvOptions, out);
    }
  }

}
