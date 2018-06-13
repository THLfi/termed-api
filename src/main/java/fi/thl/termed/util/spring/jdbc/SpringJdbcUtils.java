package fi.thl.termed.util.spring.jdbc;

import com.google.common.collect.AbstractIterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import javax.sql.DataSource;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;

public final class SpringJdbcUtils {

  private SpringJdbcUtils() {
  }

  public static <T> Iterator<T> resultSetToMappingIterator(ResultSet rs, RowMapper<T> rowMapper) {
    return new AbstractIterator<T>() {
      @Override
      protected T computeNext() {
        try {
          return rs.next() ? rowMapper.mapRow(rs, rs.getRow()) : endOfData();
        } catch (SQLException e) {
          throw new InvalidResultSetAccessException(e);
        }
      }
    };
  }

  public static String getDatabaseProductName(DataSource dataSource) {
    try {
      return JdbcUtils.extractDatabaseMetaData(dataSource, "getDatabaseProductName");
    } catch (MetaDataAccessException e) {
      throw new RuntimeException(e);
    }
  }

}
