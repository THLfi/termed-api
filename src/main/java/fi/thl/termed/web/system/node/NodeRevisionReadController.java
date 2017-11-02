package fi.thl.termed.web.system.node;

import static fi.thl.termed.util.collect.StreamUtils.toListAndClose;
import static java.util.Comparator.comparing;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.ObjectRevision;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeRevisionsByNodeId;
import fi.thl.termed.service.node.specification.NodeRevisionsLessOrEqualToRevision;
import fi.thl.termed.util.collect.Tuple2;
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
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionReadService;

  @Autowired
  private Service<Long, Revision> revisionService;

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}/revisions")
  public List<ObjectRevision<NodeId>> getNodeRevisions(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @AuthenticationPrincipal User user) {
    return toListAndClose(nodeRevisionReadService
        .getKeyStream(new NodeRevisionsByNodeId(new NodeId(id, typeId, graphId)), user)
        .map(revisionId -> {
          Revision revision = revisionService.get(revisionId.getRevision(), user)
              .orElseThrow(IllegalStateException::new);
          return new ObjectRevision<>(revision, null, revisionId.getId());
        }));
  }

  @GetJsonMapping("/graphs/{graphId}/types/{typeId}/nodes/{id}/revisions/{number}")
  public ObjectRevision<Node> getNodeRevision(
      @PathVariable("graphId") UUID graphId,
      @PathVariable("typeId") String typeId,
      @PathVariable("id") UUID id,
      @PathVariable("number") Long number,
      @AuthenticationPrincipal User user) {
    RevisionId<NodeId> revisionId = RevisionId.of(new NodeId(id, typeId, graphId), number);

    if (nodeRevisionReadService.exists(revisionId, user)) {
      Revision revision = revisionService.get(number, user)
          .orElseThrow(IllegalStateException::new);
      Tuple2<RevisionType, Node> nodeRevision = nodeRevisionReadService.get(revisionId, user)
          .orElseThrow(IllegalStateException::new);
      return new ObjectRevision<>(revision, nodeRevision._1, nodeRevision._2);
    }

    try (Stream<RevisionId<NodeId>> revisionIds = nodeRevisionReadService
        .getKeyStream(new NodeRevisionsLessOrEqualToRevision(revisionId), user)) {

      RevisionId<NodeId> maxRevisionLessOrEqualToRequested = revisionIds
          .max(comparing(RevisionId::getRevision))
          .orElseThrow(NotFoundException::new);

      Revision revision = revisionService
          .get(maxRevisionLessOrEqualToRequested.getRevision(), user)
          .orElseThrow(IllegalStateException::new);
      Tuple2<RevisionType, Node> nodeRevision = nodeRevisionReadService
          .get(maxRevisionLessOrEqualToRequested, user)
          .orElseThrow(IllegalStateException::new);
      return new ObjectRevision<>(revision, nodeRevision._1, nodeRevision._2);
    }
  }

}
