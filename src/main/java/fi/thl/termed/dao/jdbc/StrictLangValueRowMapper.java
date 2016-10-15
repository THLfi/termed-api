package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import fi.thl.termed.domain.StrictLangValue;

public class StrictLangValueRowMapper implements RowMapper<StrictLangValue> {

  @Override
  public StrictLangValue mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new StrictLangValue(rs.getString("lang"),
                               rs.getString("value"),
                               rs.getString("regex"));
  }

}
