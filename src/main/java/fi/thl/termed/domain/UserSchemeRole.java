package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserSchemeRole implements Serializable {

  private final String username;
  private final SchemeId scheme;
  private final String role;

  public UserSchemeRole(String username, SchemeId scheme, String role) {
    this.username = checkNotNull(username, "username can't be null in %s", getClass());
    this.scheme = checkNotNull(scheme, "scheme can't be null in %s", getClass());
    this.role = checkNotNull(role, "role can't be null in %s", getClass());
  }

  public String getUsername() {
    return username;
  }

  public UUID getSchemeId() {
    return scheme != null ? scheme.getId() : null;
  }

  public SchemeId getScheme() {
    return scheme;
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
    UserSchemeRole that = (UserSchemeRole) o;
    return Objects.equals(username, that.username) &&
           Objects.equals(scheme, that.scheme) &&
           Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(username, scheme, role);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("scheme", scheme)
        .add("role", role)
        .toString();
  }

}
