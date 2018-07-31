package fi.thl.termed.domain;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.util.RandomUtils.randomAlphanumericString;
import static fi.thl.termed.util.collect.ListUtils.nullToEmpty;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class User implements UserDetails, Identifiable<String> {

  private final String username;
  private final String password;

  private final AppRole appRole;
  private final ImmutableList<GraphRole> graphRoles;

  public User(String username, String password, AppRole appRole, List<GraphRole> graphRoles) {
    this.username = checkNotNull(username, "username can't be null in %s", getClass());
    this.password = checkNotNull(password, "password can't be null in %s", getClass());
    this.appRole = checkNotNull(appRole, "appRole can't be null in %s", getClass());
    this.graphRoles = copyOf(graphRoles);
  }

  public User(String username, String password, AppRole appRole) {
    this(username, password, appRole, of());
  }

  public User(User user) {
    this(user.username, user.password, user.appRole, user.graphRoles);
  }

  public static User newUser(String username) {
    return newUser(username, randomAlphanumericString(25));
  }

  public static User newUser(String username, String password) {
    return new User(username, password, AppRole.USER);
  }

  public static User newAdmin(String username) {
    return newAdmin(username, randomAlphanumericString(25));
  }

  public static User newAdmin(String username, String password) {
    return new User(username, password, AppRole.ADMIN);
  }

  public static User newSuperuser(String username) {
    return newSuperuser(username, randomAlphanumericString(25));
  }

  public static User newSuperuser(String username, String password) {
    return new User(username, password, AppRole.SUPERUSER);
  }

  @Override
  public String identifier() {
    return username;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return of(new SimpleGrantedAuthority(appRole.toString()));
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

  public List<GraphRole> getGraphRoles() {
    return nullToEmpty(graphRoles);
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
        .add("appRole", appRole)
        .add("graphRoles", graphRoles)
        .toString();
  }

}
