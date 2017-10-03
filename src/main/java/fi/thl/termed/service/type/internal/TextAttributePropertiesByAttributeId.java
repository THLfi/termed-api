package fi.thl.termed.service.type.internal;

import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class TextAttributePropertiesByAttributeId
    extends AbstractSqlSpecification<PropertyValueId<TextAttributeId>, LangValue> {

  private TextAttributeId textAttributeId;

  TextAttributePropertiesByAttributeId(TextAttributeId textAttributeId) {
    this.textAttributeId = textAttributeId;
  }

  @Override
  public boolean test(PropertyValueId<TextAttributeId> propertyValueId, LangValue value) {
    return Objects.equals(propertyValueId.getSubjectId(), textAttributeId);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    TypeId domainId = textAttributeId.getDomainId();
    return ParametrizedSqlQuery.of(
        "text_attribute_domain_graph_id = ? and text_attribute_domain_id = ? and text_attribute_id = ?",
        domainId.getGraphId(), domainId.getId(), textAttributeId.getId());
  }

}
