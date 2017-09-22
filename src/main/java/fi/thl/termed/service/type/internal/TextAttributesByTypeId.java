package fi.thl.termed.service.type.internal;

import java.util.Objects;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class TextAttributesByTypeId
    extends AbstractSqlSpecification<TextAttributeId, TextAttribute> {

  private TypeId typeId;

  public TextAttributesByTypeId(TypeId typeId) {
    this.typeId = typeId;
  }

  @Override
  public boolean test(TextAttributeId key, TextAttribute value) {
    return Objects.equals(key.getDomainId(), typeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "domain_graph_id = ? and domain_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{typeId.getGraphId(), typeId.getId()};
  }

}
