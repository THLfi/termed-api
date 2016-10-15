package fi.thl.termed.spesification.sql;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.StrictLangValue;

public class ResourceTextAttributeValuesByAttributeId
    extends AbstractSpecification<ResourceAttributeValueId, StrictLangValue>
    implements SqlSpecification<ResourceAttributeValueId, StrictLangValue> {

  private TextAttributeId attributeId;

  public ResourceTextAttributeValuesByAttributeId(TextAttributeId attributeId) {
    this.attributeId = attributeId;
  }

  public TextAttributeId getAttributeId() {
    return attributeId;
  }

  @Override
  protected boolean accept(ResourceAttributeValueId attributeValueId, StrictLangValue value) {
    TextAttributeId valueAttributeId =
        new TextAttributeId(new ClassId(attributeValueId.getResourceId()),
                            attributeValueId.getAttributeId());
    return Objects.equal(valueAttributeId, attributeId);
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and resource_type_id = ? and attribute_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    ClassId domainId = attributeId.getDomainId();
    return new Object[]{domainId.getSchemeId(), domainId.getId(), attributeId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceTextAttributeValuesByAttributeId that =
        (ResourceTextAttributeValuesByAttributeId) o;
    return Objects.equal(attributeId, that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(attributeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("attributeId", attributeId)
        .toString();
  }

}
