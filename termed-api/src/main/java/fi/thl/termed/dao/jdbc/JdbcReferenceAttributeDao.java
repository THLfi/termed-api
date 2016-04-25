package fi.thl.termed.dao.jdbc;

import com.google.common.collect.Iterables;

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
import fi.thl.termed.spesification.sql.SqlSpecification;
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
    ClassId rangeId = referenceAttributeId.getRangeId();

    jdbcTemplate.update(
        "insert into reference_attribute (scheme_id, domain_id, range_scheme_id, range_id, id, uri, index) values (?, ?, ?, ?, ?, ?, ?)",
        domainId.getSchemeId(),
        domainId.getId(),
        rangeId.getSchemeId(),
        rangeId.getId(),
        referenceAttributeId.getId(),
        referenceAttribute.getUri(),
        referenceAttribute.getIndex());
  }

  @Override
  public void update(ReferenceAttributeId referenceAttributeId,
                     ReferenceAttribute referenceAttribute) {

    ClassId domainId = referenceAttributeId.getDomainId();
    ClassId rangeId = referenceAttributeId.getRangeId();

    jdbcTemplate.update(
        "update reference_attribute set uri = ?, index = ? where scheme_id = ? and domain_id = ? and range_scheme_id = ? and range_id = ? and id = ?",
        referenceAttribute.getUri(),
        referenceAttribute.getIndex(),
        domainId.getSchemeId(),
        domainId.getId(),
        rangeId.getSchemeId(),
        rangeId.getId(),
        referenceAttributeId.getId());
  }

  @Override
  public void delete(ReferenceAttributeId referenceAttributeId) {
    ClassId domainId = referenceAttributeId.getDomainId();
    ClassId rangeId = referenceAttributeId.getRangeId();

    jdbcTemplate.update(
        "delete from reference_attribute where scheme_id = ? and domain_id = ? and range_scheme_id = ? and range_id = ? and id = ?",
        domainId.getSchemeId(),
        domainId.getId(),
        rangeId.getSchemeId(),
        rangeId.getId(),
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
    ClassId rangeId = referenceAttributeId.getRangeId();

    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute where scheme_id = ? and domain_id = ? and range_scheme_id = ? and range_id = ? and id = ?",
        Long.class,
        domainId.getSchemeId(),
        domainId.getId(),
        rangeId.getSchemeId(),
        rangeId.getId(),
        referenceAttributeId.getId()) > 0;
  }

  @Override
  protected <E> E get(ReferenceAttributeId referenceAttributeId, RowMapper<E> mapper) {
    ClassId domainId = referenceAttributeId.getDomainId();
    ClassId rangeId = referenceAttributeId.getRangeId();

    return Iterables.getFirst(jdbcTemplate.query(
        "select * from reference_attribute where scheme_id = ? and domain_id = ? and range_scheme_id = ? and range_id = ? and id = ?",
        mapper,
        domainId.getSchemeId(),
        domainId.getId(),
        rangeId.getSchemeId(),
        rangeId.getId(),
        referenceAttributeId.getId()), null);
  }

  @Override
  protected RowMapper<ReferenceAttributeId> buildKeyMapper() {
    return new RowMapper<ReferenceAttributeId>() {
      public ReferenceAttributeId mapRow(ResultSet rs, int rowNum) throws SQLException {
        ClassId domainId = new ClassId(UUIDs.fromString(rs.getString("scheme_id")),
                                       rs.getString("domain_id"));
        ClassId rangeId = new ClassId(UUIDs.fromString(rs.getString("range_scheme_id")),
                                      rs.getString("range_id"));
        return new ReferenceAttributeId(domainId, rangeId, rs.getString("id"));
      }
    };
  }

  @Override
  protected RowMapper<ReferenceAttribute> buildValueMapper() {
    return new RowMapper<ReferenceAttribute>() {
      public ReferenceAttribute mapRow(ResultSet rs, int rowNum) throws SQLException {
        Class domain = new Class(rs.getString("domain_id"));
        domain.setScheme(new Scheme(UUIDs.fromString(rs.getString("scheme_id"))));
        Class range = new Class(rs.getString("range_id"));
        range.setScheme(new Scheme(UUIDs.fromString(rs.getString("range_scheme_id"))));

        ReferenceAttribute referenceAttribute =
            new ReferenceAttribute(rs.getString("id"), rs.getString("uri"));
        referenceAttribute.setDomain(domain);
        referenceAttribute.setRange(range);
        referenceAttribute.setIndex(rs.getInt("index"));

        return referenceAttribute;
      }
    };
  }

}
