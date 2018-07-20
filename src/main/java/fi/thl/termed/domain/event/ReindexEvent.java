package fi.thl.termed.domain.event;

import java.io.Serializable;
import java.util.stream.Stream;

public class ReindexEvent<K extends Serializable> {

  private final Stream<K> keys;

  public ReindexEvent(Stream<K> keys) {
    this.keys = keys;
  }

  public Stream<K> getKeys() {
    return keys;
  }

}
