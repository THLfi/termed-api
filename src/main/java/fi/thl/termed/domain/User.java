package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fi.thl.termed.util.collect.ListUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class User implements UserDetails, Identifiable<String> {

  private String username;
  private String password;

  private AppRole appRole;
  private List<SchemeRole> schemeRoles;

  public User(String username, String password, AppRole appRole, List<SchemeRole> schemeRoles) {
    this.username = checkNotNull(username, "username can't be null in %s", getClass());
    this.password = checkNotNull(password, "password can't be null in %s", getClass());
    this.appRole = checkNotNull(appRole, "appRole can't be null in %s", getClass());
    this.schemeRoles = schemeRoles;
  }

  public User(String username, String password, AppRole appRole) {
    this.username = checkNotNull(username, "username can't be null in %s", getClass());
    this.password = checkNotNull(password, "password can't be null in %s", getClass());
    this.appRole = checkNotNull(appRole, "appRole can't be null in %s", getClass());
  }

  public User(User user) {
    this.username = user.username;
    this.password = user.password;
    this.appRole = user.appRole;
    this.schemeRoles = user.schemeRoles;
  }

  @Override
  public String identifier() {
    return username;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return Collections.<GrantedAuthority>singleton(new SimpleGrantedAuthority(appRole.toString()));
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public AppRole getAppRole() {
    return appRole;
  }

  public List<SchemeRole> getSchemeRoles() {
    return ListUtils.nullToEmpty(schemeRoles);
  }

  public void setSchemeRoles(List<SchemeRole> schemeRoles) {
    this.schemeRoles = schemeRoles;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isAccountNonExpired() {
    return isEnabled();
  }

  @Override
  public boolean isAccountNonLocked() {
    return isEnabled();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return isEnabled();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", username)
        .toString();
  }

}
