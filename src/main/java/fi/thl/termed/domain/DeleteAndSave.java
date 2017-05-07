package fi.thl.termed.domain;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeleteAndSave<K extends Serializable, V> {

  private List<K> deletes = new ArrayList<>();
  private List<V> saves = new ArrayList<>();

  public DeleteAndSave() {
  }

  public DeleteAndSave(List<K> deletes, List<V> saves) {
    this.deletes = deletes;
    this.saves = saves;
  }

  public List<K> getDeletes() {
    return deletes;
  }

  public List<V> getSaves() {
    return saves;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("deletes", deletes)
        .add("saves", saves)
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
    return Objects.equals(deletes, that.deletes) &&
        Objects.equals(saves, that.saves);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deletes, saves);
  }

}
