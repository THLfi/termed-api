package fi.thl.termed.spesification.resource;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.SqlSpecification;
import fi.thl.termed.spesification.AbstractSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourcesByClassId extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource>, SqlSpecification<ResourceId, Resource> {

  private final ClassId classId;

  public ResourcesByClassId(ClassId classId) {
    this.classId = classId;
  }

  public ClassId getClassId() {
    return classId;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));
    return Objects.equal(resourceId.getSchemeId(), classId.getSchemeId()) &&
           Objects.equal(resourceId.getTypeId(), classId.getId());
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("scheme.id", classId.getSchemeId().toString())), MUST);
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
    return Objects.equal(classId, that.classId);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(classId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("classId", classId)
        .toString();
  }

}
