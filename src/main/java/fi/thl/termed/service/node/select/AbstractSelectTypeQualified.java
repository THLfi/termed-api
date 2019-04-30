package fi.thl.termed.service.node.select;

import static java.util.Objects.requireNonNull;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.query.Select;
import java.util.Objects;

public abstract class AbstractSelectTypeQualified implements Select {

  protected final TypeId typeId;

  AbstractSelectTypeQualified(TypeId typeId) {
    this.typeId = requireNonNull(typeId);
  }

  public TypeId getTypeId() {
    return typeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AbstractSelectTypeQualified that = (AbstractSelectTypeQualified) o;
    return Objects.equals(typeId, that.typeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeId);
  }

  @Override
  public String toString() {
    return UUIDs.toString(typeId.getGraphId()) + "." + typeId.getId();
  }

}
