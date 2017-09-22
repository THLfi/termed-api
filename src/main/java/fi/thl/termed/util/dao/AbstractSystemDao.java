package fi.thl.termed.util.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fi.thl.termed.util.query.Specification;
import fi.thl.termed.util.query.MatchAll;

public abstract class AbstractSystemDao<K extends Serializable, V> implements SystemDao<K, V> {

  @Override
  public void insert(Map<K, V> map) {
    map.forEach(this::insert);
  }

  @Override
  public void update(Map<K, V> map) {
    map.forEach(this::update);
  }

  @Override
  public void delete(List<K> keys) {
    keys.forEach(this::delete);
  }

  @Override
  public Map<K, V> getMap() {
    return getMap(new MatchAll<>());
  }

  @Override
  public Map<K, V> getMap(List<K> keys) {
    Map<K, V> results = new LinkedHashMap<>();
    keys.forEach(key -> get(key).ifPresent(value -> results.put(key, value)));
    return results;
  }

  @Override
  public List<K> getKeys() {
    return getKeys(new MatchAll<>());
  }

  @Override
  public List<K> getKeys(Specification<K, V> specification) {
    return new ArrayList<>(getMap(specification).keySet());
  }

  @Override
  public List<V> getValues() {
    return getValues(new MatchAll<>());
  }

  @Override
  public List<V> getValues(Specification<K, V> specification) {
    return new ArrayList<>(getMap(specification).values());
  }

  @Override
  public List<V> getValues(List<K> keys) {
    return new ArrayList<>(getMap(keys).values());
  }

}
