package fi.thl.termed.web.node;

import static fi.thl.termed.service.node.specification.NodeSpecifications.specifyByQuery;
import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static fi.thl.termed.util.spring.SpEL.EMPTY_LIST;
import static java.lang.String.format;
import static java.lang.String.join;
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
import fi.thl.termed.service.node.select.Selects;
import fi.thl.termed.service.node.util.NodesToCsv;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.csv.CsvDelimiter;
import fi.thl.termed.util.csv.CsvLineBreak;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.csv.CsvQuoteChar;
import fi.thl.termed.util.query.MatchAll;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.Service2;
import fi.thl.termed.util.spring.annotation.GetCsvMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.http.MediaTypes;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
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
  private Service2<GraphId, Graph> graphService;
  @Autowired
  private Service2<TypeId, Type> typeService;
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

    Set<Select> selects = Selects.parse(join(",", select));
    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    Specification<NodeId, Node> spec = specifyByQuery(
        graphs, types, types, where);

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {

      try (OutputStream out = response.getOutputStream()) {
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

    Set<Select> selects = Selects.parse(join(",", select));
    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    List<Type> graphTypes = toListAndClose(
        typeService.values(new Query<>(new TypesByGraphId(graphId)), user));
    Specification<NodeId, Node> spec = specifyByQuery(
        graphs, types, graphTypes, where);

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {

      try (OutputStream out = response.getOutputStream()) {
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
    Type domain = typeService.get(new TypeId(typeId, graphId), user)
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

    Set<Select> selects = Selects.parse(join(",", select));
    List<Graph> graphs = toListAndClose(graphService.values(new Query<>(new MatchAll<>()), user));
    List<Type> types = toListAndClose(typeService.values(new Query<>(new MatchAll<>()), user));
    Specification<NodeId, Node> spec = specifyByQuery(graphs, types, domain, where);

    try (Stream<Node> nodes = nodeService
        .getValueStream(new Query<>(selects, spec, sort, max), user)) {
      try (OutputStream out = response.getOutputStream()) {
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

}
