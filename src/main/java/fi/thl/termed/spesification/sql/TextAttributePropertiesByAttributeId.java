package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.specification.AbstractSpecification;
import fi.thl.termed.domain.LangValue;

public class TextAttributePropertiesByAttributeId
    extends AbstractSpecification<PropertyValueId<TextAttributeId>, LangValue>
    implements SqlSpecification<PropertyValueId<TextAttributeId>, LangValue> {

  private TextAttributeId textAttributeId;

  public TextAttributePropertiesByAttributeId(TextAttributeId textAttributeId) {
    this.textAttributeId = textAttributeId;
  }

  @Override
  public boolean accept(PropertyValueId<TextAttributeId> propertyValueId, LangValue value) {
    return Objects.equals(propertyValueId.getSubjectId(), textAttributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "text_attribute_scheme_id = ? and text_attribute_domain_id = ? and text_attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    ClassId domainId = textAttributeId.getDomainId();
    return new Object[]{domainId.getSchemeId(), domainId.getId(), textAttributeId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextAttributePropertiesByAttributeId that = (TextAttributePropertiesByAttributeId) o;
    return Objects.equals(textAttributeId, that.textAttributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(textAttributeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("textAttributeId", textAttributeId)
        .toString();
  }

}
