package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcTextAttributeDao extends AbstractJdbcDao<TextAttributeId, TextAttribute> {

  public JdbcTextAttributeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(TextAttributeId textAttributeId, TextAttribute textAttribute) {
    ClassId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into text_attribute (scheme_id, domain_id, id, uri, regex, index) values (?, ?, ?, ?, ?, ?)",
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getId(),
        textAttribute.getUri(),
        textAttribute.getRegex(),
        textAttribute.getIndex());
  }

  @Override
  public void update(TextAttributeId textAttributeId, TextAttribute textAttribute) {
    ClassId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "update text_attribute set uri = ?, regex = ?, index = ? where scheme_id = ? and domain_id = ? and id = ?",
        textAttribute.getUri(),
        textAttribute.getRegex(),
        textAttribute.getIndex(),
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getId());
  }

  @Override
  public void delete(TextAttributeId textAttributeId) {
    ClassId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from text_attribute where scheme_id = ? and domain_id = ? and id = ?",
        domainId.getSchemeId(),
        domainId.getId(),
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
        "select count(*) from text_attribute where scheme_id = ? and domain_id = ? and id = ?",
        Long.class,
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(TextAttributeId textAttributeId, RowMapper<E> mapper) {
    ClassId domainId = textAttributeId.getDomainId();

    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from text_attribute where scheme_id = ? and domain_id = ? and id = ?",
        mapper,
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getId()));
  }

  @Override
  protected RowMapper<TextAttributeId> buildKeyMapper() {
    return new RowMapper<TextAttributeId>() {
      public TextAttributeId mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClassId domainId =
            new ClassId(UUIDs.fromString(rs.getString("scheme_id")), rs.getString("domain_id"));
        return new TextAttributeId(domainId, rs.getString("id"));
      }
    };
  }

  @Override
  protected RowMapper<TextAttribute> buildValueMapper() {
    return new RowMapper<TextAttribute>() {
      public TextAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
        Scheme domainScheme = new Scheme(UUIDs.fromString(rs.getString("scheme_id")));
        Class domain = new Class(domainScheme, rs.getString("domain_id"));

        TextAttribute textAttribute = new TextAttribute(domain, rs.getString("id"));
        textAttribute.setUri(rs.getString("uri"));
        textAttribute.setRegex(rs.getString("regex"));
        textAttribute.setIndex(rs.getInt("index"));

        return textAttribute;
      }
    };
  }

}
