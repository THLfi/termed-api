package fi.thl.termed.util.index.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

/**
 * Analyzer that tokenizes on whitespace and ignores case. (Similar to SimpleAnalyzer that tokenizes
 * more aggressively on all non-letter character.)
 */
public final class LowerCaseWhitespaceAnalyzer extends Analyzer {

  @Override
  protected TokenStreamComponents createComponents(String fieldName) {
    Tokenizer source = new WhitespaceTokenizer();
    TokenStream filter = new LowerCaseFilter(source);
    return new TokenStreamComponents(source, filter);
  }

}
