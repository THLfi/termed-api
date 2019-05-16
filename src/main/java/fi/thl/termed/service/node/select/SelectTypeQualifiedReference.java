package fi.thl.termed.service.node.select;

import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.UUIDs;

public class SelectTypeQualifiedReference extends AbstractSelectTypeQualifiedFieldWithDepth {

  public SelectTypeQualifiedReference(ReferenceAttributeId attributeId) {
    super(attributeId.getDomainId(), attributeId.getId());
  }

  public SelectTypeQualifiedReference(ReferenceAttributeId attributeId, int depth) {
    super(attributeId.getDomainId(), attributeId.getId(), depth);
  }

  @Override
  public String toLuceneSelectField() {
    return UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() + ".references." + field;
  }

  @Override
  public String toString() {
    return UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() +
        ".references." + field + ":" + depth;
  }

}
