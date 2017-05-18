package fi.thl.termed.util.index.lucene;

import static com.google.common.base.Strings.isNullOrEmpty;
import static fi.thl.termed.util.index.lucene.LuceneConstants.DOCUMENT_ID;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

import fi.thl.termed.util.Converter;
import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.Specification;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LuceneIndex<K extends Serializable, V> implements Index<K, V> {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private Converter<V, Document> documentConverter;
  private Converter<K, String> keyConverter;

  private IndexWriter writer;
  private SearcherManager searcherManager;

  private TimerTask refreshTask;
  private TimerTask commitTask;

  private ExecutorService indexingExecutor;

  public LuceneIndex(String directoryPath,
      Converter<K, String> keyConverter,
      Converter<V, Document> documentConverter) {

    this.keyConverter = keyConverter;
    this.documentConverter = documentConverter;

    try {
      Analyzer a = new LowerCaseWhitespaceAnalyzer();
      IndexWriterConfig c = new IndexWriterConfig(a).setOpenMode(CREATE_OR_APPEND);
      this.writer = new IndexWriter(openDirectory(directoryPath), c);
      this.searcherManager = new SearcherManager(writer, new SearcherFactory());
    } catch (IOException e) {
      throw new LuceneException(e);
    }

    this.refreshTask = new TimerTask() {
      public void run() {
        refresh();
      }
    };
    this.commitTask = new TimerTask() {
      public void run() {
        commit();
      }
    };

    new Timer().schedule(refreshTask, 0, 1000);
    new Timer().schedule(commitTask, 0, 10000);

    this.indexingExecutor = Executors.newSingleThreadExecutor();
  }

  private Directory openDirectory(String directoryPath) throws IOException {
    log.info("Opening index directory {}", directoryPath);
    return isNullOrEmpty(directoryPath) ? new RAMDirectory()
        : FSDirectory.open(Paths.get(directoryPath));
  }

  @Override
  public void index(List<K> keys, Function<K, Optional<V>> valueProvider) {
    indexingExecutor.submit(new IndexingTask(keys, valueProvider));
  }

  @Override
  public void index(K key, V value) {
    Term documentIdTerm = new Term(DOCUMENT_ID, keyConverter.apply(key));

    Document document = requireNonNull(documentConverter.apply(value));
    document.add(new StringField(documentIdTerm.field(), documentIdTerm.text(), Field.Store.YES));

    try {
      writer.updateDocument(documentIdTerm, document);
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  @Override
  public Stream<V> get(Specification<K, V> specification, List<String> sort, int max) {
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();
      Query query = ((LuceneSpecification<K, V>) specification).luceneQuery();
      return query(searcher, query, max, sort, documentConverter.inverse());
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, List<String> sort, int max) {
    IndexSearcher searcher = null;
    try {
      searcher = searcherManager.acquire();
      Query query = ((LuceneSpecification<K, V>) specification).luceneQuery();
      return query(searcher, query, max, sort, d -> keyConverter.applyInverse(d.get(DOCUMENT_ID)));
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  @Override
  public boolean isEmpty() {
    try {
      IndexSearcher searcher = searcherManager.acquire();
      try {
        TotalHitCountCollector hitCountCollector = new TotalHitCountCollector();
        searcher.search(new MatchAllDocsQuery(), hitCountCollector);
        return hitCountCollector.getTotalHits() == 0;
      } finally {
        searcherManager.release(searcher);
      }
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  @Override
  public Stream<V> get(List<K> ids) {
    IndexSearcher searcher = null;
    try {
      BooleanQuery.Builder q = new BooleanQuery.Builder();
      ids.forEach(k -> q.add(new TermQuery(new Term(DOCUMENT_ID, keyConverter.apply(k))), SHOULD));
      searcher = searcherManager.acquire();
      return query(searcher, q.build(), ids.size(), emptyList(), documentConverter.inverse());
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  @Override
  public Optional<V> get(K id) {
    IndexSearcher searcher = null;
    try {
      TermQuery q = new TermQuery(new Term(DOCUMENT_ID, keyConverter.apply(id)));
      searcher = searcherManager.acquire();
      return query(searcher, q, 1, emptyList(), documentConverter.inverse()).findFirst();
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  private <E> Stream<E> query(IndexSearcher searcher, Query query, int max, List<String> orderBy,
      Function<Document, E> documentDeserializer) throws IOException {
    log.trace("{}", query);
    TopFieldDocs docs = searcher.search(query, max > 0 ? max : Integer.MAX_VALUE, sort(orderBy));
    return Arrays.stream(docs.scoreDocs)
        .map(new ScoreDocLoader(searcher))
        .map(documentDeserializer)
        .onClose(() -> tryRelease(searcher));
  }

  private void tryRelease(IndexSearcher searcher) {
    try {
      searcherManager.release(searcher);
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  private Sort sort(List<String> orderBy) {
    if (ListUtils.isNullOrEmpty(orderBy)) {
      return new Sort(SortField.FIELD_SCORE);
    }
    List<SortField> sortFields = orderBy.stream()
        .map(field -> new SortField(field, SortField.Type.STRING))
        .collect(Collectors.toList());
    return new Sort(sortFields.toArray(new SortField[sortFields.size()]));
  }

  @Override
  public void delete(K key) {
    try {
      writer.deleteDocuments(new Term(DOCUMENT_ID, keyConverter.apply(key)));
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  public void refresh() {
    try {
      searcherManager.maybeRefresh();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  public void refreshBlocking() {
    try {
      searcherManager.maybeRefreshBlocking();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  public void commit() {
    try {
      writer.commit();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  public void close() {
    log.debug("Closing {}", getClass().getSimpleName());

    try {
      indexingExecutor.shutdown();
      refreshTask.cancel();
      searcherManager.close();
      commitTask.cancel();
      writer.close();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  private class IndexingTask implements Runnable {

    private List<K> keys;
    private Function<K, Optional<V>> valueProvider;

    IndexingTask(List<K> keys, Function<K, Optional<V>> valueProvider) {
      this.keys = keys;
      this.valueProvider = valueProvider;
    }

    @Override
    public void run() {
      log.info("Indexing");

      ProgressReporter reporter = new ProgressReporter(log, "Indexed", 1000, keys.size());

      for (K id : keys) {
        Optional<V> value = valueProvider.apply(id);

        if (value.isPresent()) {
          index(id, value.get());
        } else {
          delete(id);
        }

        reporter.tick();
      }

      reporter.report();

      log.info("Done");
    }
  }

  private class ScoreDocLoader implements Function<ScoreDoc, Document> {

    private IndexSearcher searcher;

    ScoreDocLoader(IndexSearcher searcher) {
      this.searcher = searcher;
    }

    @Override
    public Document apply(ScoreDoc scoreDoc) {
      try {
        return searcher.doc(scoreDoc.doc);
      } catch (IOException e) {
        throw new LuceneException(e);
      }
    }
  }

}
