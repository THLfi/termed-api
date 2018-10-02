package fi.thl.termed.domain.event;

import java.io.Serializable;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ReindexEvent<K extends Serializable> {

  private final Supplier<Stream<K>> keyStreamSupplier;

  public ReindexEvent(Supplier<Stream<K>> keyStreamSupplier) {
    this.keyStreamSupplier = keyStreamSupplier;
  }

  public Supplier<Stream<K>> getKeyStreamSupplier() {
    return keyStreamSupplier;
  }

}
