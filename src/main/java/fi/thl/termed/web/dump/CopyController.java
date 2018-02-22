package fi.thl.termed.web.dump;

import static fi.thl.termed.util.service.SaveMode.saveMode;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static fi.thl.termed.util.spring.SpEL.RANDOM_UUID;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Multimaps;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dump")
public class CopyController {

  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private Service<TypeId, Type> typeService;
  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private PlatformTransactionManager manager;

  @PostJsonMapping(params = "copy=true", produces = MediaType.TEXT_PLAIN_VALUE)
  public String copy(
      @RequestParam("sourceGraphId") UUID sourceGraphId,
      @RequestParam(name = "targetGraphId", defaultValue = RANDOM_UUID) UUID targetGraphId,
      @RequestParam(name = "mode", defaultValue = "insert") String mode,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {

    TransactionStatus tx = manager.getTransaction(new DefaultTransactionDefinition());

    try {
      Graph sourceGraph = graphService.get(GraphId.of(sourceGraphId), user)
          .orElseThrow(NotFoundException::new);

      graphService.save(mapGraphToGraph(sourceGraph, targetGraphId),
          saveMode(mode), opts(sync), user);

      typeService.save(
          typeService.getValues(new TypesByGraphId(sourceGraphId), user).stream()
              .map(t -> mapTypeToGraph(t, targetGraphId))
              .collect(toList()),
          saveMode(mode), opts(sync), user);

      try (Stream<Node> nodes = nodeService
          .getValueStream(new NodesByGraphId(sourceGraphId), user)) {
        nodeService.save(
            mapNodesToGraph(nodes, targetGraphId).collect(toList()),
            saveMode(mode), opts(sync), user);
      }

    } catch (RuntimeException | Error e) {
      manager.rollback(tx);
      throw e;
    }

    manager.commit(tx);

    return targetGraphId.toString();
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
            .map(a -> mapReferenceAttributesTo(a, targetTypeId))
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

  private ReferenceAttribute mapReferenceAttributesTo(ReferenceAttribute a, TypeId typeId) {
    return ReferenceAttribute.builder()
        .id(a.getId(), typeId)
        .range(a.getRange().equals(a.getDomain()) ? typeId : a.getRange())
        .copyOptionalsFrom(a)
        .build();
  }

  private Stream<Node> mapNodesToGraph(Stream<Node> nodes, UUID graphId) {
    return nodes.peek(node -> node.setType(TypeId.of(node.getTypeId(), graphId)));
  }

}
