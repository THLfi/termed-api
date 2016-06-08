package fi.thl.termed.exchange;

import java.io.Serializable;

public interface Exchange<K extends Serializable, V, E> extends Exporter<K, V, E>, Importer<E> {

}
