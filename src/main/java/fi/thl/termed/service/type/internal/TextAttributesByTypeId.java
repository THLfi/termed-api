package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class TextAttributesByTypeId
    extends AbstractSqlSpecification<TextAttributeId, TextAttribute> {

  private TypeId typeId;

  TextAttributesByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(TextAttributeId key, TextAttribute value) {
    return Objects.equals(key.getDomainId(), typeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of(
        "domain_graph_id = ? and domain_id = ?", typeId.getGraphId(), typeId.getId());
  }

}
