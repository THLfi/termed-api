package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public final class UrlWithCredentials {

  private final String url;
  private final String username;
  private final String password;

  public UrlWithCredentials(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  public String getUrl() {
    return url;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("url", url)
        .add("username", username)
        .add("password", password)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UrlWithCredentials that = (UrlWithCredentials) o;
    return Objects.equals(url, that.url) &&
        Objects.equals(username, that.username) &&
        Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, username, password);
  }

}
