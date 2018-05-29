package fi.thl.termed.web.admin;

import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.service.node.specification.NodesByTypeId;
import fi.thl.termed.service.type.specification.TypesByGraphId;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeCodeAdminController {

  @Autowired
  private Service<TypeId, Type> typeService;

  @Autowired
  private Service<NodeId, Node> nodeService;

  @DeleteMapping("/graphs/{graphId}/node-codes")
  @ResponseStatus(NO_CONTENT)
  public void resetGraphNodeCodes(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      Map<TypeId, String> prefixes = typeService.getValues(new TypesByGraphId(graphId), user)
          .stream()
          .collect(Collectors.toMap(Type::identifier, Type::getNodeCodePrefixOrDefault));

      try (Stream<Node> stream = nodeService.getValueStream(new NodesByGraphId(graphId), user)) {
        List<Node> nodes = stream
            .peek(node -> node.setCode(prefixes.get(node.getType()) + node.getNumber()))
            .collect(toList());

        nodeService.save(nodes, SaveMode.UPDATE, opts(sync), user);
      }
    } else {
      throw new AccessDeniedException("");
    }
  }

  @DeleteMapping("/graphs/{graphId}/types/{typeId}/node-codes")
  @ResponseStatus(NO_CONTENT)
  public void resetTypeNodeCodes(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      String prefix = typeService.get(TypeId.of(typeId, graphId), user)
          .map(Type::getNodeCodePrefixOrDefault)
          .orElseThrow(NotFoundException::new);

      try (Stream<Node> stream = nodeService.getValueStream(
          and(new NodesByGraphId(graphId),
              new NodesByTypeId(typeId)), user)) {
        List<Node> nodes = stream
            .peek(node -> node.setCode(prefix + node.getNumber()))
            .collect(toList());

        nodeService.save(nodes, SaveMode.UPDATE, opts(sync), user);
      }
    } else {
      throw new AccessDeniedException("");
    }
  }

}