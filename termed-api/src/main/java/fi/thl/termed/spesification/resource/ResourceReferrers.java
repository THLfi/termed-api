package fi.thl.termed.spesification.resource;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Map;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.LuceneSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourceReferrers extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource> {

  private ResourceId objectId;
  private ReferenceAttributeId attrId;

  public ResourceReferrers(ResourceId objectId, ReferenceAttributeId attrId, ClassId rangeId) {
    Preconditions.checkArgument(Objects.equal(new ClassId(objectId), rangeId));
    this.objectId = objectId;
    this.attrId = attrId;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));

    if (Objects.equal(new ClassId(resourceId), attrId.getDomainId())) {
      for (Map.Entry<String, ResourceId> entry : resource.getReferenceIds().entries()) {
        if (Objects.equal(entry.getKey(), attrId.getId()) &&
            Objects.equal(entry.getValue(), objectId)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    ClassId domainId = attrId.getDomainId();
    query.add(new TermQuery(new Term("scheme.id", domainId.getSchemeId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", domainId.getId())), MUST);
    query.add(new TermQuery(new Term(attrId.getId() + ".resourceId", objectId.toString())), MUST);
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
    ResourceReferrers that = (ResourceReferrers) o;
    return Objects.equal(objectId, that.objectId) &&
           Objects.equal(attrId, that.attrId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(objectId, attrId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("objectId", objectId)
        .add("attrId", attrId)
        .toString();
  }

}
