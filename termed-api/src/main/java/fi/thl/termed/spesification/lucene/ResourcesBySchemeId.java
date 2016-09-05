package fi.thl.termed.spesification.lucene;

import com.google.common.base.Objects;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.common.AbstractSpecification;

public class ResourcesBySchemeId extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource> {

  private final UUID schemeId;

  public ResourcesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource value) {
    return Objects.equal(resourceId.getSchemeId(), schemeId);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("scheme.id", schemeId.toString()));
  }

}