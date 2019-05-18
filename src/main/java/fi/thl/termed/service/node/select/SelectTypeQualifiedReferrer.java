package fi.thl.termed.service.node.select;

import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.UUIDs;

public class SelectTypeQualifiedReferrer extends AbstractSelectTypeQualifiedFieldWithDepth {

  private transient String luceneSelectField;

  public SelectTypeQualifiedReferrer(ReferenceAttributeId attributeId) {
    super(attributeId.getDomainId(), attributeId.getId());
  }

  public SelectTypeQualifiedReferrer(ReferenceAttributeId attributeId, int depth) {
    super(attributeId.getDomainId(), attributeId.getId(), depth);
  }

  @Override
  public String toLuceneSelectField() {
    if (luceneSelectField == null) {
      luceneSelectField =
          UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() + ".referrers." + field;
    }
    return luceneSelectField;
  }

  @Override
  public String toString() {
    return UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() +
        ".referrers." + field + ":" + depth;
  }

}
