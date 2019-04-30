package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.AbstractSelectQualified;

public class SelectAllReferences extends AbstractSelectQualified {

  public SelectAllReferences() {
  }

  public SelectAllReferences(String qualifier) {
    super(qualifier);
  }

  @Override
  public String toString() {
    return qualifier + "references.*";
  }

}
