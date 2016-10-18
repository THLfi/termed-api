package fi.thl.termed.service.property.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Property;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcPropertyDao extends AbstractJdbcDao<String, Property> {

  public JdbcPropertyDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(String id, Property property) {
    jdbcTemplate.update("insert into property (id, uri, index) values (?, ?, ?)",
                        id, property.getUri(), property.getIndex());
  }

  @Override
  public void update(String id, Property property) {
    jdbcTemplate.update("update property set uri = ?, index = ? where id = ?",
                        property.getUri(), property.getIndex(), id);
  }

  @Override
  public void delete(String id) {
    jdbcTemplate.update("delete from property where id = ?", id);
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from property order by index", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<String, Property> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from property where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(String id) {
    return jdbcTemplate.queryForObject("select count(*) from property where id = ?",
                                       Long.class, id) > 0;
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
    return (rs, rowNum) -> new Property(rs.getString("id"),
                                        rs.getString("uri"),
                                        rs.getInt("index"));
  }

}
