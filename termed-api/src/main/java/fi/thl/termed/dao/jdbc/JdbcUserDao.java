package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.AppRole;
import fi.thl.termed.domain.User;
import fi.thl.termed.spesification.SqlSpecification;

public class JdbcUserDao extends AbstractJdbcDao<String, User> {

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
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from users", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<String, User> specification, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from users where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(String username) {
    return jdbcTemplate.queryForObject("select count(*) from users where username = ?",
                                       Long.class, username) > 0;
  }

  @Override
  protected <E> E get(String username, RowMapper<E> mapper) {
    return Iterables.getFirst(jdbcTemplate.query("select * from users where username = ?",
                                                 mapper, username), null);
  }

  @Override
  protected RowMapper<String> buildKeyMapper() {
    return new RowMapper<String>() {
      @Override
      public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getString("username");
      }
    };
  }

  @Override
  protected RowMapper<User> buildValueMapper() {
    return new RowMapper<User>() {
      @Override
      public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        String username = rs.getString("username");
        String password = rs.getString("password");
        AppRole appRole = AppRole.valueOf(rs.getString("app_role"));
        return new User(username, password, appRole);
      }
    };
  }

}
