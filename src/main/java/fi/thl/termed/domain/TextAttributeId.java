package fi.thl.termed.domain;

public class TextAttributeId extends AttributeId {

  public TextAttributeId(TextAttribute attribute) {
    super(attribute);
  }

  public TextAttributeId(TypeId domainId, String id) {
    super(domainId, id);
  }

}
