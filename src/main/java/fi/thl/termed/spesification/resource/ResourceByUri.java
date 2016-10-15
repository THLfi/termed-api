package fi.thl.termed.spesification.resource;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.SqlSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourceByUri extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource>, SqlSpecification<ResourceId, Resource> {

  private UUID schemeId;
  private String uri;

  public ResourceByUri(UUID schemeId, String uri) {
    this.schemeId = schemeId;
    this.uri = uri;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));
    return Objects.equal(resource.getSchemeId(), schemeId) &&
           Objects.equal(resource.getUri(), uri);
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("scheme.id", schemeId.toString())), MUST);
    query.add(new TermQuery(new Term("uri", uri)), MUST);
    return query;
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and uri = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId, uri};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceByUri that = (ResourceByUri) o;
    return Objects.equal(schemeId, that.schemeId) &&
           Objects.equal(uri, that.uri);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId, uri);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .add("uri", uri)
        .toString();
  }

}
