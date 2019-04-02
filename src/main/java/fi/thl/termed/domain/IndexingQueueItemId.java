package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Objects;

public class IndexingQueueItemId<K> implements Serializable {

  private final K id;
  private final Long indexingQueueId;

  private IndexingQueueItemId(K id, Long indexingQueueId) {
    this.id = requireNonNull(id);
    this.indexingQueueId = requireNonNull(indexingQueueId);
  }

  public static <K> IndexingQueueItemId<K> of(K id, Long indexingQueueId) {
    return new IndexingQueueItemId<>(id, indexingQueueId);
  }

  public K getId() {
    return id;
  }

  public Long getIndexingQueueId() {
    return indexingQueueId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IndexingQueueItemId<?> that = (IndexingQueueItemId<?>) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(indexingQueueId, that.indexingQueueId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, indexingQueueId);
  }

}
