package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.UserSchemeRoleId;

public class UserSchemeRolesByUsername extends SqlSpecification<UserSchemeRoleId, Void> {

  private String username;

  public UserSchemeRolesByUsername(String username) {
    this.username = username;
  }

  @Override
  public boolean accept(UserSchemeRoleId id, Void value) {
    return Objects.equal(id.getUsername(), username);
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
