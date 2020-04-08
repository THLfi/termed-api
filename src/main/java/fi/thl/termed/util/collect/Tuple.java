package fi.thl.termed.util.collect;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Tuple extends Serializable {

  static <T1, T2> Stream<Tuple2<T1, T2>> entriesAsTuples(Map<T1, T2> map) {
    return map.entrySet().stream().map(Tuple::of);
  }

  static <T1, T2> Map<T1, T2> tuplesToMap(Stream<Tuple2<T1, T2>> tupleStream) {
    try (Stream<Tuple2<T1, T2>> closableTupleStream = tupleStream) {
      return closableTupleStream.collect(Collectors.toMap(e -> e._1, e -> e._2));
    }
  }

  static <T1, T2> Tuple2<T1, T2> of(Map.Entry<T1, T2> entry) {
    return new Tuple2<>(entry.getKey(), entry.getValue());
  }

  static <T1, T2> Tuple2<T1, T2> of(T1 t1, T2 t2) {
    return new Tuple2<>(t1, t2);
  }

  static <T1, T2, T3> Tuple3<T1, T2, T3> of(T1 t1, T2 t2, T3 t3) {
    return new Tuple3<>(t1, t2, t3);
  }

  static <T1, T2, T3, T4> Tuple4<T1, T2, T3, T4> of(T1 t1, T2 t2, T3 t3, T4 t4) {
    return new Tuple4<>(t1, t2, t3, t4);
  }

}
