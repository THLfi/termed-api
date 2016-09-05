package fi.thl.termed.spesification;

import org.apache.lucene.search.Query;

import java.io.Serializable;

public interface LuceneSpecification<K extends Serializable, V> extends Specification<K, V> {

  Query luceneQuery();

}
