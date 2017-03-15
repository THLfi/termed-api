package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.util.Date;
import java.util.Objects;

/**
 * Wraps event to serializable format
 */
public class WebEvent {

  private Date date;
  private String type;
  private Object body;

  public WebEvent(Date date, String type, Object body) {
    this.date = date;
    this.type = type;
    this.body = body;
  }

  public Date getDate() {
    return date;
  }

  public String getType() {
    return type;
  }

  public Object getBody() {
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
    return Objects.equals(date, webEvent.date) &&
        Objects.equals(type, webEvent.type) &&
        Objects.equals(body, webEvent.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, type, body);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("timestamp", date)
        .add("type", type)
        .add("body", body)
        .toString();
  }

}
