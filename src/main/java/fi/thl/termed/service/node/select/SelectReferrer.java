package fi.thl.termed.service.node.select;

public class SelectReferrer extends SelectWithDepth {

  public SelectReferrer(String attributeId) {
    super(attributeId);
  }

  public SelectReferrer(String field, int depth) {
    super(field, depth);
  }

  public SelectReferrer(String qualifier, String attributeId) {
    super(qualifier, attributeId);
  }

  public SelectReferrer(String qualifier, String attributeId, int depth) {
    super(qualifier, attributeId, depth);
  }

  @Override
  public String toString() {
    return qualifier + "referrers." + field + ":" + depth;
  }

}
