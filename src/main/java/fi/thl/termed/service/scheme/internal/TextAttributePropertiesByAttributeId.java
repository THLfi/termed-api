package fi.thl.termed.service.scheme.internal;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.specification.AbstractSqlSpecification;

public class TextAttributePropertiesByAttributeId
    extends AbstractSqlSpecification<PropertyValueId<TextAttributeId>, LangValue> {

  private TextAttributeId textAttributeId;

  public TextAttributePropertiesByAttributeId(TextAttributeId textAttributeId) {
    this.textAttributeId = textAttributeId;
  }

  @Override
  public boolean test(PropertyValueId<TextAttributeId> propertyValueId, LangValue value) {
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

}
