package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class SchemeRole implements Serializable {

  private final UUID schemeId;

  private final String role;

  public SchemeRole(UUID schemeId, String role) {
    this.schemeId = checkNotNull(schemeId, "schemeId can't be null in %s", getClass());
    this.role = checkNotNull(role, "role can't be null in %s", getClass());
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
    SchemeRole that = (SchemeRole) o;
    return Objects.equals(schemeId, that.schemeId) &&
           Objects.equals(role, that.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(schemeId, role);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("role", role)
        .toString();
  }

}
