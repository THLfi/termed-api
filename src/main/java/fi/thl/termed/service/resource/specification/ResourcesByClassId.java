package fi.thl.termed.service.resource.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourcesByClassId
    implements LuceneSpecification<ResourceId, Resource>, SqlSpecification<ResourceId, Resource> {

  private final ClassId classId;

  public ResourcesByClassId(ClassId classId) {
    this.classId = classId;
  }

  public ClassId getClassId() {
    return classId;
  }

  @Override
  public boolean test(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equals(resourceId, new ResourceId(resource)));
    return Objects.equals(resourceId.getTypeSchemeId(), classId.getSchemeId()) &&
           Objects.equals(resourceId.getTypeId(), classId.getId());
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("type.scheme.id", classId.getSchemeId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", classId.getId())), MUST);
    return query;
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and type_id = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{classId.getSchemeId(), classId.getId()};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourcesByClassId that = (ResourcesByClassId) o;
    return Objects.equals(classId, that.classId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("classId", classId)
        .toString();
  }

}
