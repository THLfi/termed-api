package fi.thl.termed.service.node.internal;

import static java.util.Optional.ofNullable;

import fi.thl.termed.domain.NodeAttributeValueId;
import fi.thl.termed.domain.NodeId;
import fi.thl.termed.domain.RevisionId;
import fi.thl.termed.domain.RevisionType;
import fi.thl.termed.domain.StrictLangValue;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.dao.AbstractJdbcPostgresDao;
import fi.thl.termed.util.dao.SystemDao2;
import java.util.Optional;
import javax.sql.DataSource;

public class JdbcPostgresNodeTextAttributeValueRevisionDao extends
    AbstractJdbcPostgresDao<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> {

  public JdbcPostgresNodeTextAttributeValueRevisionDao(
      SystemDao2<RevisionId<NodeAttributeValueId>, Tuple2<RevisionType, StrictLangValue>> delegate,
      DataSource dataSource) {
    super(delegate, dataSource, "node_text_attribute_value_aud");
  }

  @Override
  protected String[] toRow(RevisionId<NodeAttributeValueId> k,
      Tuple2<RevisionType, StrictLangValue> v) {
    NodeAttributeValueId nodeAttributeValueId = k.getId();
    NodeId nodeId = nodeAttributeValueId.getNodeId();

    Optional<StrictLangValue> langValue = ofNullable(v._2);

    return new String[]{
        nodeId.getTypeGraphId().toString(),
        nodeId.getTypeId(),
        nodeId.getId().toString(),
        nodeAttributeValueId.getAttributeId(),
        nodeAttributeValueId.getIndex().toString(),
        langValue.map(StrictLangValue::getLang).orElse(null),
        langValue.map(StrictLangValue::getValue).orElse(null),
        langValue.map(StrictLangValue::getRegex).orElse(null),
        k.getRevision().toString(),
        v._1.toString()
    };
  }

}
