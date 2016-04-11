package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to represent resource identity based on code.
 */
public class ResourceCode implements Serializable {

  private final UUID schemeId;

  private final String typeId;

  private final String code;

  public ResourceCode(UUID schemeId, String typeId, String code) {
    this.schemeId = checkNotNull(schemeId, "schemeId can't be null in %s", getClass());
    this.typeId = checkNotNull(typeId, "typeId can't be null in %s", getClass());
    this.code = checkNotNull(code, "code can't be null in %s", getClass());
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  public String getTypeId() {
    return typeId;
  }

  public String getCode() {
    return code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceCode that = (ResourceCode) o;
    return Objects.equal(schemeId, that.schemeId) &&
           Objects.equal(typeId, that.typeId) &&
           Objects.equal(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId, typeId, code);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("typeId", typeId)
        .add("code", code)
        .toString();
  }

}
