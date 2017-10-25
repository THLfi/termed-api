package fi.thl.termed.util.service;

import static java.lang.String.format;

import fi.thl.termed.domain.User;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcSequenceService implements SequenceService {

  private JdbcTemplate jdbcTemplate;
  private String sequenceName;

  public JdbcSequenceService(DataSource dataSource, String sequenceName) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.sequenceName = sequenceName;
  }

  @Override
  public Long getAndAdvance(User user) {
    return jdbcTemplate.queryForObject(format("select nextval('%s')", sequenceName), Long.class);
  }

}
