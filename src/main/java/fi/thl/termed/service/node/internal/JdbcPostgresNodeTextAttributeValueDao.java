package fi.thl.termed.service.node.internal;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.dao.AbstractJdbcPostgresDao;
import fi.thl.termed.util.dao.SystemDao2;
import javax.sql.DataSource;

public class JdbcPostgresNodeTextAttributeValueDao extends
    AbstractJdbcPostgresDao<NodeAttributeValueId, StrictLangValue> {

  public JdbcPostgresNodeTextAttributeValueDao(
      SystemDao2<NodeAttributeValueId, StrictLangValue> delegate, DataSource dataSource) {
    super(delegate, dataSource, "node_text_attribute_value");
  }

  @Override
  protected String[] toRow(NodeAttributeValueId k, StrictLangValue v) {
    NodeId nodeId = k.getNodeId();

    return new String[]{
        nodeId.getTypeGraphId().toString(),
        nodeId.getTypeId(),
        nodeId.getId().toString(),
        k.getAttributeId(),
        k.getIndex().toString(),
        v.getLang(),
        v.getValue(),
        v.getRegex()
    };
  }

}
