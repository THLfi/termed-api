package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to represent resource identity based on uri.
 */
public class ResourceUri implements Serializable {

  private final UUID schemeId;

  private final String typeId;

  private final String uri;

  public ResourceUri(UUID schemeId, String typeId, String uri) {
    this.schemeId = checkNotNull(schemeId, "schemeId can't be null in %s", getClass());
    this.typeId = checkNotNull(typeId, "typeId can't be null in %s", getClass());
    this.uri = checkNotNull(uri, "uri can't be null in %s", getClass());
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  public String getTypeId() {
    return typeId;
  }

  public String getUri() {
    return uri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceUri that = (ResourceUri) o;
    return Objects.equal(schemeId, that.schemeId) &&
           Objects.equal(typeId, that.typeId) &&
           Objects.equal(uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId, typeId, uri);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("typeId", typeId)
        .add("uri", uri)
        .toString();
  }

}
