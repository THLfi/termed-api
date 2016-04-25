package fi.thl.termed.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import fi.thl.termed.util.LangValue;

public class LangValueRowMapper implements RowMapper<LangValue> {

  @Override
  public LangValue mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new LangValue(rs.getString("lang"), rs.getString("value"));
  }

}
