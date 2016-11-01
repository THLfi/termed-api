package fi.thl.termed.domain;

public class ReferenceAttributeId extends AttributeId {

  public ReferenceAttributeId(ReferenceAttribute attribute) {
    super(attribute);
  }

  public ReferenceAttributeId(TypeId domainId, String id) {
    super(domainId, id);
  }

}
