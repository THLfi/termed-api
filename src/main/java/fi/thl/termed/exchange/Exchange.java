package fi.thl.termed.exchange;

import java.io.Serializable;

/**
 * Exchange implements both Exporter and Importer.
 */
public interface Exchange<K extends Serializable, V, E> extends Exporter<K, V, E>, Importer<E> {

}
