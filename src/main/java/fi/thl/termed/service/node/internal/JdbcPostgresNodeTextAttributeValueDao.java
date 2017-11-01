package fi.thl.termed.service.node.internal;

import static fi.thl.termed.util.postgresql.CopyManagerUtils.copyInAsCsv;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.dao.ForwardingSystemDao;
import fi.thl.termed.util.dao.SystemDao;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Implements faster bulk insert for Postgres. If backed database is not Postgres, forwards insert
 * to delegate.
 */
public class JdbcPostgresNodeTextAttributeValueDao extends
    ForwardingSystemDao<NodeAttributeValueId, StrictLangValue> {

  private Logger log = LoggerFactory.getLogger(getClass());

  private DataSource dataSource;

  public JdbcPostgresNodeTextAttributeValueDao(
      SystemDao<NodeAttributeValueId, StrictLangValue> delegate, DataSource dataSource) {
    super(delegate);
    this.dataSource = dataSource;
  }

  @Override
  public void insert(Map<NodeAttributeValueId, StrictLangValue> map) {
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

  private void copyIn(BaseConnection c, Map<NodeAttributeValueId, StrictLangValue> map) {
    String sql = "COPY node_text_attribute_value FROM STDIN CSV";
    int batchSize = 10000;

    ProgressReporter reporter = new ProgressReporter(log, "Insert", batchSize, map.size());

    List<String[]> rows = new ArrayList<>();

    map.forEach((k, v) -> {
      NodeId nodeId = k.getNodeId();

      rows.add(new String[]{
          nodeId.getTypeGraphId().toString(),
          nodeId.getTypeId(),
          nodeId.getId().toString(),
          k.getAttributeId(),
          k.getIndex().toString(),
          v.getLang(),
          v.getValue(),
          v.getRegex()
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
