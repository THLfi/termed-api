package fi.thl.termed.util.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.MatchAll;

public abstract class AbstractDao<K extends Serializable, V> implements Dao<K, V> {

  @Override
  public void insert(Map<K, V> map, User user) {
    map.forEach((k, v) -> insert(k, v, user));
  }

  @Override
  public void update(Map<K, V> map, User user) {
    map.forEach((k, v) -> update(k, v, user));
  }

  @Override
  public void delete(List<K> keys, User user) {
    keys.forEach(k -> delete(k, user));
  }

  @Override
  public Map<K, V> getMap(User user) {
    return getMap(new MatchAll<>(), user);
  }

  @Override
  public Map<K, V> getMap(List<K> keys, User user) {
    Map<K, V> results = new LinkedHashMap<>();
    keys.forEach(key -> get(key, user).ifPresent(value -> results.put(key, value)));
    return results;
  }

  @Override
  public List<K> getKeys(User user) {
    return getKeys(new MatchAll<>(), user);
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification, User user) {
    return new ArrayList<>(getMap(specification, user).keySet());
  }

  @Override
  public List<V> getValues(User user) {
    return getValues(new MatchAll<>(), user);
  }

  @Override
  public List<V> getValues(Specification<K, V> specification, User user) {
    return new ArrayList<>(getMap(specification, user).values());
  }

  @Override
  public List<V> getValues(List<K> keys, User user) {
    return new ArrayList<>(getMap(keys, user).values());
  }

}
