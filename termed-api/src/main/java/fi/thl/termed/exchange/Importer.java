package fi.thl.termed.exchange;

import java.util.Map;

import fi.thl.termed.domain.User;

public interface Importer<E> {

  void save(E value, Map<String, Object> args, User currentUser);

}
