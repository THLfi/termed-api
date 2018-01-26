package fi.thl.termed.util.postgresql;

import com.opencsv.CSVWriter;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

public final class CopyManagerUtils {

  private CopyManagerUtils() {
  }

  public static long copyInAsCsv(BaseConnection pgConnection, String sql, List<String[]> rows) {
    CharArrayWriter writer = new CharArrayWriter();
    new CSVWriter(writer).writeAll(rows);

    try {
      return new CopyManager(pgConnection).copyIn(sql, new CharArrayReader(writer.toCharArray()));
    } catch (SQLException | IOException e) {
      throw new RuntimeException(e);
    }
  }

}
