package fi.thl.termed.util.service;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Iterators.partition;
import static fi.thl.termed.domain.AppRole.SUPERUSER;

import com.google.common.base.Preconditions;
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

/**
 * Abstract service that implements batched processing for save and delete.
 */
public abstract class AbstractRepository<K extends Serializable, V extends Identifiable<K>>
    implements Service<K, V> {

  private final User helper = new User("abstract-repository-helper", "", SUPERUSER);

  protected Logger log = LoggerFactory.getLogger(getClass());

  private int batchSize;

  public AbstractRepository() {
    this(1);
  }

  /**
   * @param batchSize batch size 1 means that values are processed iteratively one at a time (i.e.
   * not in batches), batch size > 1 means that values are processed in batches of given size (e.g.
   * batch could be 1000), negative size means that all values are processed in one big "batch"
   * containing all values.
   */
  public AbstractRepository(int batchSize) {
    Preconditions.checkArgument(batchSize != 0, "Illegal batch size: " + batchSize);
    this.batchSize = batchSize;
  }

  @Override
  public void save(Stream<V> values, SaveMode mode, WriteOptions opts, User user) {
    try (Stream<V> closeable = values) {
      Stream<Tuple2<K, V>> tuples = closeable.map(v -> Tuple.of(v.identifier(), v));

      switch (mode) {
        case INSERT:
          insert(tuples, opts, user);
          break;
        case UPDATE:
          update(tuples, opts, user);
          break;
        case UPSERT:
          upsert(tuples, opts, user);
          break;
        default:
          throw new IllegalStateException("Unknown save mode: " + mode);
      }
    }
  }

  private void insert(Stream<Tuple2<K, V>> inserts, WriteOptions opts, User user) {
    if (batchSize > 1) {
      insertInNBatches(inserts, opts, user);
    } else if (batchSize < 0) {
      insertBatch(inserts.collect(toImmutableList()), opts, user);
    } else if (batchSize == 1) {
      insertEach(inserts, opts, user);
    } else {
      throw new IllegalStateException("Unexpected batch size: " + batchSize);
    }
  }

  private void insertInNBatches(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    partition(stream.iterator(), batchSize)
        .forEachRemaining(batch -> insertBatch(batch, opts, user));
  }

  /**
   * Default implementation just loops each value in the given list, subclasses may override.
   */
  protected void insertBatch(List<Tuple2<K, V>> list, WriteOptions opts, User user) {
    insertEach(list.stream(), opts, user);
  }

  private void insertEach(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    stream.forEach(t -> insert(t._1, t._2, opts, user));
  }

  private void update(Stream<Tuple2<K, V>> updates, WriteOptions opts, User user) {
    if (batchSize > 1) {
      updateInNBatches(updates, opts, user);
    } else if (batchSize < 0) {
      upsertBatch(updates.collect(toImmutableList()), opts, user);
    } else if (batchSize == 1) {
      updateEach(updates, opts, user);
    } else {
      throw new IllegalStateException("Unexpected batch size: " + batchSize);
    }
  }

  private void updateInNBatches(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    partition(stream.iterator(), batchSize)
        .forEachRemaining(batch -> updateBatch(batch, opts, user));
  }

  /**
   * Default implementation just loops each value in the given list, subclasses may override.
   */
  protected void updateBatch(List<Tuple2<K, V>> list, WriteOptions opts, User user) {
    updateEach(list.stream(), opts, user);
  }

  private void updateEach(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    stream.forEach(t -> update(t._1, t._2, opts, user));
  }

  private void upsert(Stream<Tuple2<K, V>> upserts, WriteOptions opts, User user) {
    if (batchSize > 1) {
      upsertInNBatches(upserts, opts, user);
    } else if (batchSize < 0) {
      upsertBatch(upserts.collect(toImmutableList()), opts, user);
    } else if (batchSize == 1) {
      upsertEach(upserts, opts, user);
    } else {
      throw new IllegalStateException("Unexpected batch size: " + batchSize);
    }
  }

  private void upsertInNBatches(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    partition(stream.iterator(), batchSize)
        .forEachRemaining(batch -> upsertBatch(batch, opts, user));
  }

  /**
   * Default implementation first collects all inserts and updates and then calls batch insert and
   * batch update.
   */
  protected void upsertBatch(List<Tuple2<K, V>> list, WriteOptions opts, User user) {
    ImmutableList.Builder<Tuple2<K, V>> inserts = ImmutableList.builder();
    ImmutableList.Builder<Tuple2<K, V>> updates = ImmutableList.builder();

    list.forEach(e -> {
      if (exists(e._1, helper)) {
        updates.add(Tuple.of(e._1, e._2));
      } else {
        inserts.add(Tuple.of(e._1, e._2));
      }
    });

    insertBatch(inserts.build(), opts, user);
    updateBatch(updates.build(), opts, user);
  }

  private void upsertEach(Stream<Tuple2<K, V>> stream, WriteOptions opts, User user) {
    stream.forEach(t -> upsert(t._1, t._2, opts, user));
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
