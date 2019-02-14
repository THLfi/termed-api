package fi.thl.termed.util.index.lucene;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static fi.thl.termed.util.collect.FunctionUtils.toUnchecked;
import static fi.thl.termed.util.collect.StreamUtils.findFirstAndClose;
import static fi.thl.termed.util.collect.StreamUtils.toStreamWithTimeout;
import static fi.thl.termed.util.index.lucene.LuceneConstants.DOCUMENT_ID;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static org.apache.lucene.index.IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;

import fi.thl.termed.util.Converter;
import fi.thl.termed.util.collect.ListUtils;
import fi.thl.termed.util.collect.StreamUtils;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.concurrent.ExecutorUtils;
import fi.thl.termed.util.concurrent.FutureUtils;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.query.LuceneSpecification;
import fi.thl.termed.util.query.Specification;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private ExecutorService indexingExecutor;
  private ScheduledExecutorService scheduledExecutorService;

  private Pattern descendingSortPattern = Pattern.compile("(.*)[ |+]desc$");

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

    this.indexingExecutor = ExecutorUtils.newScheduledThreadPool(1);
    this.scheduledExecutorService = ExecutorUtils.newScheduledThreadPool(5);

    this.scheduledExecutorService.scheduleAtFixedRate(this::refresh, 0, 1, TimeUnit.SECONDS);
    this.scheduledExecutorService.scheduleAtFixedRate(this::commit, 0, 10, TimeUnit.SECONDS);

    BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
  }

  private Directory openDirectory(String directoryPath) throws IOException {
    log.info("Opening index directory {}", directoryPath);
    return isNullOrEmpty(directoryPath) ? new RAMDirectory()
        : FSDirectory.open(Paths.get(directoryPath));
  }

  @Override
  public void index(Supplier<Stream<K>> keySupplier, Function<K, Optional<V>> valueProvider) {
    IndexingTask indexingTask = new IndexingTask(keySupplier, valueProvider);
    addCallback(listeningDecorator(indexingExecutor).submit(indexingTask),
        FutureUtils.errorHandler(t -> log.error("", t)), directExecutor());
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
    return get(specification, sort, max, null, documentConverter.inverse());
  }

  /**
   * Expert method for searching and loading results with custom Lucene Document deserializer.
   */
  public Stream<V> get(Specification<K, V> specification, List<String> sort, int max,
      Set<String> fieldsToLoad, Function<Document, V> documentDeserializer) {
    IndexSearcher searcher = null;
    try {
      searcher = tryAcquire();
      Query query = ((LuceneSpecification<K, V>) specification).luceneQuery();
      return query(searcher, query, max, sort, fieldsToLoad, documentDeserializer);
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  @Override
  public Stream<K> getKeys(Specification<K, V> specification, List<String> sort, int max) {
    IndexSearcher searcher = null;
    try {
      searcher = tryAcquire();
      Query query = ((LuceneSpecification<K, V>) specification).luceneQuery();
      return query(searcher, query, max, sort, singleton(DOCUMENT_ID),
          d -> keyConverter.applyInverse(d.get(DOCUMENT_ID)));
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  @Override
  public long count(Specification<K, V> specification) {
    IndexSearcher searcher = null;
    try {
      searcher = tryAcquire();
      TotalHitCountCollector hitCountCollector = new TotalHitCountCollector();
      Query query = ((LuceneSpecification<K, V>) specification).luceneQuery();
      searcher.search(query, hitCountCollector);
      return hitCountCollector.getTotalHits();
    } catch (IOException e) {
      throw new LuceneException(e);
    } finally {
      tryRelease(searcher);
    }
  }

  @Override
  public boolean isEmpty() {
    IndexSearcher searcher = null;
    try {
      searcher = tryAcquire();
      TotalHitCountCollector hitCountCollector = new TotalHitCountCollector();
      searcher.search(new MatchAllDocsQuery(), hitCountCollector);
      return hitCountCollector.getTotalHits() == 0;
    } catch (IOException e) {
      throw new LuceneException(e);
    } finally {
      tryRelease(searcher);
    }
  }

  @Override
  public Stream<V> get(List<K> ids) {
    IndexSearcher searcher = null;
    try {
      BooleanQuery.Builder q = new BooleanQuery.Builder();
      ids.forEach(k -> q.add(new TermQuery(new Term(DOCUMENT_ID, keyConverter.apply(k))), SHOULD));
      searcher = tryAcquire();
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
      searcher = tryAcquire();
      return findFirstAndClose(query(searcher, q, 1, emptyList(), documentConverter.inverse()));
    } catch (IOException e) {
      tryRelease(searcher);
      throw new LuceneException(e);
    }
  }

  private <E> Stream<E> query(IndexSearcher searcher, Query query, int max, List<String> orderBy,
      Function<Document, E> documentDeserializer) throws IOException {
    return query(searcher, query, max, orderBy, null, documentDeserializer);
  }

  // null in fieldsToLoad means load all
  private <E> Stream<E> query(IndexSearcher searcher, Query query, int max, List<String> orderBy,
      Set<String> fieldsToLoad, Function<Document, E> documentDeserializer) throws IOException {
    log.trace("{}", query);
    TopFieldDocs docs = searcher.search(query, max > 0 ? max : Integer.MAX_VALUE, sort(orderBy));
    return toStreamWithTimeout(Arrays.stream(docs.scoreDocs)
        .map(toUnchecked(scoreDoc -> searcher.doc(scoreDoc.doc, fieldsToLoad)))
        .map(documentDeserializer)
        .onClose(() -> tryRelease(searcher)), scheduledExecutorService, 1, TimeUnit.HOURS);
  }

  private IndexSearcher tryAcquire() {
    try {
      return searcherManager.acquire();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
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

    return new Sort(orderBy.stream()
        .map(field -> {
          Matcher m = descendingSortPattern.matcher(field);
          return m.matches() ?
              new SortField(m.group(1), SortField.Type.STRING, true) :
              new SortField(field, SortField.Type.STRING);
        }).toArray(SortField[]::new));
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
      scheduledExecutorService.shutdown();
      searcherManager.close();
      writer.close();
    } catch (IOException e) {
      throw new LuceneException(e);
    }
  }

  private class IndexingTask implements Callable<Void> {

    private Supplier<Stream<K>> keyStreamProvider;
    private Function<K, Optional<V>> valueProvider;

    IndexingTask(Supplier<Stream<K>> keyStreamProvider, Function<K, Optional<V>> valueProvider) {
      this.keyStreamProvider = keyStreamProvider;
      this.valueProvider = valueProvider;
    }

    @Override
    public Void call() {
      log.info("Indexing");

      try (Stream<K> keyStream = keyStreamProvider.get()) {
        StreamUtils.zipIndex(keyStream, Tuple::of).forEach(t -> {

          try {
            Optional<V> value = valueProvider.apply(t._1);

            if (value.isPresent()) {
              index(t._1, value.get());
            } else {
              delete(t._1);
            }
          } catch (Throwable ex) {
            log.error("", ex);
            throw ex;
          }

          if (t._2 % 1000 == 0) {
            log.debug("Indexed {} values", t._2);
          }
        });
      }

      log.info("Done");
      return null;
    }

  }

}
