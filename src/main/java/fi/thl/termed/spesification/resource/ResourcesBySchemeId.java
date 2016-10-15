package fi.thl.termed.spesification.resource;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.UUID;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.AbstractSpecification;

public class ResourcesBySchemeId extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource>, SqlSpecification<ResourceId, Resource> {

  private final UUID schemeId;

  public ResourcesBySchemeId(UUID schemeId) {
    this.schemeId = schemeId;
  }

  public UUID getSchemeId() {
    return schemeId;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));
    return Objects.equal(resourceId.getSchemeId(), schemeId);
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("scheme.id", schemeId.toString()));
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{schemeId};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourcesBySchemeId that = (ResourcesBySchemeId) o;
    return Objects.equal(schemeId, that.schemeId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schemeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("schemeId", schemeId)
        .toString();
  }

}
