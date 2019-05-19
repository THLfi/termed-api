package fi.thl.termed.util.index.lucene;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;

public class SimpleAllCollector extends SimpleCollector {

  private int docBase = 0;
  private List<Integer> docs = new ArrayList<>();

  @Override
  protected void doSetNextReader(LeafReaderContext context) {
    docBase = context.docBase;
  }

  @Override
  public void collect(int doc) {
    docs.add(docBase + doc);
  }

  @Override
  public ScoreMode scoreMode() {
    return ScoreMode.COMPLETE_NO_SCORES;
  }

  public List<Integer> getDocs() {
    return docs;
  }

}
