package fi.thl.termed.spesification.resource;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.UUID;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.SqlSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourceByCode extends AbstractSpecification<ResourceId, Resource>
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
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));
    return Objects.equal(resource.getSchemeId(), classId.getSchemeId()) &&
           Objects.equal(resource.getTypeId(), classId.getId()) &&
           Objects.equal(resource.getCode(), code);
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

}
