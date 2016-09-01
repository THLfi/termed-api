package fi.thl.termed.util;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;

public final class ListUtils {

  private ListUtils() {
  }

  public static <T> List<T> concat(List<? extends T> l1, List<? extends T> l2) {
    return Lists.newArrayList(Iterables.concat(l1, l2));
  }

  public static <T> List<T> difference(List<T> l1, List<T> l2) {
    return Lists.newArrayList(Sets.difference(ImmutableSet.copyOf(l1), ImmutableSet.copyOf(l2)));
  }

  public static <T> List<T> filter(List<T> list, Predicate<? super T> predicate) {
    return Lists.newArrayList(Iterables.filter(list, predicate));
  }

  public static <T> List<T> flatten(List<List<T>> listOfLists) {
    List<T> result = Lists.newArrayList();

    for (List<T> list : listOfLists) {
      for (T value : list) {
        result.add(value);
      }
    }

    return result;
  }

  public static <F, T> List<List<T>> transformNested(List<List<F>> fromLists,
                                                     final Function<? super F, ? extends T> function) {
    return Lists.transform(fromLists, new Function<List<F>, List<T>>() {
      @Override
      public List<T> apply(List<F> input) {
        return Lists.transform(input, function);
      }
    });
  }

  public static <T> List<T> nullToEmpty(List<T> list) {
    return list == null ? Collections.<T>emptyList() : list;
  }

  public static <T> boolean isNullOrEmpty(List<T> list) {
    return list == null || list.isEmpty();
  }

}
