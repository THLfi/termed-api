package fi.thl.termed.util.csv;

import com.google.common.base.Charsets;
import java.nio.charset.Charset;

public class CsvOptions {

  public final char delimiter;
  public final char quoteChar;
  public final char escapeChar;
  public final String recordSeparator;
  public final boolean quoteAll;
  public final Charset charset;

  public CsvOptions(char delimiter, char quoteChar, char escapeChar, String recordSeparator,
      boolean quoteAll, Charset charset) {
    this.delimiter = delimiter != '\u0000' ? delimiter : ',';
    this.quoteChar = quoteChar != '\u0000' ? quoteChar : '"';
    this.escapeChar = escapeChar != '\u0000' ? escapeChar : '"';
    this.recordSeparator = recordSeparator != null ? recordSeparator : "\n";
    this.quoteAll = quoteAll;
    this.charset = charset != null ? charset : Charsets.UTF_8;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private char delimiter;
    private char quoteChar;
    private char escapeChar;
    private String recordSeparator;
    private boolean quoteAll;
    private Charset charset;

    public Builder delimiter(char delimiter) {
      this.delimiter = delimiter;
      return this;
    }

    public Builder delimiter(CsvDelimiter delimiter) {
      this.delimiter = delimiter.value();
      return this;
    }

    public Builder quoteChar(char quoteChar) {
      this.quoteChar = quoteChar;
      return this;
    }

    public Builder quoteChar(CsvQuoteChar quoteChar) {
      this.quoteChar = quoteChar.value();
      return this;
    }

    public Builder escapeChar(char escapeChar) {
      this.escapeChar = escapeChar;
      return this;
    }

    public Builder recordSeparator(String recordSeparator) {
      this.recordSeparator = recordSeparator;
      return this;
    }

    public Builder recordSeparator(CsvLineBreak recordSeparator) {
      this.recordSeparator = recordSeparator.value();
      return this;
    }

    public Builder quoteAll(boolean quoteAll) {
      this.quoteAll = quoteAll;
      return this;
    }

    public Builder charset(Charset charset) {
      this.charset = charset;
      return this;
    }

    public CsvOptions build() {
      return new CsvOptions(delimiter, quoteChar, escapeChar, recordSeparator, quoteAll, charset);
    }

  }

}
