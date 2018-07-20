package fi.thl.termed.util.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.thl.termed.util.postgresql.CopyManagerUtils.copyInAsCsv;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.Iterators;
import fi.thl.termed.util.collect.Tuple2;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.postgresql.core.BaseConnection;
import org.springframework.jdbc.datasource.DataSourceUtils;

/**
 * Implements faster bulk insert for Postgres. If backed database is not Postgres, forwards insert
 * to delegate.
 */
public abstract class AbstractJdbcPostgresDao<K extends Serializable, V> extends
    ForwardingSystemDao2<K, V> {

  private static final int DEFAULT_BATCH_SIZE = 10_000;
  private static final int ANALYZE_LIMIT = 1000;

  private final DataSource dataSource;
  private final String table;
  private final int batchSize;

  public AbstractJdbcPostgresDao(SystemDao2<K, V> delegate, DataSource dataSource, String table) {
    this(delegate, dataSource, table, DEFAULT_BATCH_SIZE);
  }

  public AbstractJdbcPostgresDao(SystemDao2<K, V> delegate, DataSource dataSource, String table,
      int batchSize) {
    super(delegate);

    requireNonNull(dataSource);
    requireNonNull(table);
    checkArgument(table.matches("[a-zA-Z_]+"));
    checkArgument(batchSize > 0);

    this.dataSource = dataSource;
    this.table = table;
    this.batchSize = batchSize;
  }

  @Override
  public void insert(Stream<Tuple2<K, V>> entries) {
    Connection c = DataSourceUtils.getConnection(dataSource);

    try {
      if (c.isWrapperFor(BaseConnection.class)) {
        copyIn(c.unwrap(BaseConnection.class), entries);
        return;
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      DataSourceUtils.releaseConnection(c, dataSource);
    }

    super.insert(entries);
  }

  private void copyIn(BaseConnection connection, Stream<Tuple2<K, V>> entries) {
    AtomicInteger insertCount = new AtomicInteger(0);

    try (Stream<Tuple2<K, V>> closeable = entries) {
      Iterators.partition(closeable.iterator(), batchSize).forEachRemaining(batch -> {
        copyInAsCsv(connection, format("COPY %s FROM STDIN CSV", table), toRows(batch));
        insertCount.getAndAdd(batch.size());
      });
    }

    if (insertCount.get() > ANALYZE_LIMIT) {
      analyzeTable(connection);
    }
  }

  private List<String[]> toRows(List<Tuple2<K, V>> tuples) {
    return tuples.stream().map(t -> toRow(t._1, t._2)).collect(toList());
  }

  protected abstract String[] toRow(K key, V value);

  private void analyzeTable(BaseConnection c) {
    try (Statement s = c.createStatement()) {
      s.executeUpdate(format("ANALYZE %s", table));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
