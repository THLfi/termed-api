package fi.thl.termed.web.system.node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.specification.QueryModel;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.web.node.NodeControllerReadService;

@RestController
@RequestMapping("/api")
public class NodeReadController {

  @Autowired
  private NodeControllerReadService nodeReadService;

  @GetJsonMapping("/nodes")
  public List<Node> get(@ModelAttribute QueryModel qm, @AuthenticationPrincipal User user) {
    return nodeReadService.searchNodesOfAnyType(qm, user);
  }

  @GetJsonMapping("/graphs/{graphId}/nodes")
  public List<Node> get(@PathVariable("graphId") UUID graphId,
                        @ModelAttribute QueryModel qm,
                        @AuthenticationPrincipal User user) {
    return nodeReadService.searchNodesOfAnyTypeInGraph(graphId, qm, user);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes")
  public List<Node> get(@PathVariable("graphId") UUID graphId,
                        @PathVariable("typeId") String typeId,
                        @ModelAttribute QueryModel qm,
                        @AuthenticationPrincipal User user) {
    return nodeReadService.searchNodesOfType(graphId, typeId, qm, user);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}")
  public Node get(@PathVariable("graphId") UUID graphId,
                  @PathVariable("typeId") String typeId,
                  @PathVariable("id") UUID id,
                  @AuthenticationPrincipal User user) {
    return nodeReadService.getNodeById(id, typeId, graphId, user);
  }

}
