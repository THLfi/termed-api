package fi.thl.termed.service.node.select;

public abstract class SelectAttributeWithDepth extends SelectAttribute {

  private int depth;

  SelectAttributeWithDepth(String attributeId) {
    this(attributeId, 1);
  }

  SelectAttributeWithDepth(String attributeId, int depth) {
    super(attributeId);
    this.depth = depth;
  }

  public int getDepth() {
    return depth;
  }

}
