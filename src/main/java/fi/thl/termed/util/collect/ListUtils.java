package fi.thl.termed.util.collect;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ListUtils {

  private ListUtils() {
  }

  public static <T> List<T> flatten(List<List<T>> listOfLists) {
    return listOfLists.stream().flatMap(Collection::stream).collect(Collectors.toList());
  }

  public static <F, T> List<List<T>> transformNested(List<List<F>> fromLists,
      final Function<? super F, ? extends T> function) {
    return fromLists.stream()
        .map(l -> l.stream().map(function).collect(Collectors.<T>toList()))
        .collect(Collectors.toList());
  }

  public static <T> List<T> nullToEmpty(List<T> list) {
    return list == null ? Collections.emptyList() : list;
  }

  public static <T> ImmutableList<T> nullToEmpty(ImmutableList<T> list) {
    return list == null ? ImmutableList.of() : list;
  }

  public static <T> boolean isNullOrEmpty(List<T> list) {
    return list == null || list.isEmpty();
  }

  public static <E> ImmutableList<E> nullableImmutableCopyOf(List<E> list) {
    return list != null ? ImmutableList.copyOf(list) : null;
  }

  /**
   * Applies distributive law from left to right. Transformation may produce output that is
   * exponential in size.
   *
   * As an example (a + b)(c + d) = ac + ad + bc + bd would translate to input: [[a, b], [c, d]]
   * output: [[a, c], [a, d], [b, c], [b, d]]
   */
  public static <E> List<List<E>> distribute(List<List<E>> simpleExpression) {
    // wrap to one item 'factors' to simplify further calculations
    List<List<List<E>>> expression = ListUtils
        .transformNested(simpleExpression, Collections::singletonList);

    Iterator<List<List<E>>> terms = expression.iterator();

    List<List<E>> resultTerm = new ArrayList<>();

    if (terms.hasNext()) {
      resultTerm = terms.next();
    }

    while (terms.hasNext()) {
      List<List<E>> nextTerm = terms.next();
      resultTerm = distribute(resultTerm, nextTerm);
    }

    return resultTerm;
  }

  // e.g. ([a] + [b]) ([c] + [d]) = ([a * c] + [a * d] + [b * c] + [b * d])
  private static <E> List<List<E>> distribute(List<List<E>> firstTerm, List<List<E>> secondTerm) {
    List<List<E>> resultTerm = new ArrayList<>();

    for (List<E> firstFactor : firstTerm) {
      for (List<E> secondFactor : secondTerm) {
        List<E> resultFactor = new ArrayList<>();

        resultFactor.addAll(firstFactor);
        resultFactor.addAll(secondFactor);

        resultTerm.add(resultFactor);
      }
    }

    return resultTerm;
  }

}
