package fi.thl.termed.web.system.admin;

import static com.google.common.collect.Iterators.partition;
import static fi.thl.termed.util.service.SaveMode.INSERT;
import static fi.thl.termed.util.service.WriteOptions.defaultOpts;
import static fi.thl.termed.util.service.WriteOptions.opts;
import static java.util.stream.Collectors.toList;
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
import fi.thl.termed.service.node.specification.NodesByGraphId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.SequenceService;
import fi.thl.termed.util.service.Service;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
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
  private Service<GraphId, Graph> graphService;
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
        // nodes are cascaded when revisions are deleted
        revisionService.delete(revisionService.getKeys(user), defaultOpts(), user);

        Revision r = Revision.of(revisionSeq.getAndAdvance(user), user.getUsername(), new Date());
        revisionService.save(r, INSERT, defaultOpts(), user);

        Function<List<Node>, List<Tuple2<RevisionType, Node>>> pairEachWithRevType =
            nodes -> nodes.stream().map(n -> Tuple.of(RevisionType.INSERT, n)).collect(toList());

        Consumer<List<Node>> saveNodeBatch = nodeBatch -> nodeRevisionService
            .save(pairEachWithRevType.apply(nodeBatch), SaveMode.INSERT, opts(r.getNumber()), user);

        graphService.getKeys(user).forEach(graphId -> {
          Specification<NodeId, Node> nodeSpec = new NodesByGraphId(graphId.getId());
          try (Stream<Node> graphNodes = nodeService.getValueStream(nodeSpec, user)) {
            partition(graphNodes.iterator(), 5000).forEachRemaining(saveNodeBatch);
          }
        });

      } catch (RuntimeException | Error e) {
        manager.rollback(tx);
        throw e;
      }

      manager.commit(tx);

      log.info("Done");
    }
  }

}
