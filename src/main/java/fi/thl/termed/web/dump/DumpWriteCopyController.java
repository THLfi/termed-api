package fi.thl.termed.web.dump;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static fi.thl.termed.util.spring.SpEL.RANDOM_UUID;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.Dump;
import fi.thl.termed.domain.DumpId;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DumpWriteCopyController {

  @Autowired
  private Service<DumpId, Dump> dumpService;

  @PostJsonMapping(path = "/graphs/{graphId}/dump", params = "copy=true",
      produces = APPLICATION_JSON_UTF8_VALUE)
  public GraphId copyDump(
      @PathVariable("graphId") UUID sourceGraphId,
      @RequestParam(name = "targetGraphId", defaultValue = RANDOM_UUID) UUID targetGraphId,
      @RequestParam(name = "mode", defaultValue = "insert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "generateCodes", defaultValue = "false") boolean generateCodes,
      @RequestParam(name = "generateUris", defaultValue = "false") boolean generateUris,
      @AuthenticationPrincipal User user) {

    try (Dump dump = dumpService.get(new DumpId(GraphId.of(sourceGraphId)), user)
        .orElseThrow(NotFoundException::new)) {
      dumpService.save(new Dump(
              dump.getGraphs().map(graph -> mapGraphToGraph(graph, targetGraphId)),
              dump.getTypes().map(type -> mapTypeToGraph(type, targetGraphId)),
              dump.getNodes().map(node -> mapNodeToGraph(node, targetGraphId))),
          saveMode(mode), opts(sync, generateCodes, generateUris), user);
    }

    return new GraphId(targetGraphId);
  }

  private Graph mapGraphToGraph(Graph sourceGraph, UUID targetGraphId) {
    return Graph.builder().id(targetGraphId)
        .copyOptionalsFrom(sourceGraph)
        .code(null) // set code to null to avoid duplicate codes
        .properties(Multimaps.transformEntries(sourceGraph.getProperties(),
            (k, v) -> k.toLowerCase().contains("label")
                ? LangValue.of(v.getLang(), v.getValue() + " (Copy)") : v))
        .build();
  }

  private Type mapTypeToGraph(Type sourceType, UUID graphId) {
    TypeId targetTypeId = TypeId.of(sourceType.getId(), graphId);
    return Type.builder()
        .id(targetTypeId)
        .copyOptionalsFrom(sourceType)
        .textAttributes(sourceType.getTextAttributes().stream()
            .map(a -> mapTextAttributeToType(a, targetTypeId))
            .collect(toList()))
        .referenceAttributes(sourceType.getReferenceAttributes().stream()
            .map(a -> mapReferenceAttributeTo(a, targetTypeId))
            .collect(toList()))
        .build();
  }

  private TextAttribute mapTextAttributeToType(TextAttribute a, TypeId typeId) {
    return TextAttribute.builder()
        .id(a.getId(), typeId)
        .regex(a.getRegex())
        .copyOptionalsFrom(a)
        .build();
  }

  private ReferenceAttribute mapReferenceAttributeTo(ReferenceAttribute a, TypeId typeId) {
    return ReferenceAttribute.builder()
        .id(a.getId(), typeId)
        .range(Objects.equals(a.getRangeGraphId(), a.getDomainGraphId())
            ? TypeId.of(a.getRangeId(), typeId.getGraphId())
            : a.getRange())
        .copyOptionalsFrom(a)
        .build();
  }

  private Node mapNodeToGraph(Node node, UUID graphId) {
    return Node.builder()
        .id(node.getId(), node.getTypeId(), graphId)
        .copyOptionalsFrom(node)
        .build();
  }

}
