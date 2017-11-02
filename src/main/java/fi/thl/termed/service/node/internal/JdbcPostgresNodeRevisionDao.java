package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.postgresql.CopyManagerUtils.copyInAsCsv;
import static java.util.Optional.ofNullable;

import com.google.common.base.Strings;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.ForwardingSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Implements faster bulk insert for Postgres. If backed database is not Postgres, forwards insert
 * to delegate.
 */
public class JdbcPostgresNodeRevisionDao extends
    ForwardingSystemDao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private DataSource dataSource;

  public JdbcPostgresNodeRevisionDao(
      SystemDao<RevisionId<NodeId>, Tuple2<RevisionType, Node>> delegate, DataSource dataSource) {
    super(delegate);
    this.dataSource = dataSource;
  }

  @Override
  public void insert(Map<RevisionId<NodeId>, Tuple2<RevisionType, Node>> map) {
    Connection c = DataSourceUtils.getConnection(dataSource);

    try {
      if (c.isWrapperFor(BaseConnection.class)) {
        copyIn(c.unwrap(BaseConnection.class), map);
        return;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      DataSourceUtils.releaseConnection(c, dataSource);
    }

    super.insert(map);
  }

  private void copyIn(BaseConnection c, Map<RevisionId<NodeId>, Tuple2<RevisionType, Node>> map) {
    String sql = "COPY node_aud FROM STDIN CSV";
    int batchSize = 10000;

    ProgressReporter reporter = new ProgressReporter(log, "Insert", batchSize, map.size());

    List<String[]> rows = new ArrayList<>();

    map.forEach((k, v) -> {
      NodeId nodeId = k.getId();
      Optional<Node> node = ofNullable(v._2);

      rows.add(new String[]{
          nodeId.getTypeGraphId().toString(),
          nodeId.getTypeId(),
          nodeId.getId().toString(),
          node.map(Node::getCode).map(Strings::emptyToNull).orElse(null),
          node.map(Node::getUri).map(Strings::emptyToNull).orElse(null),
          node.map(Node::getNumber).map(Object::toString).orElse(null),
          node.map(Node::getCreatedBy).orElse(null),
          node.map(Node::getCreatedDate).map(DateTime::new).map(Object::toString).orElse(null),
          node.map(Node::getLastModifiedBy).orElse(null),
          node.map(Node::getLastModifiedDate).map(DateTime::new).map(Object::toString).orElse(null),
          k.getRevision().toString(),
          v._1.toString()
      });

      if (rows.size() % batchSize == 0) {
        copyInAsCsv(c, sql, rows);
        rows.clear();
      }

      reporter.tick();
    });

    if (!rows.isEmpty()) {
      copyInAsCsv(c, sql, rows);
      if (map.size() >= 1000) {
        reporter.report();
      }
    }
  }

}
