package fi.thl.termed.repository.lucene;

/**
 * Wrapper for Exceptions thrown by Lucene.
 */
public class LuceneException extends RuntimeException {

  public LuceneException(Throwable cause) {
    super(cause);
  }

}
