package fi.thl.termed.util.dao;

import static java.lang.String.format;

import com.google.common.base.Preconditions;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcSystemSequenceDao implements SystemSequenceDao {

  private final JdbcTemplate jdbcTemplate;
  private final String sequenceName;

  public JdbcSystemSequenceDao(DataSource dataSource, String sequenceName) {
    Preconditions.checkArgument(sequenceName.matches("[A-Za-z0-9_]+"));
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.sequenceName = sequenceName;
  }

  @Override
  public Long getAndAdvance() {
    return jdbcTemplate.queryForObject(format("select nextval('%s')", sequenceName), Long.class);
  }

}
