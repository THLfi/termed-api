package fi.thl.termed.service.user.internal;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcUserDao extends AbstractJdbcDao2<String, User> {

  public JdbcUserDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(String username, User user) {
    jdbcTemplate.update("insert into users (username, password, app_role) values (?, ?, ?)",
        username, user.getPassword(), user.getAppRole().toString());
  }

  @Override
  public void update(String username, User user) {
    jdbcTemplate.update("update users set password = ?, app_role = ? where username = ?",
        user.getPassword(), user.getAppRole().toString(), username);
  }

  @Override
  public void delete(String username) {
    jdbcTemplate.update("delete from users where username = ?", username);
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<String, User> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from users where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(String username) {
    return jdbcTemplate.queryForOptional("select count(*) from users where username = ?",
        Long.class, username)
        .orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(String username, RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from users where username = ?",
        mapper, username).stream().findFirst();
  }

  @Override
  protected RowMapper<String> buildKeyMapper() {
    return (rs, rowNum) -> rs.getString("username");
  }

  @Override
  protected RowMapper<User> buildValueMapper() {
    return (rs, rowNum) -> {
      String username = rs.getString("username");
      String password = rs.getString("password");
      AppRole appRole = AppRole.valueOf(rs.getString("app_role"));
      return new User(username, password, appRole);
    };
  }

}
