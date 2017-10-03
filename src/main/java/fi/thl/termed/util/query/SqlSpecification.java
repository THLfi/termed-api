package fi.thl.termed.util.query;

import java.io.Serializable;

public interface SqlSpecification<K extends Serializable, V> extends Specification<K, V> {

  ParametrizedSqlQuery sql();

  default String sqlQueryTemplate() {
    return sql().getQueryTemplate();
  }

  default Object[] sqlQueryParameters() {
    return sql().getQueryParameters();
  }

}
