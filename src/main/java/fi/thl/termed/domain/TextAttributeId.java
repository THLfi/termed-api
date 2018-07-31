package fi.thl.termed.domain;

public final class TextAttributeId extends AttributeId {

  public TextAttributeId(TextAttribute attribute) {
    super(attribute);
  }

  public TextAttributeId(TypeId domainId, String id) {
    super(domainId, id);
  }

}
