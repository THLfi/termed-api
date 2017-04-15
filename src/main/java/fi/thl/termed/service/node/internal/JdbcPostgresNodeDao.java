package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.postgresql.CopyManagerUtils.copyInAsCsv;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.ProgressReporter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.joda.time.DateTime;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Implements faster bulk insert for Postgres. If backed database is not Postgres, delegates back to
 * generic JdbcNodeDao insert.
 */
public class JdbcPostgresNodeDao extends JdbcNodeDao {

  private DataSource dataSource;

  public JdbcPostgresNodeDao(DataSource dataSource) {
    super(dataSource);
    this.dataSource = dataSource;
  }

  @Override
  public void insert(Map<NodeId, Node> map) {
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

  private void copyIn(BaseConnection c, Map<NodeId, Node> map) {
    String sql = "COPY node FROM STDIN CSV";
    int batchSize = 10000;

    ProgressReporter reporter = new ProgressReporter(log, "Insert", batchSize, map.size());

    List<String[]> rows = new ArrayList<>();

    map.forEach((k, v) -> {
      rows.add(new String[]{
          k.getTypeGraphId().toString(),
          k.getTypeId(),
          k.getId().toString(),
          v.getCode(),
          v.getUri(),
          v.getCreatedBy(),
          new DateTime(v.getCreatedDate()).toString(),
          v.getLastModifiedBy(),
          new DateTime(v.getLastModifiedDate()).toString()
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

    if (map.size() >= 1000) {
      // analyze table to speed subsequent node attribute value inserts
      analyzeTable(c);
    }
  }

  private void analyzeTable(BaseConnection c) {
    try (Statement s = c.createStatement()) {
      s.executeUpdate("ANALYZE node");
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
