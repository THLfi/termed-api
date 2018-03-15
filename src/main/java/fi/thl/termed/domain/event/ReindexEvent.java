package fi.thl.termed.domain.event;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Objects;

public class ReindexEvent<K extends Serializable> {

  private final ImmutableSet<K> keys;

  public ReindexEvent(Iterable<K> keys) {
    this.keys = ImmutableSet.copyOf(keys);
  }

  public ImmutableSet<K> getKeys() {
    return keys;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReindexEvent<?> that = (ReindexEvent<?>) o;
    return Objects.equals(keys, that.keys);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keys);
  }

}
