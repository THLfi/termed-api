package fi.thl.termed.web.admin;

import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.Revision;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import fi.thl.termed.util.spring.transaction.TransactionUtils;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
  private PlatformTransactionManager manager;

  /**
   * Deletes each revision with all included data, then initializes a new revision with the current
   * state of the database.
   */
  @DeleteMapping("/revisions")
  @ResponseStatus(NO_CONTENT)
  public void purgeRevisions(@AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.SUPERUSER) {
      log.warn("Purge revisions (user: {})", user.getUsername());

      TransactionUtils.runInTransaction(manager, () -> {
        // nodes are cascaded when revisions are deleted
        revisionService.delete(revisionService.getKeys(user), defaultOpts(), user);

        Long revision = revisionSeq.getAndAdvance(user);

        revisionService.save(
            Revision.of(revision, user.getUsername(), new Date()),
            SaveMode.INSERT, defaultOpts(), user);

        try (Stream<NodeId> nodeIds = nodeService.getKeyStream(user)) {
          WriteOptions revisionOpts = opts(revision);

          nodeIds
              .map(nodeId -> nodeService.get(nodeId, user))
              .map(Optional::get)
              .forEach(node -> nodeRevisionService
                  .save(Tuple.of(RevisionType.INSERT, node), SaveMode.INSERT, revisionOpts, user));
        }
        return null;
      });

      log.info("Done");
    }
  }

}
