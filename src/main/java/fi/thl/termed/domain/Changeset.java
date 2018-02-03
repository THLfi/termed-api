package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.ListUtils.nullToEmpty;
import static fi.thl.termed.util.collect.ListUtils.nullableImmutableCopyOf;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Changeset<K extends Serializable, V> {

  private final ImmutableList<K> delete;
  private final ImmutableList<V> save;
  private final ImmutableList<V> patch;

  public Changeset(List<K> delete, List<V> save, List<V> patch) {
    this.delete = nullableImmutableCopyOf(delete);
    this.save = nullableImmutableCopyOf(save);
    this.patch = nullableImmutableCopyOf(patch);
  }

  public ImmutableList<K> getDelete() {
    return nullToEmpty(delete);
  }

  public ImmutableList<V> getSave() {
    return nullToEmpty(save);
  }

  public ImmutableList<V> getPatch() {
    return nullToEmpty(patch);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("delete", delete)
        .add("save", save)
        .add("patch", patch)
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
    Changeset<?, ?> that = (Changeset<?, ?>) o;
    return Objects.equals(delete, that.delete) &&
        Objects.equals(save, that.save) &&
        Objects.equals(patch, that.patch);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delete, save, patch);
  }

}
