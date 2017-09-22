package fi.thl.termed.service.node.select;

import fi.thl.termed.util.query.Select;

public class SelectAllReferrers extends Select {

  public SelectAllReferrers() {
    super("referrers.*");
  }

}
