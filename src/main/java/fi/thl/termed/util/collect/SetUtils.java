package fi.thl.termed.util.collect;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.util.function.Function;
import java.util.stream.Collector;

public final class SetUtils {

  private SetUtils() {
  }

  public static <T> Collector<T, ?, ImmutableSet<T>> toImmutableSet() {
    return Collector.of(
        ImmutableSet.Builder::new,
        ImmutableSet.Builder::add,
        (l, r) -> l.addAll(r.build()),
        (Function<Builder<T>, ImmutableSet<T>>) ImmutableSet.Builder::build);
  }

}
