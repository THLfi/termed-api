package fi.thl.termed.repository.dao.jdbc;

import com.google.common.collect.Iterables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.repository.dao.TextAttributeDao;
import fi.thl.termed.repository.spesification.SqlSpecification;
import fi.thl.termed.util.UUIDs;

@Repository
public class JdbcTextAttributeDao
    extends AbstractJdbcDao<TextAttributeId, TextAttribute> implements TextAttributeDao {

  @Autowired
  public JdbcTextAttributeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(TextAttributeId textAttributeId, TextAttribute textAttribute) {
    ClassId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into text_attribute (scheme_id, domain_id, regex, id, uri, index) values (?, ?, ?, ?, ?, ?)",
        domainId.getSchemeId(),
        domainId.getId(),
        textAttribute.getRegex(),
        textAttribute.getId(),
        textAttribute.getUri(),
        textAttribute.getIndex());
  }

  @Override
  public void update(TextAttributeId textAttributeId, TextAttribute textAttribute) {
    ClassId domainId = textAttributeId.getDomainId();

    // update part of the key (regex), update is expected to cascade
    jdbcTemplate.update(
        "update text_attribute set uri = ?, index = ?, regex = ? where scheme_id = ? and domain_id = ? and id = ?",
        textAttribute.getUri(),
        textAttribute.getIndex(),
        textAttribute.getRegex(),
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getId());
  }

  @Override
  public void delete(TextAttributeId textAttributeId) {
    ClassId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from text_attribute where scheme_id = ? and domain_id = ? and regex = ? and id = ?",
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getRegex(),
        textAttributeId.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from text_attribute", mapper);
  }

  @Override
  protected <E> List<E> get(SqlSpecification<TextAttributeId, TextAttribute> specification,
                            RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from text_attribute where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(TextAttributeId textAttributeId) {
    ClassId domainId = textAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from text_attribute where scheme_id = ? and domain_id = ? and regex = ? and id = ?",
        Long.class,
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getRegex(),
        textAttributeId.getId()) > 0;
  }

  @Override
  protected <E> E get(TextAttributeId textAttributeId, RowMapper<E> mapper) {
    ClassId domainId = textAttributeId.getDomainId();

    return Iterables.getFirst(jdbcTemplate.query(
        "select * from text_attribute where scheme_id = ? and domain_id = ? and regex = ? and id = ?",
        mapper,
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getRegex(),
        textAttributeId.getId()), null);
  }

  @Override
  protected RowMapper<TextAttributeId> buildKeyMapper() {
    return new RowMapper<TextAttributeId>() {
      public TextAttributeId mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClassId domainId =
            new ClassId(UUIDs.fromString(rs.getString("scheme_id")), rs.getString("domain_id"));
        return new TextAttributeId(domainId, rs.getString("regex"), rs.getString("id"));
      }
    };
  }

  @Override
  protected RowMapper<TextAttribute> buildValueMapper() {
    return new RowMapper<TextAttribute>() {
      public TextAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
        Class domain = new Class(rs.getString("domain_id"));
        domain.setScheme(new Scheme(UUIDs.fromString(rs.getString("scheme_id"))));

        TextAttribute textAttribute = new TextAttribute(rs.getString("id"), rs.getString("uri"));
        textAttribute.setDomain(domain);
        textAttribute.setRegex(rs.getString("regex"));
        textAttribute.setIndex(rs.getInt("index"));

        return textAttribute;
      }
    };
  }

}
