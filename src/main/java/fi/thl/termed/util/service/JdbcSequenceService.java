package fi.thl.termed.util.service;

import static java.lang.String.format;

import com.google.common.base.Preconditions;
import fi.thl.termed.domain.Permission;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.permission.PermissionEvaluator;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;

public class JdbcSequenceService implements SequenceService {

  private JdbcTemplate jdbcTemplate;
  private String sequenceName;
  private PermissionEvaluator<String> sequencePermissionEvaluator;

  public JdbcSequenceService(DataSource dataSource, String sequenceName,
      PermissionEvaluator<String> sequencePermissionEvaluator) {
    Preconditions.checkArgument(sequenceName.matches("[A-Za-z0-9_]+"));
    this.jdbcTemplate = new JdbcTemplate(dataSource);
    this.sequenceName = sequenceName;
    this.sequencePermissionEvaluator = sequencePermissionEvaluator;
  }

  @Override
  public Long getAndAdvance(User user) {
    if (sequencePermissionEvaluator.hasPermission(user, sequenceName, Permission.READ) &&
        sequencePermissionEvaluator.hasPermission(user, sequenceName, Permission.UPDATE)) {
      return jdbcTemplate.queryForObject(format("select nextval('%s')", sequenceName), Long.class);
    }
    throw new AccessDeniedException("Access is denied");
  }

}
