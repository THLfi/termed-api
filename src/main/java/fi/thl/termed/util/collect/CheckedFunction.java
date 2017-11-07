package fi.thl.termed.util.collect;

@FunctionalInterface
public interface CheckedFunction<F, T> {

  T apply(F f) throws Exception;

}
