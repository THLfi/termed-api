package fi.thl.termed.util.collect;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class StreamUtils {

  private StreamUtils() {
  }

  private static ScheduledExecutorService scheduledExecutorService = Executors
      .newScheduledThreadPool(10);

  public static <T> Stream<T> toStreamWithTimeout(Stream<T> stream, int delay, TimeUnit timeUnit) {
    ScheduledFuture<Void> closeOnTimeout = scheduledExecutorService.schedule(() -> {
      stream.close();
      return null;
    }, delay, timeUnit);

    // cancel closeOnTimeout if stream is properly closed on time
    return stream.onClose(() -> closeOnTimeout.cancel(false));
  }

  public static <T> List<T> toListAndClose(Stream<T> stream) {
    try (Stream<T> autoClosed = stream) {
      return autoClosed.collect(toList());
    }
  }

  public static <T> Optional<T> findFirstAndClose(Stream<T> stream) {
    try (Stream<T> autoClosed = stream) {
      return autoClosed.findFirst();
    }
  }

  public static <T> Stream<T> toStream(Optional<T> optional) {
    return optional.map(Stream::of).orElse(Stream.empty());
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

  public static <L, Z> Stream<Z> zipWithIndex(Stream<L> l, BiFunction<L, Integer, Z> zipper) {
    return zip(l, IntStream.iterate(0, i -> i + 1).boxed(), zipper);
  }

}
