package fi.thl.termed.service.webhook.internal;

import fi.thl.termed.domain.Webhook;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcWebhookDao extends AbstractJdbcDao<UUID, Webhook> {

  public JdbcWebhookDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(UUID id, Webhook webhook) {
    jdbcTemplate.update("insert into webhook (id, url) values (?, ?)",
        id, webhook.getUrl().toString());
  }

  @Override
  public void update(UUID id, Webhook webhook) {
    jdbcTemplate.update("update webhook set url = ?",
        webhook.getUrl().toString());
  }

  @Override
  public void delete(UUID id) {
    jdbcTemplate.update("delete from webhook where id = ?", id);
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from webhook", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<UUID, Webhook> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from webhook where %s", specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(UUID id) {
    return jdbcTemplate.queryForObject("select count(*) from webhook where id = ?",
        Long.class, id) > 0;
  }

  @Override
  protected <E> Optional<E> get(UUID id, RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from webhook where id = ?",
        mapper, id).stream().findFirst();
  }

  @Override
  protected RowMapper<UUID> buildKeyMapper() {
    return (rs, rowNum) -> UUIDs.fromString(rs.getString("id"));
  }

  @Override
  protected RowMapper<Webhook> buildValueMapper() {
    return (rs, rowNum) -> {
      UUID id = UUIDs.fromString(rs.getString("id"));
      URI url = parseUriFromDatabase(rs.getString("url"));
      return new Webhook(id, url);
    };
  }

  private URI parseUriFromDatabase(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Malformed uri in database.", e);
    }
  }

}
