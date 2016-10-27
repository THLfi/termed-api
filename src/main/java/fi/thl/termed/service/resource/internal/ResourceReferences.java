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

public class ResourceReferences implements LuceneSpecification<ResourceId, Resource> {

  private ResourceId subjectId;
  private String attrId;

  public ResourceReferences(ResourceId subjectId, String attrId) {
    this.subjectId = subjectId;
    this.attrId = attrId;
  }

  @Override
  public boolean test(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equals(resourceId, new ResourceId(resource)));

    for (Map.Entry<String, ResourceId> entry : resource.getReferrers().entries()) {
      if (Objects.equals(entry.getKey(), attrId) &&
          Objects.equals(entry.getValue(), subjectId)) {
        return true;
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    return new TermQuery(new Term("referrers." + attrId + ".resourceId", subjectId.toString()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceReferences that = (ResourceReferences) o;
    return Objects.equals(subjectId, that.subjectId) &&
           Objects.equals(attrId, that.attrId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subjectId, attrId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("subjectId", subjectId)
        .add("attrId", attrId)
        .toString();
  }

}
