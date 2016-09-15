package fi.thl.termed.spesification.sql;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.SqlSpecification;

public class TextAttributesBySchemeIds
    extends AbstractSpecification<TextAttributeId, TextAttribute>
    implements SqlSpecification<TextAttributeId, TextAttribute> {

  private Set<UUID> schemeIds;

  public TextAttributesBySchemeIds(List<UUID> schemeIds) {
    this.schemeIds = ImmutableSet.copyOf(schemeIds);
  }

  @Override
  public boolean accept(TextAttributeId key, TextAttribute value) {
    return schemeIds.contains(key.getDomainId().getSchemeId());
  }

  @Override
  public String sqlQueryTemplate() {
    List<String> placeholders = Collections.nCopies(schemeIds.size(), "?");
    return "scheme_id id in (" + Joiner.on(", ").join(placeholders) + ")";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return schemeIds.toArray(new Object[schemeIds.size()]);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextAttributesBySchemeIds that = (TextAttributesBySchemeIds) o;
    return Objects.equal(schemeIds, that.schemeIds);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeIds);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeIds", schemeIds)
        .toString();
  }

}
