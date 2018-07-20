package fi.thl.termed.service.property.internal;

import static java.lang.String.format;

import com.google.common.base.Strings;
import fi.thl.termed.domain.Property;
import fi.thl.termed.util.dao.AbstractJdbcDao2;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

public class JdbcPropertyDao extends AbstractJdbcDao2<String, Property> {

  public JdbcPropertyDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(String id, Property property) {
    jdbcTemplate.update("insert into property (id, uri, index) values (?, ?, ?)",
        id,
        property.getUri().map(Strings::emptyToNull).orElse(null),
        property.getIndex().orElse(null));
  }

  @Override
  public void update(String id, Property property) {
    jdbcTemplate.update("update property set uri = ?, index = ? where id = ?",
        property.getUri().map(Strings::emptyToNull).orElse(null),
        property.getIndex().orElse(null),
        id);
  }

  @Override
  public void delete(String id) {
    jdbcTemplate.update("delete from property where id = ?", id);
  }

  @Override
  protected <E> Stream<E> get(SqlSpecification<String, Property> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        format("select * from property where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(String id) {
    return jdbcTemplate.queryForOptional("select count(*) from property where id = ?",
        Long.class, id).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(String id, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        "select * from property where id = ?", mapper, id).stream().findFirst();
  }

  @Override
  protected RowMapper<String> buildKeyMapper() {
    return (rs, rowNum) -> rs.getString("id");
  }

  @Override
  protected RowMapper<Property> buildValueMapper() {
    return (rs, rowNum) -> Property.builder().id(rs.getString("id"))
        .uri(rs.getString("uri"))
        .index(rs.getInt("index"))
        .build();
  }

}
