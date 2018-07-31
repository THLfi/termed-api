package fi.thl.termed.domain;

import static com.google.common.base.Strings.nullToEmpty;

import com.google.common.base.MoreObjects;
import fi.thl.termed.util.RegularExpressions;
import java.util.Objects;

/**
 * Class to represent localized string with pattern.
 */
public class StrictLangValue {

  private final String lang;

  private final String value;

  private final String regex;

  public StrictLangValue(String value) {
    this("", value);
  }

  public StrictLangValue(String lang, String value) {
    this(lang, value, RegularExpressions.ALL);
  }

  public StrictLangValue(String lang, String value, String regex) {
    this.lang = lang;
    this.value = value;
    this.regex = regex;
  }

  public String getLang() {
    return nullToEmpty(lang);
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
    return Objects.equals(lang, that.lang) &&
        Objects.equals(value, that.value) &&
        Objects.equals(regex, that.regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lang, value, regex);
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
