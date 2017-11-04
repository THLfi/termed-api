package fi.thl.termed.web.system.admin;

import static com.google.common.collect.Iterators.partition;
import static fi.thl.termed.util.service.SaveMode.INSERT;
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
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/revisions")
public class RevisionAdminController {

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
  @DeleteMapping
  @ResponseStatus(NO_CONTENT)
  public void purgeRevisions(@AuthenticationPrincipal User user) {
    if (user.getAppRole() == AppRole.ADMIN || user.getAppRole() == AppRole.SUPERUSER) {
      log.warn("Purge revisions (user: {})", user.getUsername());

      TransactionStatus tx = manager.getTransaction(new DefaultTransactionDefinition());

      try {
        revisionService.delete(revisionService.getKeys(user), defaultOpts(), user);

        Revision r = Revision.of(revisionSeq.getAndAdvance(user), user.getUsername(), new Date());
        revisionService.save(r, INSERT, defaultOpts(), user);

        try (Stream<Node> nodes = nodeService.getValueStream(user)) {
          partition(nodes.iterator(), 5000).forEachRemaining(nodeBatch -> {
            List<Tuple2<RevisionType, Node>> nodeRevisions = nodeBatch.stream()
                .map(n -> Tuple.of(RevisionType.INSERT, n)).collect(Collectors.toList());
            nodeRevisionService.save(nodeRevisions, INSERT, opts(r.getNumber()), user);
          });
        }

      } catch (RuntimeException | Error e) {
        manager.rollback(tx);
        throw e;
      }

      manager.commit(tx);

      log.info("Done");
    }
  }

}
