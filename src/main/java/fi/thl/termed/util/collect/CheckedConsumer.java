package fi.thl.termed.util.collect;

@FunctionalInterface
public interface CheckedConsumer<T> {

  void apply(T f) throws Exception;

}
