package fi.thl.termed.service.resource.specification;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Objects;
import java.util.UUID;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.specification.LuceneSpecification;
import fi.thl.termed.util.specification.SqlSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourceByCode
    implements LuceneSpecification<ResourceId, Resource>, SqlSpecification<ResourceId, Resource> {

  private ClassId classId;
  private String code;

  public ResourceByCode(UUID schemeId, String typeId, String code) {
    this(new ClassId(schemeId, typeId), code);
  }

  public ResourceByCode(ClassId classId, String code) {
    this.classId = classId;
    this.code = code;
  }

  @Override
  public boolean test(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equals(resourceId, new ResourceId(resource)));
    return Objects.equals(resource.getSchemeId(), classId.getSchemeId()) &&
           Objects.equals(resource.getTypeId(), classId.getId()) &&
           Objects.equals(resource.getCode(), code);
  }

  @Override
  public Query luceneQuery() {
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("scheme.id", classId.getSchemeId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", classId.getId())), MUST);
    query.add(new TermQuery(new Term("coded", code)), MUST);
    return query;
  }

  @Override
  public String sqlQueryTemplate() {
    return "scheme_id = ? and type_id = ? and code = ?";
  }

  @Override
  public Object[] sqlQueryParameters() {
    return new Object[]{classId.getSchemeId(), classId.getId(), code};
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourceByCode that = (ResourceByCode) o;
    return Objects.equals(classId, that.classId) &&
           Objects.equals(code, that.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classId, code);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("classId", classId)
        .add("code", code)
        .toString();
  }

}
