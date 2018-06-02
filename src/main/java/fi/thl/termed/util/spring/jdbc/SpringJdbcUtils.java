package fi.thl.termed.util.spring.jdbc;

import com.google.common.collect.AbstractIterator;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.RowMapper;

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

}
