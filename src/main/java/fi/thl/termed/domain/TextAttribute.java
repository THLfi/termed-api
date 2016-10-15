package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import fi.thl.termed.util.RegularExpressions;

public class TextAttribute extends Attribute {

  private String regex;

  public TextAttribute(Class domain, String id) {
    super(domain, id);
  }

  public TextAttribute(Class domain, String id, String uri) {
    super(domain, id, uri);
  }

  public TextAttribute(TextAttribute textAttribute) {
    super(textAttribute);
    this.regex = textAttribute.regex;
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
