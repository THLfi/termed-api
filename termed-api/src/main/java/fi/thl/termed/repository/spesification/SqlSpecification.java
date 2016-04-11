package fi.thl.termed.repository.spesification;

import java.io.Serializable;

public abstract class SqlSpecification<K extends Serializable, V> extends Specification<K, V> {

  public abstract String sqlQueryTemplate();

  public abstract Object[] sqlQueryParameters();

}
