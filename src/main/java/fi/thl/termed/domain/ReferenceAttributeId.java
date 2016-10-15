package fi.thl.termed.domain;

public class ReferenceAttributeId extends AttributeId {

  public ReferenceAttributeId(ReferenceAttribute attribute) {
    super(attribute);
  }

  public ReferenceAttributeId(ClassId domainId, String id) {
    super(domainId, id);
  }

}
