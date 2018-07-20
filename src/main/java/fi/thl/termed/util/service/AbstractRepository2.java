package fi.thl.termed.util.service;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterators.partition;
import static fi.thl.termed.domain.AppRole.SUPERUSER;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.collect.Identifiable;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.Query;
import fi.thl.termed.util.query.Specification;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractRepository2<K extends Serializable, V extends Identifiable<K>>
    implements Service2<K, V> {

  private final User helper = new User("abstract-repository-helper", "", SUPERUSER);

  protected Logger log = LoggerFactory.getLogger(getClass());

  private int batchSize;

  public AbstractRepository2() {
    this(1);
  }

  public AbstractRepository2(int batchSize) {
    this.batchSize = batchSize;
  }

  @Override
  public Stream<K> save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    try (Stream<V> closeable = values) {
      Stream<Tuple2<K, V>> tuples = closeable.map(v -> Tuple.of(v.identifier(), v));

      switch (mode) {
        case INSERT:
          return insert(tuples, opts, user);
        case UPDATE:
          return update(tuples, opts, user);
        case UPSERT:
          return upsert(tuples, opts, user);
        default:
          throw new IllegalStateException("Unknown save mode: " + mode);
      }
    }
  }

  private Stream<K> insert(Stream<Tuple2<K, V>> inserts, WriteOptions opts, User user) {
    if (batchSize > 1) {
      return insertInNBatches(inserts, opts, user);
    } else if (batchSize < 0) {
      return insertBatch(inserts.collect(toImmutableList()), opts, user);
    } else if (batchSize == 1) {
      return insertEach(inserts, opts, user);
    } else {
      throw new IllegalStateException("Unexpected batch size: " + batchSize);
    }
  }

  private Stream<K> insertInNBatches(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();
    partition(stream.iterator(), batchSize)
        .forEachRemaining(batch -> insertBatch(batch, opts, user).forEach(keys));
    return keys.build();
  }

  /**
   * Default implementation just loops each value in the given list, subclasses may override.
   */
  protected Stream<K> insertBatch(List<Tuple2<K, V>> list, WriteOptions opts, User user) {
    return insertEach(list.stream(), opts, user);
  }

  private Stream<K> insertEach(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();
    stream.forEach(t -> {
      insert(t._1, t._2, opts, user);
      keys.accept(t._1);
    });
    return keys.build();
  }

  private Stream<K> update(Stream<Tuple2<K, V>> updates, WriteOptions opts, User user) {
    if (batchSize > 1) {
      return updateInNBatches(updates, opts, user);
    } else if (batchSize < 0) {
      return upsertBatch(updates.collect(toImmutableList()), opts, user);
    } else if (batchSize == 1) {
      return updateEach(updates, opts, user);
    } else {
      throw new IllegalStateException("Unexpected batch size: " + batchSize);
    }
  }

  private Stream<K> updateInNBatches(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();
    partition(stream.iterator(), batchSize)
        .forEachRemaining(batch -> updateBatch(batch, opts, user).forEach(keys));
    return keys.build();
  }

  /**
   * Default implementation just loops each value in the given list, subclasses may override.
   */
  protected Stream<K> updateBatch(List<Tuple2<K, V>> list, WriteOptions opts, User user) {
    return updateEach(list.stream(), opts, user);
  }

  private Stream<K> updateEach(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();
    stream.forEach(t -> {
      update(t._1, t._2, opts, user);
      keys.accept(t._1);
    });
    return keys.build();
  }

  private Stream<K> upsert(Stream<Tuple2<K, V>> upserts, WriteOptions opts, User user) {
    if (batchSize > 1) {
      return upsertInNBatches(upserts, opts, user);
    } else if (batchSize < 0) {
      return upsertBatch(upserts.collect(toImmutableList()), opts, user);
    } else if (batchSize == 1) {
      return upsertEach(upserts, opts, user);
    } else {
      throw new IllegalStateException("Unexpected batch size: " + batchSize);
    }
  }

  private Stream<K> upsertInNBatches(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();
    partition(stream.iterator(), batchSize)
        .forEachRemaining(batch -> upsertBatch(batch, opts, user).forEach(keys));
    return keys.build();
  }

  /**
   * Default implementation first collects all inserts and updates and then calls batch insert and
   * batch update.
   */
  protected Stream<K> upsertBatch(List<Tuple2<K, V>> list, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();

    ImmutableList.Builder<Tuple2<K, V>> inserts = ImmutableList.builder();
    ImmutableList.Builder<Tuple2<K, V>> updates = ImmutableList.builder();

    list.forEach(e -> {
      if (exists(e._1, helper)) {
        updates.add(Tuple.of(e._1, e._2));
      } else {
        inserts.add(Tuple.of(e._1, e._2));
      }
    });

    insertBatch(inserts.build(), opts, user).forEach(keys);
    updateBatch(updates.build(), opts, user).forEach(keys);

    return keys.build();
  }

  private Stream<K> upsertEach(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    Stream.Builder<K> keys = Stream.builder();
    stream.forEach(t -> {
      upsert(t._1, t._2, opts, user);
      keys.accept(t._1);
    });
    return keys.build();
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
        upsert(key, value, opts, user);
        break;
      default:
        throw new IllegalStateException("Unknown save mode: " + mode);
    }

    return key;
  }

  protected void upsert(K key, V value, WriteOptions opts, User user) {
    if (exists(key, helper)) {
      update(key, value, opts, user);
    } else {
      insert(key, value, opts, user);
    }
  }

  protected abstract void insert(K key, V value, WriteOptions opts, User user);

  protected abstract void update(K key, V value, WriteOptions opts, User user);

  @Override
  public void delete(Stream<K> keys, WriteOptions opts, User user) {
    try (Stream<K> deletes = keys) {
      if (batchSize > 1) {
        deleteInNBatches(deletes, opts, user);
      } else if (batchSize < 0) {
        deleteBatch(deletes.collect(toImmutableList()), opts, user);
      } else if (batchSize == 1) {
        deleteEach(deletes, opts, user);
      } else {
        throw new IllegalStateException("Unexpected batch size: " + batchSize);
      }
    }
  }

  private void deleteInNBatches(Stream<K> deletes, WriteOptions opts, User user) {
    partition(deletes.iterator(), batchSize)
        .forEachRemaining(batch -> deleteBatch(batch, opts, user));
  }

  /**
   * Default implementation just loops each value in the given list, subclasses may override.
   */
  protected void deleteBatch(List<K> deletes, WriteOptions opts, User user) {
    deleteEach(deletes.stream(), opts, user);
  }

  private void deleteEach(Stream<K> deletes, WriteOptions opts, User user) {
    deletes.forEach(key -> delete(key, opts, user));
  }

  @Override
  public long count(Specification<K, V> spec, User user) {
    try (Stream<K> keys = keys(new Query<>(spec), user)) {
      return keys.count();
    }
  }

}
