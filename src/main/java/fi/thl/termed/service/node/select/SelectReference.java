package fi.thl.termed.service.node.select;

public class SelectReference extends SelectWithDepth {

  public SelectReference(String attributeId) {
    super(attributeId);
  }

  public SelectReference(String qualifier, String attributeId) {
    super(qualifier, attributeId);
  }

  public SelectReference(String qualifier, String attributeId, int depth) {
    super(qualifier, attributeId, depth);
  }

  @Override
  public String toString() {
    return qualifier + "references." + field + ":" + depth;
  }

}
