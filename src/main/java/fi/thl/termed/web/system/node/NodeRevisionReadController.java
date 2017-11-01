package fi.thl.termed.web.system.node;

import static java.util.Comparator.comparing;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeRevisionsByNodeId;
import fi.thl.termed.service.node.specification.NodeRevisionsLessOrEqualToRevision;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NodeRevisionReadController {

  @Autowired
  private Service<RevisionId<NodeId>, Revision<NodeId, Node>> nodeRevisionReadService;

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}/revisions")
  public List<RevisionId<NodeId>> getNodeRevisions(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User user) {
    return nodeRevisionReadService.getKeys(
        new NodeRevisionsByNodeId(new NodeId(id, typeId, graphId)), user);
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}/revisions/{number}")
  public Revision<NodeId, Node> getNodeRevision(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("number") Long number,
      @AuthenticationPrincipal User user) {
    RevisionId<NodeId> revisionId = RevisionId.of(new NodeId(id, typeId, graphId), number);

    if (nodeRevisionReadService.exists(revisionId, user)) {
      return nodeRevisionReadService.get(revisionId, user)
          .orElseThrow(IllegalStateException::new);
    }

    try (Stream<RevisionId<NodeId>> ids = nodeRevisionReadService
        .getKeyStream(new NodeRevisionsLessOrEqualToRevision(revisionId), user)) {

      RevisionId<NodeId> latestRevisionLessOrEqualToTargetRevision =
          ids.max(comparing(RevisionId::getRevision)).orElseThrow(NotFoundException::new);

      return nodeRevisionReadService.get(latestRevisionLessOrEqualToTargetRevision, user)
          .orElseThrow(IllegalStateException::new);
    }
  }

}
