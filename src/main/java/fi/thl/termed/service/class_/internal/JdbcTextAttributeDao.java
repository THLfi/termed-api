package fi.thl.termed.service.class_.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.domain.SchemeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

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

    return jdbcTemplate.query(
        "select * from text_attribute where scheme_id = ? and domain_id = ? and id = ?",
        mapper,
        domainId.getSchemeId(),
        domainId.getId(),
        textAttributeId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<TextAttributeId> buildKeyMapper() {
    return (rs, rowNum) -> {
      ClassId domainId =
          new ClassId(rs.getString("domain_id"), UUIDs.fromString(rs.getString("scheme_id")));
      return new TextAttributeId(domainId, rs.getString("id"));
    };
  }

  @Override
  protected RowMapper<TextAttribute> buildValueMapper() {
    return (rs, rowNum) -> {
      SchemeId domainScheme = new SchemeId(UUIDs.fromString(rs.getString("scheme_id")));
      ClassId domain = new ClassId(rs.getString("domain_id"), domainScheme);

      TextAttribute textAttribute = new TextAttribute(rs.getString("id"), domain);
      textAttribute.setUri(rs.getString("uri"));
      textAttribute.setRegex(rs.getString("regex"));
      textAttribute.setIndex(rs.getInt("index"));

      return textAttribute;
    };
  }

}
