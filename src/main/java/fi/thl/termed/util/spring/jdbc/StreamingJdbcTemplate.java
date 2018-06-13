package fi.thl.termed.util.spring.jdbc;

import static fi.thl.termed.util.spring.jdbc.SpringJdbcUtils.resultSetToMappingIterator;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Streams;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class StreamingJdbcTemplate extends JdbcTemplate {

  public StreamingJdbcTemplate(DataSource dataSource) {
    super(dataSource);
  }

  public <T> Stream<T> queryForStream(String sql, RowMapper<T> rowMapper, Object... args)
      throws DataAccessException {
    return queryForStream(sql, args, rowMapper);
  }

  public <T> Stream<T> queryForStream(String sql, Object[] args, RowMapper<T> rowMapper)
      throws DataAccessException {

    DataSource dataSource = requireNonNull(getDataSource());

    Connection connection = DataSourceUtils.getConnection(dataSource);
    ResultSet resultSet;

    try {
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      newArgPreparedStatementSetter(args).setValues(preparedStatement);
      resultSet = preparedStatement.executeQuery();
    } catch (SQLException | RuntimeException | Error e) {
      DataSourceUtils.releaseConnection(connection, dataSource);
      throw new RuntimeException(e);
    }

    return Streams.stream(resultSetToMappingIterator(resultSet, rowMapper))
        .onClose(() -> DataSourceUtils.releaseConnection(connection, dataSource));
  }

  public <T> Optional<T> queryForOptional(String sql, Class<T> requiredType, Object... args) {
    return Optional.ofNullable(queryForObject(sql, requiredType, args));
  }

}
