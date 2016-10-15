package fi.thl.termed.util.collect;

import com.google.common.collect.MapDifference;

import java.util.Objects;

public class SimpleValueDifference<V> implements MapDifference.ValueDifference<V> {

  private final V leftValue;
  private final V rightValue;

  public SimpleValueDifference(V leftValue, V rightValue) {
    this.leftValue = leftValue;
    this.rightValue = rightValue;
  }

  @Override
  public V leftValue() {
    return leftValue;
  }

  @Override
  public V rightValue() {
    return rightValue;
  }

  public boolean equals(Object o) {
    if (o instanceof MapDifference.ValueDifference) {
      MapDifference.ValueDifference<?> valueDifference = (MapDifference.ValueDifference<?>) o;
      return Objects.equals(leftValue, valueDifference.leftValue()) &&
             Objects.equals(rightValue, valueDifference.rightValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(leftValue, rightValue);
  }

}
