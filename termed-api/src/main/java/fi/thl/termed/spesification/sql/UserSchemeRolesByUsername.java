package fi.thl.termed.spesification.sql;

import com.google.common.base.Objects;

import fi.thl.termed.domain.UserSchemeRoleId;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.common.AbstractSpecification;

public class UserSchemeRolesByUsername extends AbstractSpecification<UserSchemeRoleId, Void>
    implements SqlSpecification<UserSchemeRoleId, Void> {

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
