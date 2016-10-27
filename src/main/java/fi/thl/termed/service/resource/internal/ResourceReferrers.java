package fi.thl.termed.service.resource.internal;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Map;
import java.util.Objects;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.LuceneSpecification;

public class ResourceReferrers implements LuceneSpecification<ResourceId, Resource> {

  private ResourceId objectId;
  private String attrId;

  public ResourceReferrers(ResourceId objectId, String attrId) {
    this.objectId = objectId;
    this.attrId = attrId;
  }

  @Override
  public boolean test(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equals(resourceId, new ResourceId(resource)));

    for (Map.Entry<String, ResourceId> entry : resource.getReferences().entries()) {
      if (Objects.equals(entry.getKey(), attrId) &&
          Objects.equals(entry.getValue(), objectId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term(attrId + ".resourceId", objectId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceReferrers that = (ResourceReferrers) o;
    return Objects.equals(objectId, that.objectId) &&
           Objects.equals(attrId, that.attrId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(objectId, attrId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("objectId", objectId)
        .add("attrId", attrId)
        .toString();
  }

}
