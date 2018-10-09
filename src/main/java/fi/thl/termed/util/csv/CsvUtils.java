package fi.thl.termed.util.csv;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.List;

public final class CsvUtils {

  private CsvUtils() {
  }

  public static List<String[]> readCsv(CsvOptions opts, InputStream in) {
    CSVReader csvReader = new CSVReaderBuilder(
        new InputStreamReader(in, opts.charset))
        .withCSVParser(
            new CSVParserBuilder()
                .withSeparator(opts.delimiter)
                .withQuoteChar(opts.quoteChar)
                .withEscapeChar(opts.escapeChar != opts.quoteChar ? opts.escapeChar : '\\')
                .withStrictQuotes(opts.quoteAll)
                .build())
        .build();

    try {
      return csvReader.readAll();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      tryCloseReader(csvReader);
    }
  }

  private static void tryCloseReader(CSVReader csvReader) {
    try {
      csvReader.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void writeCsv(OutputStream out, CsvOptions opts, List<String[]> rows) {
    try {
      CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(out, opts.charset),
          opts.delimiter, opts.quoteChar, opts.escapeChar, opts.recordSeparator);
      csvWriter.writeAll(rows, opts.quoteAll);
      csvWriter.close();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
