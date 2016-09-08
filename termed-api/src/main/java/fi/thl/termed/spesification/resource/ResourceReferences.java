package fi.thl.termed.spesification.resource;

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

public class ResourceReferences extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource> {

  private ResourceId subjectId;
  private ReferenceAttributeId attrId;
  private ClassId rangeId;

  public ResourceReferences(ResourceId subjectId, ReferenceAttributeId attrId, ClassId rangeId) {
    Preconditions.checkArgument(Objects.equal(new ClassId(subjectId), attrId.getDomainId()));
    this.subjectId = subjectId;
    this.attrId = attrId;
    this.rangeId = rangeId;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));

    if (Objects.equal(new ClassId(resourceId), rangeId)) {
      for (Map.Entry<String, ResourceId> entry : resource.getReferrerIds().entries()) {
        if (Objects.equal(entry.getKey(), attrId.getId()) &&
            Objects.equal(entry.getValue(), subjectId)) {
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

}
