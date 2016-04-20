package fi.thl.termed.domain;

import com.google.common.base.Objects;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

public class TextAttributeId implements Serializable {

  private final ClassId domainId;

  private final String regex;

  private final String id;

  public TextAttributeId(ClassId domainId, String regex, String id) {
    this.domainId = checkNotNull(domainId, "domainId can't be null in %s", getClass());
    this.regex = checkNotNull(regex, "regex can't be null in %s", getClass());
    this.id = checkNotNull(id, "id can't be null in %s", getClass());
  }

  public ClassId getDomainId() {
    return domainId;
  }

  public String getRegex() {
    return regex;
  }

  public String getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TextAttributeId that = (TextAttributeId) o;
    // regex is ignored here and in hashCode as it's kind of updatable extra part of the identity
    return Objects.equal(domainId, that.domainId) &&
           Objects.equal(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(domainId, id);
  }

}
