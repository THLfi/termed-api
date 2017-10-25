package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

import com.google.common.base.MoreObjects;
import fi.thl.termed.util.collect.Identifiable;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public class Revision<K extends Serializable, V> implements Identifiable<RevisionId<K>> {

  private final K id;
  private final Long revision;
  private final RevisionType type;
  private final V object;

  private Revision(K id, Long revision, RevisionType type, V object) {
    this.id = requireNonNull(id);
    this.revision = requireNonNull(revision);
    this.type = requireNonNull(type);
    this.object = object;
  }

  public static <K extends Serializable, V> Revision<K, V> of(RevisionId<K> revisionId,
      RevisionType type, V object) {
    return new Revision<>(revisionId.getId(), revisionId.getRevision(), type, object);
  }

  public static <K extends Serializable, V> Revision<K, V> of(K id, Long revision,
      RevisionType type, V object) {
    return new Revision<>(id, revision, type, object);
  }

  @Override
  public RevisionId<K> identifier() {
    return RevisionId.of(id, revision);
  }

  public K getId() {
    return id;
  }

  public Long getRevision() {
    return revision;
  }

  public RevisionType getType() {
    return type;
  }

  public Optional<V> getObject() {
    return ofNullable(object);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Revision<?, ?> that = (Revision<?, ?>) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(revision, that.revision) &&
        Objects.equals(type, that.type) &&
        Objects.equals(object, that.object);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, revision, type, object);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("revision", revision)
        .add("type", type)
        .add("object", object)
        .toString();
  }

}
