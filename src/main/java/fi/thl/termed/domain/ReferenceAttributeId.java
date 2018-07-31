package fi.thl.termed.domain;

public final class ReferenceAttributeId extends AttributeId {

  public ReferenceAttributeId(ReferenceAttribute attribute) {
    super(attribute);
  }

  public ReferenceAttributeId(TypeId domainId, String id) {
    super(domainId, id);
  }

}
