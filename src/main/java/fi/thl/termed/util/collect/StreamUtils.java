package fi.thl.termed.util.collect;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.StreamSupport.stream;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public final class StreamUtils {

  private StreamUtils() {
  }

  public static <L, R> Stream<Map.Entry<L, R>> zip(Stream<L> l, Stream<R> r) {
    return zip(l, r, SimpleImmutableEntry::new);
  }

  public static <L, R, Z> Stream<Z> zip(Stream<L> l, Stream<R> r, BiFunction<L, R, Z> zipper) {
    return stream(spliteratorUnknownSize(new Iterator<Z>() {
      Iterator<L> leftIterator = l.iterator();
      Iterator<R> rightIterator = r.iterator();

      @Override
      public boolean hasNext() {
        return leftIterator.hasNext() && rightIterator.hasNext();
      }

      @Override
      public Z next() {
        return zipper.apply(leftIterator.next(), rightIterator.next());
      }
    }, Spliterator.ORDERED), l.isParallel() || r.isParallel());
  }

}
