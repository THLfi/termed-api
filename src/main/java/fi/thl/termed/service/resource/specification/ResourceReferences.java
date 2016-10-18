package fi.thl.termed.service.resource.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Map;
import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.LuceneSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourceReferences implements LuceneSpecification<ResourceId, Resource> {

  private ResourceId subjectId;
  private ReferenceAttributeId attrId;
  private ClassId rangeId;

  public ResourceReferences(ResourceId subjectId, ReferenceAttributeId attrId, ClassId rangeId) {
    Preconditions.checkArgument(Objects.equals(new ClassId(subjectId), attrId.getDomainId()));
    this.subjectId = subjectId;
    this.attrId = attrId;
    this.rangeId = rangeId;
  }

  public ReferenceAttributeId getAttrId() {
    return attrId;
  }

  public ResourceId getSubjectId() {
    return subjectId;
  }

  public ClassId getRangeId() {
    return rangeId;
  }

  @Override
  public boolean test(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equals(resourceId, new ResourceId(resource)));

    if (Objects.equals(new ClassId(resourceId), rangeId)) {
      for (Map.Entry<String, ResourceId> entry : resource.getReferrerIds().entries()) {
        if (Objects.equals(entry.getKey(), attrId.getId()) &&
            Objects.equals(entry.getValue(), subjectId)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("scheme.id", rangeId.getSchemeId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", rangeId.getId())), MUST);
    query.add(new TermQuery(new Term("referrers." + attrId.getId() + ".resourceId",
                                     subjectId.toString())), MUST);
    return query;
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
           Objects.equals(attrId, that.attrId) &&
           Objects.equals(rangeId, that.rangeId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subjectId, attrId, rangeId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("subjectId", subjectId)
        .add("attrId", attrId)
        .add("rangeId", rangeId)
        .toString();
  }

}
