package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Changeset<K extends Serializable, V> {

  private List<K> delete = new ArrayList<>();
  private List<V> save = new ArrayList<>();
  private List<V> patch = new ArrayList<>();

  public Changeset() {
  }

  public Changeset(List<K> delete, List<V> save, List<V> patch) {
    this.delete = delete;
    this.save = save;
    this.patch = patch;
  }

  public List<K> getDelete() {
    return delete;
  }

  public List<V> getSave() {
    return save;
  }

  public List<V> getPatch() {
    return patch;
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
