package fi.thl.termed.util.specification;

import java.io.Serializable;

public interface SqlSpecification<K extends Serializable, V> extends Specification<K, V> {

  String sqlQueryTemplate();

  Object[] sqlQueryParameters();

}
