package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.AbstractSelectQualified;

public class SelectAllReferrers extends AbstractSelectQualified {

  public SelectAllReferrers() {
  }

  public SelectAllReferrers(String qualifier) {
    super(qualifier);
  }

  @Override
  public String toString() {
    return qualifier + "referrers.*";
  }

}
