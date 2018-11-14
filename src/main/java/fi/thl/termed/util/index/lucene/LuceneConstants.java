package fi.thl.termed.util.index.lucene;

import org.apache.lucene.index.IndexWriter;

public final class LuceneConstants {

  private static final int MAX_BYTES_IN_UTF8_CHAR = 4;

  public static final String DOCUMENT_ID = "_document_id";
  public static final String DEFAULT_SEARCH_FIELD = "_all";
  public static final String CACHED_RESULT_FIELD = "_cached_result";
  public static final String CACHED_REFERRERS_FIELD = "_cached_referrers";
  public static final int MAX_SAFE_TERM_LENGTH_IN_UTF8_CHARS =
      IndexWriter.MAX_TERM_LENGTH / MAX_BYTES_IN_UTF8_CHAR;

  private LuceneConstants() {
  }

}
