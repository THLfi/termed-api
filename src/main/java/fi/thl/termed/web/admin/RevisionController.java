package fi.thl.termed.web.admin;

import static fi.thl.termed.util.query.AndSpecification.and;
import static fi.thl.termed.util.query.OrSpecification.or;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Graph;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.User;
import fi.thl.termed.service.node.specification.NodeRevisionsByGraphId;
import fi.thl.termed.service.node.specification.NodeRevisionsByRevisionType;
import fi.thl.termed.service.node.specification.NodeRevisionsLessOrEqualToRevisionNumber;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Queries;
import fi.thl.termed.util.query.Specifications;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.annotation.GetJsonMapping;
import fi.thl.termed.util.spring.annotation.PostJsonMapping;
import fi.thl.termed.util.spring.exception.NotFoundException;
import fi.thl.termed.util.spring.transaction.TransactionUtils;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RevisionController {

  private Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  private Service<Long, Revision> revisionService;
  @Autowired
  private SequenceService revisionSeq;
  @Autowired
  private Service<RevisionId<NodeId>, Tuple2<RevisionType, Node>> nodeRevisionService;
  @Autowired
  private Service<NodeId, Node> nodeService;
  @Autowired
  private Service<GraphId, Graph> graphService;
  @Autowired
  private PlatformTransactionManager manager;

  @GetJsonMapping("/revisions")
  public Stream<Revision> getRevisions(
      @RequestParam(value = "max", defaultValue = "50") int max,
      @AuthenticationPrincipal User user) {

    if (user.getAppRole() != AppRole.SUPERUSER) {
      throw new AccessDeniedException("");
    }

    Stream<Revision> revisions = revisionService
        .values(Queries.query(Specifications.matchAll()), user);

    return max > 0 ? revisions.limit(max) : revisions;
  }

  @PostJsonMapping(value = "/graphs/{graphId}", produces = {}, params = "targetRevision")
  @ResponseStatus(NO_CONTENT)
  public void revertGraphNodesToRevision(
      @PathVariable("graphId") UUID graphId,
      @RequestParam(name = "sync", defaultValue = "false") boolean sync,
      @RequestParam(name = "targetRevision") Long targetRevision,
      @AuthenticationPrincipal User user) {

    if (user.getAppRole() != AppRole.SUPERUSER) {
      throw new AccessDeniedException("");
    }

    if (!graphService.exists(GraphId.of(graphId), user)) {
      throw new NotFoundException("Graph not found: " + graphId);
    }

    if (!revisionService.exists(targetRevision, user)) {
      throw new NotFoundException("Revision not found: " + targetRevision);
    }

    log.warn("Reverting graph {} to revision {} (user: {})", graphId, targetRevision,
        user.getUsername());

    Map<NodeId, Long> nodeRevisionsLessOrEqualToTargetRevision;

    try (Stream<RevisionId<NodeId>> revisionIds = nodeRevisionService
        .keys(Queries.query(and(
            NodeRevisionsByGraphId.of(graphId),
            NodeRevisionsLessOrEqualToRevisionNumber.of(targetRevision),
            or(
                NodeRevisionsByRevisionType.of(RevisionType.INSERT),
                NodeRevisionsByRevisionType.of(RevisionType.UPDATE)))), user)) {
      nodeRevisionsLessOrEqualToTargetRevision = revisionIds
          .collect(toMap(RevisionId::getId, RevisionId::getRevision, Long::max));
    }

    nodeService.save(
        nodeRevisionsLessOrEqualToTargetRevision.entrySet().stream()
            .map(idAndRevision -> nodeRevisionService
                .get(RevisionId.of(idAndRevision.getKey(), idAndRevision.getValue()), user)
                .map(t -> t._2)
                .orElseThrow(IllegalStateException::new)),
        SaveMode.UPSERT, WriteOptions.opts(sync), user);

    log.info("Done");
  }

  /**
   * Deletes each revision with all included data, then initializes a new revision with the current
   * state of the database.
   */
  @DeleteMapping("/revisions")
  @ResponseStatus(NO_CONTENT)
  public void purgeRevisions(@AuthenticationPrincipal User user) {
    if (user.getAppRole() != AppRole.SUPERUSER) {
      throw new AccessDeniedException("");
    }

    log.warn("Deleting revision history (user: {})", user.getUsername());

    TransactionUtils.runInTransaction(manager, () -> {
      // node revisions are cascaded when revisions are deleted
      revisionService.delete(revisionService.keys(Queries.matchAll(), user), defaultOpts(), user);

      Long revision = revisionSeq.getAndAdvance(user);

      revisionService.save(
          Revision.of(revision, user.getUsername(), LocalDateTime.now()),
          SaveMode.INSERT, defaultOpts(), user);

      try (Stream<Node> nodes = nodeService.values(Queries.matchAll(), user)) {
        WriteOptions revisionOpts = opts(revision);
        nodeRevisionService.save(
            nodes.map(node -> Tuple.of(RevisionType.INSERT, node)),
            SaveMode.INSERT,
            revisionOpts,
            user);
      }
      return null;
    });

    log.info("Done");
  }

}
