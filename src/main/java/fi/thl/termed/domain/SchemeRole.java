package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemeRole implements Serializable {

  private final SchemeId scheme;

  private final String role;

  public SchemeRole(SchemeId scheme, String role) {
    this.scheme = checkNotNull(scheme, "scheme can't be null in %s", getClass());
    this.role = checkNotNull(role, "role can't be null in %s", getClass());
  }

  public SchemeId getScheme() {
    return scheme;
  }

  public UUID getSchemeId() {
    return scheme != null ? scheme.getId() : null;
  }

  public String getRole() {
    return role;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemeRole that = (SchemeRole) o;
    return Objects.equals(scheme, that.scheme) &&
           Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scheme, role);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("scheme", scheme)
        .add("role", role)
        .toString();
  }

}
