package fi.thl.termed.repository.dao.jdbc;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceAttributeValueId;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.repository.dao.ResourceBulkInsertDao;
import fi.thl.termed.util.StrictLangValue;

@Repository
public class PostgresResourceBulkInsertDao implements ResourceBulkInsertDao {

  private Logger log = LoggerFactory.getLogger(getClass());

  private CopyManager copyManager;

  private boolean supported;

  @Autowired
  public PostgresResourceBulkInsertDao(
      @Value("${spring.datasource.url}") String url,
      @Value("${spring.datasource.username}") String username,
      @Value("${spring.datasource.password}") String password)
      throws SQLException {

    Connection connection = null;

    try {
      connection = DriverManager.getConnection(url, username, password);
    } catch (SQLException e) {
      log.warn("{}", e.getMessage());
    }

    if (connection != null && connection instanceof BaseConnection) {
      this.copyManager = new CopyManager((BaseConnection) connection);
      this.supported = true;
    } else {
      log.warn("Can't initialize {} with {}", getClass(), connection);
      this.supported = false;
    }
  }

  public boolean isSupported() {
    return supported;
  }

  @Override
  public void insert(Map<ResourceId, Resource> resources) {
    Multimap<String, Object[]> batch = LinkedListMultimap.create();

    log.info("Creating copy statements");

    for (Map.Entry<ResourceId, Resource> entry : resources.entrySet()) {
      addResourcesToBatch(batch, entry.getKey(), entry.getValue());
    }

    for (String sql : batch.keySet()) {
      long startTime = System.nanoTime();
      log.info("Copy {} rows with {}", batch.get(sql).size(), sql);

      copyIn(sql, new StringReader(rowsToTsv(batch.get(sql))));

      log.info("Done in {} ms.", (System.nanoTime() - startTime) / 1000000);
    }
  }

  private String rowsToTsv(Collection<Object[]> rows) {
    StringBuilder builder = new StringBuilder();
    for (Object[] row : rows) {
      builder.append(Joiner.on('\t').useForNull("\\N").join(row)).append('\n');
    }
    return builder.toString();
  }

  private void copyIn(String sql, Reader reader) {
    try {
      copyManager.copyIn(sql, reader);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addResourcesToBatch(Multimap<String, Object[]> batch,
                                   ResourceId resourceId,
                                   Resource resource) {
    addToBatch(batch,
               "COPY resource (scheme_id, type_id, id, code, uri, created_by, created_date, last_modified_by, last_modified_date) FROM stdin",
               resourceId.getSchemeId(),
               resourceId.getTypeId(),
               resourceId.getId(),
               resource.getCode(),
               resource.getUri(),
               resource.getCreatedBy(),
               resource.getCreatedDate(),
               resource.getLastModifiedBy(),
               resource.getLastModifiedDate());

    addTextAttrValuesToBatch(batch, resourceId, resource.getProperties());
    addRefAttrValuesToBatch(batch, resourceId, resource.getReferences());
  }

  private void addTextAttrValuesToBatch(Multimap<String, Object[]> batch,
                                        ResourceId resourceId,
                                        Multimap<String, StrictLangValue> properties) {
    int index = 0;
    for (Map.Entry<String, StrictLangValue> entry : properties.entries()) {
      addTextAttrValueToBatch(batch,
                              new ResourceAttributeValueId(resourceId, entry.getKey(), index++),
                              entry.getValue());
    }
  }

  private void addTextAttrValueToBatch(Multimap<String, Object[]> batch,
                                       ResourceAttributeValueId resourceAttributeValueId,
                                       StrictLangValue langValue) {
    addToBatch(batch,
               "COPY resource_text_attribute_value (scheme_id, resource_type_id, resource_id, attribute_id, index, lang, value, regex) FROM stdin",
               resourceAttributeValueId.getResourceId().getSchemeId(),
               resourceAttributeValueId.getResourceId().getTypeId(),
               resourceAttributeValueId.getResourceId().getId(),
               resourceAttributeValueId.getAttributeId(),
               resourceAttributeValueId.getIndex(),
               langValue.getLang(),
               escapeTsv(langValue.getValue()),
               langValue.getRegex());
  }

  private String escapeTsv(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\t", "\\\t")
        .replace("\r", "\\\r")
        .replace("\n", "\\\\n");
  }

  private void addRefAttrValuesToBatch(Multimap<String, Object[]> batch,
                                       ResourceId resourceId,
                                       Multimap<String, Resource> references) {
    int index = 0;
    for (Map.Entry<String, Resource> entry : references.entries()) {
      addRefAttrValueToBatch(batch,
                             new ResourceAttributeValueId(resourceId, entry.getKey(), index++),
                             entry.getValue());
    }
  }

  private void addRefAttrValueToBatch(Multimap<String, Object[]> batch,
                                      ResourceAttributeValueId resourceAttributeValueId,
                                      Resource value) {
    addToBatch(batch,
               "COPY resource_reference_attribute_value (scheme_id, resource_type_id, resource_id, attribute_id, index, value_scheme_id, value_type_id, value_id) FROM stdin",
               resourceAttributeValueId.getResourceId().getSchemeId(),
               resourceAttributeValueId.getResourceId().getTypeId(),
               resourceAttributeValueId.getResourceId().getId(),
               resourceAttributeValueId.getAttributeId(),
               resourceAttributeValueId.getIndex(),
               value.getSchemeId(),
               value.getTypeId(),
               value.getId());
  }

  private void addToBatch(Multimap<String, Object[]> batch, String sql, Object... data) {
    batch.put(sql, data);
  }

}
