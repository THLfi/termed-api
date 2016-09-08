package fi.thl.termed.spesification.resource;

import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.TrueSpecification;

public class AllResources extends TrueSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource> {

  // Index does not currently contain any other types than Resource,
  // if this were to change, this should be updated to query only Resource type docs.
  @Override
  public Query luceneQuery() {
    return new MatchAllDocsQuery();
  }

}
