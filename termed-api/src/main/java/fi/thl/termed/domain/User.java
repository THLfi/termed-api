package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class User implements UserDetails {

  private String username;
  private String password;
  private String appRole;

  public User(String username, String password, String appRole) {
    Preconditions.checkNotNull(username);
    Preconditions.checkNotNull(password);
    Preconditions.checkNotNull(appRole);

    this.username = username;
    this.password = password;
    this.appRole = appRole;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return Collections.<GrantedAuthority>singleton(new SimpleGrantedAuthority(appRole));
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getPassword() {
    return password;
  }

  public String getAppRole() {
    return appRole;
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
