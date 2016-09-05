package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.UUIDs;

public class JdbcClassDao extends AbstractJdbcDao<ClassId, Class> {

  public JdbcClassDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ClassId classId, Class newClass) {
    jdbcTemplate.update("insert into class (scheme_id, id, uri, index) values (?, ?, ?, ?)",
                        classId.getSchemeId(),
                        classId.getId(),
                        newClass.getUri(),
                        newClass.getIndex());
  }

  @Override
  public void update(ClassId classId, Class newClass) {
    jdbcTemplate.update("update class set uri = ?, index = ? where scheme_id = ? and id = ?",
                        newClass.getUri(),
                        newClass.getIndex(),
                        classId.getSchemeId(),
                        classId.getId());
  }

  @Override
  public void delete(ClassId classId) {
    jdbcTemplate.update("delete from class where scheme_id = ? and id = ?",
                        classId.getSchemeId(),
                        classId.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from class", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<ClassId, Class> specification, RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from class where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);

  }

  @Override
  public boolean exists(ClassId classId) {
    return jdbcTemplate.queryForObject("select count(*) from class where scheme_id = ? and id = ?",
                                       Long.class, classId.getSchemeId(), classId.getId()) > 0;
  }

  @Override
  protected <E> E get(ClassId classId, RowMapper<E> mapper) {
    return Iterables.getFirst(jdbcTemplate.query(
        "select * from class where scheme_id = ? and id = ?",
        mapper, classId.getSchemeId(), classId.getId()), null);
  }

  @Override
  protected RowMapper<ClassId> buildKeyMapper() {
    return new RowMapper<ClassId>() {
      public ClassId mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ClassId(UUIDs.fromString(rs.getString("scheme_id")),
                           rs.getString("id"));
      }
    };
  }

  @Override
  protected RowMapper<Class> buildValueMapper() {
    return new RowMapper<Class>() {
      public Class mapRow(ResultSet rs, int rowNum) throws SQLException {
        Class cls = new Class(rs.getString("id"), rs.getString("uri"));
        cls.setIndex(rs.getInt("index"));
        cls.setScheme(new Scheme(UUIDs.fromString(rs.getString("scheme_id"))));
        return cls;
      }
    };
  }

}
