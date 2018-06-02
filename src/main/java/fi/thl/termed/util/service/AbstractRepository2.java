package fi.thl.termed.util.service;

import static fi.thl.termed.domain.AppRole.SUPERUSER;
import static fi.thl.termed.util.collect.StreamUtils.forEachAndClose;

import com.google.common.collect.Iterators;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.stream.Stream;

public abstract class AbstractRepository2<K extends Serializable, V extends Identifiable<K>>
    implements Service2<K, V> {

  private static final int UPSERT_BATCH_SIZE = 10_000;

  private final User helper = new User("abstract-repository-helper", "", SUPERUSER);

  @Override
  public Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    switch (mode) {
      case INSERT:
        return insert(values, opts, user);
      case UPDATE:
        return update(values, opts, user);
      case UPSERT:
        try (Stream<V> closeable = values) {
          Stream.Builder<K> keys = Stream.builder();

          Iterators.partition(closeable.iterator(), UPSERT_BATCH_SIZE).forEachRemaining(batch -> {
            Stream.Builder<V> inserts = Stream.builder();
            Stream.Builder<V> updates = Stream.builder();

            batch.forEach(value -> {
              if (exists(value.identifier(), helper)) {
                updates.accept(value);
              } else {
                inserts.accept(value);
              }
            });

            forEachAndClose(insert(inserts.build(), opts, user), keys);
            forEachAndClose(update(updates.build(), opts, user), keys);
          });

          return keys.build();
        }
      default:
        throw new IllegalStateException("Unknown save mode: " + mode);
    }
  }

  protected Stream<K> insert(Stream<V> stream, WriteOptions opts, User user) {
    try (Stream<V> closeable = stream) {
      Stream.Builder<K> keys = Stream.builder();

      closeable.forEach(value -> {
        K key = value.identifier();
        insert(key, value, opts, user);
        keys.accept(key);
      });

      return keys.build();
    }
  }

  protected Stream<K> update(Stream<V> stream, WriteOptions opts, User user) {
    try (Stream<V> closeable = stream) {
      Stream.Builder<K> keys = Stream.builder();

      closeable.forEach(value -> {
        K key = value.identifier();
        update(key, value, opts, user);
        keys.accept(key);
      });

      return keys.build();
    }
  }

  @Override
  public K save(V value, SaveMode mode, WriteOptions opts, User user) {
    K key = value.identifier();

    switch (mode) {
      case INSERT:
        insert(key, value, opts, user);
        break;
      case UPDATE:
        update(key, value, opts, user);
        break;
      case UPSERT:
        if (exists(key, helper)) {
          update(key, value, opts, user);
        } else {
          insert(key, value, opts, user);
        }
        break;
      default:
        throw new IllegalStateException("Unknown save mode: " + mode);
    }

    return key;
  }

  protected abstract void insert(K key, V value, WriteOptions opts, User user);

  protected abstract void update(K key, V value, WriteOptions opts, User user);

  @Override
  public void delete(Stream<K> keys, WriteOptions opts, User user) {
    try (Stream<K> closeable = keys) {
      closeable.forEach(key -> delete(key, opts, user));
    }
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    try (Stream<K> keys = keys(new Query<>(spec), user)) {
      return keys.count();
    }
  }

}
