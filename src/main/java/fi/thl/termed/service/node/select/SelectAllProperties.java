package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.Select;

public class SelectAllProperties extends Select {

  public SelectAllProperties() {
    super("properties.*");
  }

}
