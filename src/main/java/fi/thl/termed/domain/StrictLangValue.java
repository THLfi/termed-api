package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import fi.thl.termed.util.RegularExpressions;

/**
 * Class to represent localized string with pattern.
 */
public class StrictLangValue {

  private String lang;

  private String value;

  private String regex;

  public StrictLangValue(String lang, String value) {
    this(lang, value, RegularExpressions.ALL);
  }

  public StrictLangValue(String lang, String value, String regex) {
    this.lang = lang;
    this.value = value;
    this.regex = regex;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getRegex() {
    return regex;
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
