package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.util.Objects;

import fi.thl.termed.util.RegularExpressions;

public class TextAttribute extends Attribute implements Identifiable<TextAttributeId> {

  private String regex;

  public TextAttribute(String id, ClassId domain) {
    super(id, domain);
  }

  public TextAttribute(String id, String uri, ClassId domain) {
    super(id, uri, domain);
  }

  public TextAttribute(TextAttribute textAttribute) {
    super(textAttribute);
    this.regex = textAttribute.regex;
  }

  @Override
  public TextAttributeId identifier() {
    return new TextAttributeId(getDomain(), getId());
  }

  public String getRegex() {
    return MoreObjects.firstNonNull(regex, RegularExpressions.ALL);
  }

  public void setRegex(String regex) {
    this.regex = regex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    TextAttribute that = (TextAttribute) o;
    return Objects.equals(regex, that.regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), regex);
  }

  @Override
  public MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("regex", regex);
  }

}
