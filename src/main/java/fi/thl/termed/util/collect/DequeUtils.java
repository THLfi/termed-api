package fi.thl.termed.util.collect;

import java.util.ArrayDeque;
import java.util.Deque;

public final class DequeUtils {

  private DequeUtils() {
  }

  public static <T> ArrayDeque<T> newArrayDeque(T element) {
    ArrayDeque<T> list = new ArrayDeque<>();
    list.add(element);
    return list;
  }

  public static <T> Deque<T> addFirst(T element, Deque<T> deque) {
    deque.addFirst(element);
    return deque;
  }

}
