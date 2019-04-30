package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.AbstractSelectQualifiedField;

public class SelectProperty extends AbstractSelectQualifiedField {

  public SelectProperty(String field) {
    super(field);
  }

  public SelectProperty(String qualifier, String field) {
    super(qualifier, field);
  }

  @Override
  public String toString() {
    return qualifier + "properties." + field;
  }

}
