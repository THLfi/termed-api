package fi.thl.termed.service.node.select;

import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.UUIDs;

public class SelectTypeQualifiedProperty extends AbstractSelectTypeQualifiedField {

  private transient String luceneSelectField;

  public SelectTypeQualifiedProperty(TextAttributeId textAttributeId) {
    super(textAttributeId.getDomainId(), textAttributeId.getId());
  }

  @Override
  public String toLuceneSelectField() {
    if (luceneSelectField == null) {
      luceneSelectField =
          UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() + ".properties." + field;
    }
    return luceneSelectField;
  }

  @Override
  public String toString() {
    return UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId() + ".properties." + field;
  }

}
