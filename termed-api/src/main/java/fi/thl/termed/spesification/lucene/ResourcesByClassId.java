package fi.thl.termed.spesification.lucene;

import com.google.common.base.Objects;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.spesification.common.AbstractSpecification;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourcesByClassId extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource> {

  private final ClassId classId;

  public ResourcesByClassId(ClassId classId) {
    this.classId = classId;
  }

  public ClassId getClassId() {
    return classId;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource value) {
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

}
