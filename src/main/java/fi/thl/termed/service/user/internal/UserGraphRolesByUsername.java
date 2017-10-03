package fi.thl.termed.service.user.internal;

import fi.thl.termed.domain.Empty;
import fi.thl.termed.domain.UserGraphRole;
import fi.thl.termed.util.query.AbstractSqlSpecification;
import fi.thl.termed.util.query.ParametrizedSqlQuery;
import java.util.Objects;

public class UserGraphRolesByUsername extends AbstractSqlSpecification<UserGraphRole, Empty> {

  private String username;

  UserGraphRolesByUsername(String username) {
    this.username = username;
  }

  @Override
  public boolean test(UserGraphRole id, Empty value) {
    return Objects.equals(id.getUsername(), username);
  }

  @Override
  public ParametrizedSqlQuery sql() {
    return ParametrizedSqlQuery.of("username = ?", username);
  }

}
