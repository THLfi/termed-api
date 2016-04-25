package fi.thl.termed.index.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;

/**
 * Analyzer that tokenizes on whitespace and ignores case. (Similar to SimpleAnalyzer that tokenizes
 * more aggressively on all non-letter character.)
 */
public final class LowerCaseWhitespaceAnalyzer extends Analyzer {

  private final Version matchVersion;

  public LowerCaseWhitespaceAnalyzer(Version matchVersion) {
    this.matchVersion = matchVersion;
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName,
                                                   Reader reader) {
    Tokenizer source = new WhitespaceTokenizer(matchVersion, reader);
    TokenStream filter = new LowerCaseFilter(matchVersion, source);
    return new TokenStreamComponents(source, filter);
  }

}
