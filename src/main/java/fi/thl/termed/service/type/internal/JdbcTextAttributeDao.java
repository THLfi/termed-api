package fi.thl.termed.service.type.internal;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import fi.thl.termed.domain.GraphId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.UUIDs;
import fi.thl.termed.util.dao.AbstractJdbcDao;
import fi.thl.termed.util.specification.SqlSpecification;

import static com.google.common.base.Strings.emptyToNull;

public class JdbcTextAttributeDao extends AbstractJdbcDao<TextAttributeId, TextAttribute> {

  public JdbcTextAttributeDao(DataSource dataSource) {
    super(dataSource);
  }

  @Override
  public void insert(TextAttributeId textAttributeId, TextAttribute textAttribute) {
    TypeId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "insert into text_attribute (domain_graph_id, domain_id, id, uri, regex, index) values (?, ?, ?, ?, ?, ?)",
        domainId.getGraphId(),
        domainId.getId(),
        textAttributeId.getId(),
        emptyToNull(textAttribute.getUri()),
        textAttribute.getRegex(),
        textAttribute.getIndex());
  }

  @Override
  public void update(TextAttributeId textAttributeId, TextAttribute textAttribute) {
    TypeId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "update text_attribute set uri = ?, regex = ?, index = ? where domain_graph_id = ? and domain_id = ? and id = ?",
        emptyToNull(textAttribute.getUri()),
        textAttribute.getRegex(),
        textAttribute.getIndex(),
        domainId.getGraphId(),
        domainId.getId(),
        textAttributeId.getId());
  }

  @Override
  public void delete(TextAttributeId textAttributeId) {
    TypeId domainId = textAttributeId.getDomainId();

    jdbcTemplate.update(
        "delete from text_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        domainId.getGraphId(),
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
    TypeId domainId = textAttributeId.getDomainId();

    return jdbcTemplate.queryForObject(
        "select count(*) from text_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        Long.class,
        domainId.getGraphId(),
        domainId.getId(),
        textAttributeId.getId()) > 0;
  }

  @Override
  protected <E> Optional<E> get(TextAttributeId textAttributeId, RowMapper<E> mapper) {
    TypeId domainId = textAttributeId.getDomainId();

    return jdbcTemplate.query(
        "select * from text_attribute where domain_graph_id = ? and domain_id = ? and id = ?",
        mapper,
        domainId.getGraphId(),
        domainId.getId(),
        textAttributeId.getId()).stream().findFirst();
  }

  @Override
  protected RowMapper<TextAttributeId> buildKeyMapper() {
    return (rs, rowNum) -> {
      TypeId domainId =
          new TypeId(rs.getString("domain_id"), UUIDs.fromString(rs.getString("domain_graph_id")));
      return new TextAttributeId(domainId, rs.getString("id"));
    };
  }

  @Override
  protected RowMapper<TextAttribute> buildValueMapper() {
    return (rs, rowNum) -> {
      GraphId domainGraph = new GraphId(UUIDs.fromString(rs.getString("domain_graph_id")));
      TypeId domain = new TypeId(rs.getString("domain_id"), domainGraph);

      TextAttribute textAttribute = new TextAttribute(rs.getString("id"), domain);
      textAttribute.setUri(rs.getString("uri"));
      textAttribute.setRegex(rs.getString("regex"));
      textAttribute.setIndex(rs.getInt("index"));

      return textAttribute;
    };
  }

}
