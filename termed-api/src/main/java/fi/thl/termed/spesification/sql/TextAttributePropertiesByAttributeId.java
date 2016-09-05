package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.common.AbstractSpecification;
import fi.thl.termed.util.LangValue;

public class TextAttributePropertiesByAttributeId
    extends AbstractSpecification<PropertyValueId<TextAttributeId>, LangValue>
    implements SqlSpecification<PropertyValueId<TextAttributeId>, LangValue> {

  private TextAttributeId textAttributeId;

  public TextAttributePropertiesByAttributeId(TextAttributeId textAttributeId) {
    this.textAttributeId = textAttributeId;
  }

  @Override
  public boolean accept(PropertyValueId<TextAttributeId> propertyValueId, LangValue value) {
    return Objects.equal(propertyValueId.getSubjectId(), textAttributeId);
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
