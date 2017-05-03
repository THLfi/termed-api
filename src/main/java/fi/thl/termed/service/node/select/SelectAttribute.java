package fi.thl.termed.service.node.select;

import java.util.Objects;

public abstract class SelectAttribute implements Select {

  private String attributeId;

  SelectAttribute(String attributeId) {
    this.attributeId = attributeId;
  }

  public String getAttributeId() {
    return attributeId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SelectAttribute that = (SelectAttribute) o;
    return Objects.equals(attributeId, that.attributeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeId);
  }

}
