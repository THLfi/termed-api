package fi.thl.termed.util.index.lucene;

/**
 * Wrapper for Exceptions thrown by Lucene.
 */
public class LuceneException extends RuntimeException {

  public LuceneException(Throwable cause) {
    super(cause);
  }

  public LuceneException(String message, Throwable cause) {
    super(message, cause);
  }

}
