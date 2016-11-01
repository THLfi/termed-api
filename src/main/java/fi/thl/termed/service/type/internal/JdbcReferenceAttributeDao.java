package fi.thl.termed.service.type.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

public class JdbcReferenceAttributeDao
    extends AbstractJdbcDao<ReferenceAttributeId, ReferenceAttribute> {

  public JdbcReferenceAttributeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(ReferenceAttributeId referenceAttributeId,
                     ReferenceAttribute referenceAttribute) {

    TypeId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into reference_attribute (domain_graph_id, domain_id, id, uri, range_graph_id, range_id, index) values (?, ?, ?, ?, ?, ?, ?)",
        domainId.getGraphId(),
        domainId.getId(),
        referenceAttributeId.getId(),
        referenceAttribute.getUri(),
        referenceAttribute.getRangeGraphId(),
        referenceAttribute.getRangeId(),
        referenceAttribute.getIndex());
  }

  @Override
  public void update(ReferenceAttributeId referenceAttributeId,
                     ReferenceAttribute referenceAttribute) {

    TypeId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "update reference_attribute set uri = ?, range_graph_id = ?, range_id = ?, index = ? where domain_graph_id = ? and domain_id = ? and id = ?",
        referenceAttribute.getUri(),
        referenceAttribute.getRangeGraphId(),
        referenceAttribute.getRangeId(),
        referenceAttribute.getIndex(),
        domainId.getGraphId(),
        domainId.getId(),
        referenceAttributeId.getId());
  }

  @Override
  public void delete(ReferenceAttributeId referenceAttributeId) {
    TypeId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from reference_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        domainId.getGraphId(),
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
    TypeId domainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from reference_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        Long.class,
        domainId.getGraphId(),
        domainId.getId(),
        referenceAttributeId.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(ReferenceAttributeId referenceAttributeId,
                                RowMapper<E> mapper) {
    TypeId domainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.query(
        "select * from reference_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        mapper,
        domainId.getGraphId(),
        domainId.getId(),
        referenceAttributeId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<ReferenceAttributeId> buildKeyMapper() {
    return (rs, rowNum) -> {
      TypeId domainId = new TypeId(
          rs.getString("domain_id"), UUIDs.fromString(rs.getString("domain_graph_id")));
      return new ReferenceAttributeId(domainId, rs.getString("id"));
    };
  }

  @Override
  protected RowMapper<ReferenceAttribute> buildValueMapper() {
    return (rs, rowNum) -> {
      GraphId domainGraph = new GraphId(UUIDs.fromString(rs.getString("domain_graph_id")));
      TypeId domain = new TypeId(rs.getString("domain_id"), domainGraph);

      GraphId rangeGraph = new GraphId(UUIDs.fromString(rs.getString("range_graph_id")));
      TypeId range = new TypeId(rs.getString("range_id"), rangeGraph);

      ReferenceAttribute referenceAttribute =
          new ReferenceAttribute(rs.getString("id"), rs.getString("uri"), domain, range);
      referenceAttribute.setIndex(rs.getInt("index"));

      return referenceAttribute;
    };
  }

}
