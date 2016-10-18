package fi.thl.termed.service.scheme.specification;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class TextAttributesBySchemeId
    extends AbstractSqlSpecification<TextAttributeId, TextAttribute> {

  private UUID schemeId;

  public TextAttributesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean test(TextAttributeId key, TextAttribute value) {
    return Objects.equals(key.getDomainId().getSchemeId(), schemeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId};
  }

}
