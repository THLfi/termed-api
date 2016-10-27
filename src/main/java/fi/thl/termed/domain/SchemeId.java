package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class SchemeId implements Serializable {

  private final UUID id;

  public SchemeId(Scheme scheme) {
    this(scheme.getId());
  }

  public SchemeId(UUID id) {
    this.id = requireNonNull(id, () -> "id can't be null in " + getClass());
  }

  public UUID getId() {
    return id;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("id", id).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SchemeId schemeId = (SchemeId) o;
    return Objects.equals(id, schemeId.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

}
