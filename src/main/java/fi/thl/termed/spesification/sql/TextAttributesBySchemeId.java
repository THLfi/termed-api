package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import java.util.UUID;

import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class TextAttributesBySchemeId
    extends AbstractSpecification<TextAttributeId, TextAttribute>
    implements SqlSpecification<TextAttributeId, TextAttribute> {

  private UUID schemeId;

  public TextAttributesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  @Override
  public boolean accept(TextAttributeId key, TextAttribute value) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextAttributesBySchemeId that = (TextAttributesBySchemeId) o;
    return Objects.equals(schemeId, that.schemeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .toString();
  }

}
