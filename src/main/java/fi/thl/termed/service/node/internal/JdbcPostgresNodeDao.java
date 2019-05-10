package fi.thl.termed.service.node.internal;

import com.google.common.base.Strings;
import fi.thl.termed.domain.Node;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.util.dao.AbstractJdbcPostgresDao;
import fi.thl.termed.util.dao.SystemDao;
import javax.sql.DataSource;

public class JdbcPostgresNodeDao extends AbstractJdbcPostgresDao<NodeId, Node> {

  public JdbcPostgresNodeDao(SystemDao<NodeId, Node> delegate, DataSource dataSource) {
    super(delegate, dataSource, "node", true);
  }

  @Override
  protected String[] toRow(NodeId k, Node v) {
    return new String[]{
        k.getTypeGraphId().toString(),
        k.getTypeId(),
        k.getId().toString(),
        v.getCode().map(Strings::emptyToNull).orElse(null),
        v.getUri().map(Strings::emptyToNull).orElse(null),
        v.getNumber().toString(),
        v.getCreatedBy(),
        v.getCreatedDate().toString(),
        v.getLastModifiedBy(),
        v.getLastModifiedDate().toString()
    };
  }

}
