package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.AbstractSelectQualified;

public class SelectAllProperties extends AbstractSelectQualified {

  public SelectAllProperties() {
  }

  public SelectAllProperties(String qualifier) {
    super(qualifier);
  }

  @Override
  public String toString() {
    return qualifier + "properties.*";
  }

}
