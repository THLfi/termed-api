package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;

public class RevisionId<K> implements Serializable {

  private final K id;
  private final Long revision;

  private RevisionId(K id, Long revision) {
    this.id = requireNonNull(id);
    this.revision = requireNonNull(revision);
  }

  public static <E> RevisionId<E> of(E value, Long version) {
    return new RevisionId<>(value, version);
  }

  public K getId() {
    return id;
  }

  public Long getRevision() {
    return revision;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevisionId<?> revisionIdId = (RevisionId<?>) o;
    return Objects.equals(id, revisionIdId.id) &&
        Objects.equals(revision, revisionIdId.revision);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, revision);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("version", revision)
        .toString();
  }

}
