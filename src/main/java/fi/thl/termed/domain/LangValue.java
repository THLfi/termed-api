package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import java.util.Objects;

/**
 * Class to represent localized string.
 */
public class LangValue {

  private final String lang;

  private final String value;

  public LangValue(StrictLangValue strictLangValue) {
    this(strictLangValue.getLang(), strictLangValue.getValue());
  }

  public LangValue(LangValue langValue) {
    this(langValue.getLang(), langValue.getValue());
  }

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
    return Objects.equals(lang, that.lang) &&
           Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lang, value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("lang", lang)
        .add("value", value)
        .toString();
  }

}
