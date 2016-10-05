package fi.thl.termed.dao.jdbc;

import com.google.common.base.Optional;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Scheme;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.util.ListUtils;
import fi.thl.termed.util.UUIDs;

public class JdbcReferenceAttributeDao
    extends AbstractJdbcDao<ReferenceAttributeId, ReferenceAttribute> {

  public JdbcReferenceAttributeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ReferenceAttributeId referenceAttributeId,
                     ReferenceAttribute referenceAttribute) {

    ClassId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into reference_attribute (scheme_id, domain_id, id, uri, range_scheme_id, range_id, index) values (?, ?, ?, ?, ?, ?, ?)",
        domainId.getSchemeId(),
        domainId.getId(),
        referenceAttributeId.getId(),
        referenceAttribute.getUri(),
        referenceAttribute.getRangeSchemeId(),
        referenceAttribute.getRangeId(),
        referenceAttribute.getIndex());
  }

  @Override
  public void update(ReferenceAttributeId referenceAttributeId,
                     ReferenceAttribute referenceAttribute) {

    ClassId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "update reference_attribute set uri = ?, range_scheme_id = ?, range_id = ?, index = ? where scheme_id = ? and domain_id = ? and id = ?",
        referenceAttribute.getUri(),
        referenceAttribute.getRangeSchemeId(),
        referenceAttribute.getRangeId(),
        referenceAttribute.getIndex(),
        domainId.getSchemeId(),
        domainId.getId(),
        referenceAttributeId.getId());
  }

  @Override
  public void delete(ReferenceAttributeId referenceAttributeId) {
    ClassId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from reference_attribute where scheme_id = ? and domain_id = ? and id = ?",
        domainId.getSchemeId(),
        domainId.getId(),
        referenceAttributeId.getId());
  }

  @Override
  protected <E> List<E> get(RowMapper<E> mapper) {
    return jdbcTemplate.query("select * from reference_attribute", mapper);
  }

  @Override
  protected <E> List<E> get(
      SqlSpecification<ReferenceAttributeId, ReferenceAttribute> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.query(
        String.format("select * from reference_attribute where %s order by index",
                      specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ReferenceAttributeId referenceAttributeId) {
    ClassId domainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute where scheme_id = ? and domain_id = ? and id = ?",
        Long.class,
        domainId.getSchemeId(),
        domainId.getId(),
        referenceAttributeId.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ReferenceAttributeId referenceAttributeId, RowMapper<E> mapper) {
    ClassId domainId = referenceAttributeId.getDomainId();

    return ListUtils.findFirst(jdbcTemplate.query(
        "select * from reference_attribute where scheme_id = ? and domain_id = ? and id = ?",
        mapper,
        domainId.getSchemeId(),
        domainId.getId(),
        referenceAttributeId.getId()));
  }

  @Override
  protected RowMapper<ReferenceAttributeId> buildKeyMapper() {
    return new RowMapper<ReferenceAttributeId>() {
      public ReferenceAttributeId mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClassId domainId = new ClassId(UUIDs.fromString(rs.getString("scheme_id")),
                                       rs.getString("domain_id"));
        return new ReferenceAttributeId(domainId, rs.getString("id"));
      }
    };
  }

  @Override
  protected RowMapper<ReferenceAttribute> buildValueMapper() {
    return new RowMapper<ReferenceAttribute>() {
      public ReferenceAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {

        Scheme domainScheme = new Scheme(UUIDs.fromString(rs.getString("scheme_id")));
        Class domain = new Class(domainScheme, rs.getString("domain_id"));

        Scheme rangeScheme = new Scheme(UUIDs.fromString(rs.getString("range_scheme_id")));
        Class range = new Class(rangeScheme, rs.getString("range_id"));

        ReferenceAttribute referenceAttribute =
            new ReferenceAttribute(domain, range, rs.getString("id"), rs.getString("uri"));
        referenceAttribute.setIndex(rs.getInt("index"));

        return referenceAttribute;
      }
    };
  }

}
