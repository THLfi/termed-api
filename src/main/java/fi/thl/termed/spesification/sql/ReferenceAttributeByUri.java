package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import java.util.UUID;

import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

public class ReferenceAttributeByUri
    extends AbstractSpecification<ReferenceAttributeId, ReferenceAttribute>
    implements SqlSpecification<ReferenceAttributeId, ReferenceAttribute> {

  private UUID schemeId;
  private String uri;

  public ReferenceAttributeByUri(UUID schemeId, String uri) {
    this.schemeId = schemeId;
    this.uri = uri;
  }

  @Override
  protected boolean accept(ReferenceAttributeId key, ReferenceAttribute value) {
    return Objects.equals(key.getDomainId().getSchemeId(), schemeId) &&
           Objects.equals(value.getUri(), uri);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and uri = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId, uri};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReferenceAttributeByUri that = (ReferenceAttributeByUri) o;
    return Objects.equals(schemeId, that.schemeId) &&
           Objects.equals(uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemeId, uri);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("uri", uri)
        .toString();
  }

}
