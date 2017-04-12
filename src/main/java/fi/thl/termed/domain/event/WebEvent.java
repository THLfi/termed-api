package fi.thl.termed.domain.event;

import com.google.common.base.MoreObjects;
import java.util.Objects;

/**
 * For serialized HTTP events, adds type name to termed event
 */
public class WebEvent {

  private String type;
  private TermedEvent body;

  public WebEvent(TermedEvent source) {
    this.type = source.getClass().getSimpleName();
    this.body = source;
  }

  public String getType() {
    return type;
  }

  public TermedEvent getBody() {
    return body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebEvent webEvent = (WebEvent) o;
    return Objects.equals(type, webEvent.type) &&
        Objects.equals(body, webEvent.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, body);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("type", type)
        .add("body", body)
        .toString();
  }

}
