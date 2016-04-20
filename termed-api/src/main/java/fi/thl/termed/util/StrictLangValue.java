package fi.thl.termed.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Class to represent localized string.
 */
public class StrictLangValue {

  private final String lang;

  private final String value;

  private final String regex;

  public StrictLangValue(String lang, String value) {
    this(lang, value, RegularExpressions.MATCH_ALL);
  }

  public StrictLangValue(String lang, String value, String regex) {
    Preconditions.checkArgument(value.matches(regex), "%s must match %s", value, regex);

    this.lang = lang;
    this.value = value;
    this.regex = regex;
  }

  public String getLang() {
    return Strings.nullToEmpty(lang);
  }

  public String getValue() {
    return value;
  }

  public String getRegex() {
    return regex;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StrictLangValue that = (StrictLangValue) o;
    return Objects.equal(lang, that.lang) &&
           Objects.equal(value, that.value) &&
           Objects.equal(regex, that.regex);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(lang, value, regex);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("lang", lang)
        .add("value", value)
        .add("regex", regex)
        .toString();
  }

}
