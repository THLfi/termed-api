package fi.thl.termed.service.user.internal;

import java.util.Objects;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.query.AbstractSqlSpecification;

public class UserGraphRolesByUsername extends AbstractSqlSpecification<UserGraphRole, Empty> {

  private String username;

  public UserGraphRolesByUsername(String username) {
    this.username = username;
  }

  @Override
  public boolean test(UserGraphRole id, Empty value) {
    return Objects.equals(id.getUsername(), username);
  }

  @Override
  public String sqlQueryTemplate() {
    return "username = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{username};
  }

}
