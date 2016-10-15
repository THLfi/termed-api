package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserSchemeRoleId implements Serializable {

  private final String username;
  private final UUID schemeId;
  private final String role;

  public UserSchemeRoleId(String username, UUID schemeId, String role) {
    this.username = checkNotNull(username, "username can't be null in %s", getClass());
    this.schemeId = checkNotNull(schemeId, "schemeId can't be null in %s", getClass());
    this.role = checkNotNull(role, "role can't be null in %s", getClass());
  }

  public String getUsername() {
    return username;
  }

  public UUID getSchemeId() {
    return schemeId;
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
    UserSchemeRoleId that = (UserSchemeRoleId) o;
    return Objects.equal(username, that.username) &&
           Objects.equal(schemeId, that.schemeId) &&
           Objects.equal(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(username, schemeId, role);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .add("schemeId", schemeId)
        .add("role", role)
        .toString();
  }

}
