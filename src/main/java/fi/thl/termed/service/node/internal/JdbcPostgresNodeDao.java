package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.dao.AbstractJdbcPostgresDao;
import fi.thl.termed.util.dao.SystemDao;
import javax.sql.DataSource;
import org.joda.time.DateTime;

public class JdbcPostgresNodeDao extends AbstractJdbcPostgresDao<NodeId, Node> {

  public JdbcPostgresNodeDao(SystemDao<NodeId, Node> delegate, DataSource dataSource) {
    super(delegate, dataSource, "node");
  }

  @Override
  protected String[] toRow(NodeId k, Node v) {
    return new String[]{
        k.getTypeGraphId().toString(),
        k.getTypeId(),
        k.getId().toString(),
        v.getCode().orElse(null),
        v.getUri().orElse(null),
        v.getNumber().toString(),
        v.getCreatedBy(),
        new DateTime(v.getCreatedDate()).toString(),
        v.getLastModifiedBy(),
        new DateTime(v.getLastModifiedDate()).toString()
    };
  }

}
