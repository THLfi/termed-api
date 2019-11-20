package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import fi.thl.termed.util.collect.Identifiable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

public final class Webhook implements Identifiable<UUID> {

  private final UUID id;
  private final URI url;

  public Webhook(URI url) {
    this(UUID.randomUUID(), url);
  }

  public Webhook(UUID id, URI url) {
    this.id = id;
    this.url = url;
  }

  public Webhook(String urlString) {
    this(UUID.randomUUID(), urlString);
  }

  public Webhook(UUID id, String urlString) {
    this.id = id;
    try {
      this.url = new URI(urlString);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public UUID getId() {
    return id;
  }

  public URI getUrl() {
    return url;
  }

  @Override
  public UUID identifier() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Webhook webhook = (Webhook) o;
    return Objects.equals(id, webhook.id) &&
        Objects.equals(url, webhook.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, url);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("url", url)
        .toString();
  }

}
