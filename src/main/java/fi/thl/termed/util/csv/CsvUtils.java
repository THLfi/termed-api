package fi.thl.termed.util.csv;

import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

public final class CsvUtils {

  private CsvUtils() {
  }

  public static void writeCsv(OutputStream out, CsvOptions opts, List<String[]> rows) {
    try {
      CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(out, opts.charset),
          opts.delimiter, opts.quoteChar, opts.escapeChar, opts.recordSeparator);
      csvWriter.writeAll(rows, opts.quoteAll);
      csvWriter.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
