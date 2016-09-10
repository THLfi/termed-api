package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import fi.thl.termed.util.RegularExpressions;

public class TextAttribute extends Attribute {

  private String regex;

  public TextAttribute(Class domain, String id) {
    super(domain, id);
  }

  public TextAttribute(Class domain, String id, String uri) {
    super(domain, id, uri);
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
    return Objects.equal(regex, that.regex);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), regex);
  }

  @Override
  public MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("regex", regex);
  }

}
