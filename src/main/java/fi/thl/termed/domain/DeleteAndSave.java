package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeleteAndSave<K extends Serializable, V> {

  private List<K> delete = new ArrayList<>();
  private List<V> save = new ArrayList<>();

  public DeleteAndSave() {
  }

  public DeleteAndSave(List<K> delete, List<V> save) {
    this.delete = delete;
    this.save = save;
  }

  public List<K> getDelete() {
    return delete;
  }

  public List<V> getSave() {
    return save;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("delete", delete)
        .add("save", save)
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
    DeleteAndSave<?, ?> that = (DeleteAndSave<?, ?>) o;
    return Objects.equals(delete, that.delete) &&
        Objects.equals(save, that.save);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delete, save);
  }

}
