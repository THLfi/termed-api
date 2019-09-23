package fi.thl.termed.web.node;

import static fi.thl.termed.util.collect.StreamUtils.findFirstAndClose;
import static fi.thl.termed.util.collect.StreamUtils.toImmutableListAndClose;

import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.util.CsvToNodes;
import fi.thl.termed.util.csv.CsvDelimiter;
import fi.thl.termed.util.csv.CsvLineBreak;
import fi.thl.termed.util.csv.CsvOptions;
import fi.thl.termed.util.csv.CsvQuoteChar;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.annotation.PostCsvMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeCsvSaveController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;

  @PostCsvMapping(path = "/nodes", produces = {})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void post(
      @RequestParam(value = "delimiter", defaultValue = "COMMA") CsvDelimiter delimiter,
      @RequestParam(value = "quoteChar", defaultValue = "DOUBLE_QUOTE") CsvQuoteChar quoteChar,
      @RequestParam(value = "lineBreak", defaultValue = "LF") CsvLineBreak lineBreak,
      @RequestParam(value = "quoteAll", defaultValue = "false") boolean quoteAll,
      @RequestParam(value = "charset", defaultValue = "UTF-8") Charset charset,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "false") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "false") boolean generateUris,
      @AuthenticationPrincipal User user,
      HttpServletRequest request) throws IOException {

    List<Type> types = toImmutableListAndClose(typeService.values(Queries.matchAll(), user));
    Function<Specification<NodeId, Node>, Optional<NodeId>> referenceIdResolver =
        specification -> findFirstAndClose(nodeService.keys(Queries.query(specification), user));

    try (InputStream input = request.getInputStream()) {
      Stream<Node> nodes = new CsvToNodes(types, referenceIdResolver).parseNodesFromCsv(
          CsvOptions.builder()
              .delimiter(delimiter)
              .quoteChar(quoteChar)
              .escapeChar(quoteChar.value())
              .recordSeparator(lineBreak)
              .charset(charset)
              .quoteAll(quoteAll)
              .build(),
          input);

      nodeService.save(
          nodes,
          SaveMode.saveMode(mode),
          WriteOptions.opts(sync, generateCodes, generateUris),
          user);
    }
  }

  @PostCsvMapping(path = "/graphs/{graphId}/nodes", produces = {})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void post(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(value = "delimiter", defaultValue = "COMMA") CsvDelimiter delimiter,
      @RequestParam(value = "quoteChar", defaultValue = "DOUBLE_QUOTE") CsvQuoteChar quoteChar,
      @RequestParam(value = "lineBreak", defaultValue = "LF") CsvLineBreak lineBreak,
      @RequestParam(value = "quoteAll", defaultValue = "false") boolean quoteAll,
      @RequestParam(value = "charset", defaultValue = "UTF-8") Charset charset,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "false") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "false") boolean generateUris,
      @AuthenticationPrincipal User user,
      HttpServletRequest request) throws IOException {

    if (!graphService.exists(GraphId.of(graphId), user)) {
      throw new NotFoundException();
    }

    List<Type> types = toImmutableListAndClose(typeService.values(Queries.matchAll(), user));
    Function<Specification<NodeId, Node>, Optional<NodeId>> referenceIdResolver =
        specification -> findFirstAndClose(nodeService.keys(Queries.query(specification), user));

    try (InputStream input = request.getInputStream()) {
      Stream<Node> nodes = new CsvToNodes(types, referenceIdResolver).parseNodesFromCsv(
          GraphId.of(graphId),
          CsvOptions.builder()
              .delimiter(delimiter)
              .quoteChar(quoteChar)
              .escapeChar(quoteChar.value())
              .recordSeparator(lineBreak)
              .charset(charset)
              .quoteAll(quoteAll)
              .build(),
          input);

      nodeService.save(
          nodes.map(node -> TypeId.of(node.getTypeId(), graphId).equals(node.getType())
              ? node
              : Node.builder()
                  .id(node.getId(), TypeId.of(node.getTypeId(), graphId))
                  .copyOptionalsFrom(node)
                  .build()),
          SaveMode.saveMode(mode),
          WriteOptions.opts(sync, generateCodes, generateUris),
          user);
    }
  }

  @PostCsvMapping(path = "/graphs/{graphId}/types/{typeId}/nodes", produces = {})
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void post(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(value = "delimiter", defaultValue = "COMMA") CsvDelimiter delimiter,
      @RequestParam(value = "quoteChar", defaultValue = "DOUBLE_QUOTE") CsvQuoteChar quoteChar,
      @RequestParam(value = "lineBreak", defaultValue = "LF") CsvLineBreak lineBreak,
      @RequestParam(value = "quoteAll", defaultValue = "false") boolean quoteAll,
      @RequestParam(value = "charset", defaultValue = "UTF-8") Charset charset,
      @RequestParam(name = "mode", defaultValue = "upsert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "false") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "false") boolean generateUris,
      @AuthenticationPrincipal User user,
      HttpServletRequest request) throws IOException {

    TypeId type = TypeId.of(typeId, graphId);

    if (!typeService.exists(type, user)) {
      throw new NotFoundException();
    }

    List<Type> types = toImmutableListAndClose(typeService.values(Queries.matchAll(), user));
    Function<Specification<NodeId, Node>, Optional<NodeId>> referenceIdResolver =
        specification -> findFirstAndClose(nodeService.keys(Queries.query(specification), user));

    try (InputStream input = request.getInputStream()) {
      Stream<Node> nodes = new CsvToNodes(types, referenceIdResolver).parseNodesFromCsv(
          type,
          CsvOptions.builder()
              .delimiter(delimiter)
              .quoteChar(quoteChar)
              .escapeChar(quoteChar.value())
              .recordSeparator(lineBreak)
              .charset(charset)
              .quoteAll(quoteAll)
              .build(),
          input);

      nodeService.save(
          nodes.map(node -> type.equals(node.getType())
              ? node
              : Node.builder()
                  .id(node.getId(), type)
                  .copyOptionalsFrom(node)
                  .build()),
          SaveMode.saveMode(mode),
          WriteOptions.opts(sync, generateCodes, generateUris),
          user);
    }
  }

}
