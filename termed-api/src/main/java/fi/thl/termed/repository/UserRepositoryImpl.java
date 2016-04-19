package fi.thl.termed.repository;

import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.User;
import fi.thl.termed.repository.spesification.Specification;

@Repository
public class UserRepositoryImpl extends UserRepository {

  private JdbcTemplate jdbcTemplate;

  @Autowired
  public UserRepositoryImpl(DataSource dataSource) {
    this.jdbcTemplate = new JdbcTemplate(dataSource);
  }

  @Override
  public void save(User user) {
    save(user.getUsername(), user);
  }

  @Override
  protected void insert(String username, User user) {
    jdbcTemplate.update("insert into users (username, password, app_role) values (?, ?, ?)",
                        username, user.getPassword(), user.getAppRole());
  }

  @Override
  protected void update(String username, User newUser, User oldUser) {
    jdbcTemplate.update("update users set password = ?, app_role = ? where username = ?",
                        newUser.getPassword(), newUser.getAppRole(), username);
  }

  @Override
  protected void delete(String username, User user) {
    delete(username);
  }

  @Override
  public void delete(String username) {
    jdbcTemplate.update("delete from users where username = ?", username);
  }

  @Override
  public boolean exists(String username) {
    return jdbcTemplate.queryForObject("select count(*) from users where username = ?",
                                       Long.class, username) > 0;
  }

  @Override
  public List<User> get() {
    return jdbcTemplate.query("select * from users", new UserRowMapper());
  }

  @Override
  public List<User> get(Specification<String, User> specification) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return get(username);
  }

  @Override
  public User get(String username) {
    return Iterables.getFirst(jdbcTemplate.query("select * from users where username = ?",
                                                 new UserRowMapper(), username), null);
  }

  private class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
      String username = rs.getString("username");
      String password = rs.getString("password");
      String appRole = rs.getString("app_role");
      return new User(username, password, appRole);
    }
  }

}
