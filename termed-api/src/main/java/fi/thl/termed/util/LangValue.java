package fi.thl.termed.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Class to represent localized string.
 */
public class LangValue {

  private final String lang;

  private final String value;

  public LangValue(String lang, String value) {
    this.lang = lang;
    this.value = value;
  }

  public String getLang() {
    return Strings.nullToEmpty(lang);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LangValue that = (LangValue) o;
    return Objects.equal(lang, that.lang) &&
           Objects.equal(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(lang, value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("lang", lang)
        .add("value", value)
        .toString();
  }

}
