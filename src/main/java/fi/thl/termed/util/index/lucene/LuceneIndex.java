package fi.thl.termed.util.index.lucene;

import com.google.common.base.Converter;
import com.google.common.collect.Lists;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import fi.thl.termed.util.ProgressReporter;
import fi.thl.termed.util.ToStringFunction;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.Query;
import fi.thl.termed.util.specification.Specification;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.transform;
import static fi.thl.termed.util.index.lucene.LuceneConstants.DOCUMENT_ID;
import static java.util.Arrays.asList;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.util.Version.LUCENE_47;

public class LuceneIndex<K extends Serializable, V> implements Index<K, V> {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private Converter<V, Document> documentConverter;
  private Function<K, String> keyToString;

  private Directory directory;
  private Analyzer analyzer;

  private IndexWriter writer;
  private SearcherManager searcherManager;

  private TimerTask refreshTask;
  private TimerTask commitTask;

  private ExecutorService indexingExecutor;

  public LuceneIndex(String directoryPath,
                     Converter<V, Document> documentConverter) {

    this.documentConverter = documentConverter;
    this.keyToString = new ToStringFunction<K>();

    this.analyzer = new LowerCaseWhitespaceAnalyzer(LUCENE_47);

    try {
      this.directory = isNullOrEmpty(directoryPath) ? new RAMDirectory()
                                                    : FSDirectory.open(new File(directoryPath));
      this.writer = new IndexWriter(
          directory, new IndexWriterConfig(LUCENE_47, analyzer).setOpenMode(CREATE_OR_APPEND));
      this.searcherManager = new SearcherManager(writer, true, new SearcherFactory());
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

  @Override
  public void reindex(List<K> ids, java.util.function.Function<K, V> objectLoadingFunction) {
    indexingExecutor.submit(new IndexingTask(ids, objectLoadingFunction));
  }

  @Override
  public void reindex(K id, V object) {
    Term docIdTerm = new Term(DOCUMENT_ID, keyToString.apply(id));

    Document doc = documentConverter.convert(object);
    doc.add(new StringField(docIdTerm.field(), docIdTerm.text(), Field.Store.NO));

    try {
      writer.updateDocument(docIdTerm, doc);
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  @Override
  public int indexSize() {
    try {
      IndexSearcher searcher = searcherManager.acquire();
      try {
        TotalHitCountCollector hitCountCollector = new TotalHitCountCollector();
        searcher.search(new MatchAllDocsQuery(), hitCountCollector);
        return hitCountCollector.getTotalHits();
      } finally {
        searcherManager.release(searcher);
      }
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  @Override
  public List<V> query(Query<K, V> specificationQuery) {
    try {
      Specification<K, V> specification = specificationQuery.getSpecification();
      org.apache.lucene.search.Query query =
          ((LuceneSpecification<K, V>) specification).luceneQuery();
      log.trace("query: {}", query);
      IndexSearcher searcher = searcherManager.acquire();
      try {
        return query(searcher, query, specificationQuery.getMax(), specificationQuery.getOrderBy());
      } finally {
        searcherManager.release(searcher);
      }
    } catch (ParseException e) {
      log.error("{}", e.getMessage());
      return Collections.emptyList();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  private List<V> query(IndexSearcher searcher, org.apache.lucene.search.Query query, int max,
                        List<String> orderBy)
      throws IOException, ParseException {
    ScoreDoc[] hits = searcher.search(query, max, sort(orderBy)).scoreDocs;
    List<Document> documents = asList(hits).stream()
        .map(new ScoreDocLoader(searcher)).collect(Collectors.toList());
    return transform(documents, documentConverter.reverse());
  }

  private Sort sort(List<String> orderBy) {
    if (ListUtils.isNullOrEmpty(orderBy)) {
      return new Sort(SortField.FIELD_SCORE);
    }

    List<SortField> sortFields = Lists.newArrayList();
    for (String field : orderBy) {
      sortFields.add(new SortField(field, SortField.Type.STRING));
    }

    return new Sort(sortFields.toArray(new SortField[sortFields.size()]));
  }

  @Override
  public void deleteFromIndex(K key) {
    try {
      writer.deleteDocuments(new Term(DOCUMENT_ID, keyToString.apply(key)));
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  protected void refresh() {
    try {
      searcherManager.maybeRefresh();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  protected void refreshBlocking() {
    try {
      searcherManager.maybeRefreshBlocking();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  protected void commit() {
    try {
      writer.commit();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  @PreDestroy
  protected void close() {
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

    private List<K> ids;
    private Function<K, V> objectLoadingFunction;

    public IndexingTask(List<K> ids, java.util.function.Function<K, V> objectLoadingFunction) {
      this.ids = ids;
      this.objectLoadingFunction = objectLoadingFunction;
    }

    @Override
    public void run() {
      log.info("Indexing");

      ProgressReporter reporter = new ProgressReporter(log, "Indexed", 1000, ids.size());

      for (K id : ids) {
        reindex(id, objectLoadingFunction.apply(id));
        reporter.tick();
      }

      reporter.report();
    }
  }

  private class ScoreDocLoader implements Function<ScoreDoc, Document> {

    private IndexSearcher searcher;

    public ScoreDocLoader(IndexSearcher searcher) {
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
