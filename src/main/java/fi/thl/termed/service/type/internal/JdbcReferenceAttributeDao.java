package fi.thl.termed.service.type.internal;

import com.google.common.base.Strings;
import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.query.SqlSpecification;
import java.util.Optional;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;

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
        referenceAttribute.getUri().map(Strings::emptyToNull).orElse(null),
        referenceAttribute.getRangeGraphId(),
        referenceAttribute.getRangeId(),
        referenceAttribute.getIndex().orElse(null));
  }

  @Override
  public void update(ReferenceAttributeId referenceAttributeId,
      ReferenceAttribute referenceAttribute) {

    TypeId domainId = referenceAttributeId.getDomainId();

    jdbcTemplate.update(
        "update reference_attribute set uri = ?, range_graph_id = ?, range_id = ?, index = ? where domain_graph_id = ? and domain_id = ? and id = ?",
        referenceAttribute.getUri().map(Strings::emptyToNull).orElse(null),
        referenceAttribute.getRangeGraphId(),
        referenceAttribute.getRangeId(),
        referenceAttribute.getIndex().orElse(null),
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
  protected <E> Stream<E> get(
      SqlSpecification<ReferenceAttributeId, ReferenceAttribute> specification,
      RowMapper<E> mapper) {
    return jdbcTemplate.queryForStream(
        String.format("select * from reference_attribute where %s order by index",
            specification.sqlQueryTemplate()),
        specification.sqlQueryParameters(), mapper);
  }

  @Override
  public boolean exists(ReferenceAttributeId referenceAttributeId) {
    TypeId domainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.queryForOptional(
        "select count(*) from reference_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        Long.class,
        domainId.getGraphId(),
        domainId.getId(),
        referenceAttributeId.getId()).orElseThrow(IllegalStateException::new) > 0;
  }

  @Override
  protected <E> Optional<E> get(ReferenceAttributeId referenceAttributeId,
      RowMapper<E> mapper) {
    TypeId domainId = referenceAttributeId.getDomainId();

    return jdbcTemplate.queryForFirst(
        "select * from reference_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        mapper,
        domainId.getGraphId(),
        domainId.getId(),
        referenceAttributeId.getId());
  }

  @Override
  protected RowMapper<ReferenceAttributeId> buildKeyMapper() {
    return (rs, rowNum) -> {
      TypeId domainId = TypeId.of(
          rs.getString("domain_id"), GraphId.fromUuidString(rs.getString("domain_graph_id")));
      return new ReferenceAttributeId(domainId, rs.getString("id"));
    };
  }

  @Override
  protected RowMapper<ReferenceAttribute> buildValueMapper() {
    return (rs, rowNum) -> {
      TypeId domain = TypeId.of(
          rs.getString("domain_id"),
          GraphId.fromUuidString(rs.getString("domain_graph_id")));

      TypeId range = TypeId.of(
          rs.getString("range_id"),
          GraphId.fromUuidString(rs.getString("range_graph_id")));

      return ReferenceAttribute.builder()
          .id(rs.getString("id"), domain).range(range)
          .uri(rs.getString("uri"))
          .index(rs.getInt("index"))
          .build();
    };
  }

}
