package fi.thl.termed.spesification.resource;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.Map;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.spesification.AbstractSpecification;
import fi.thl.termed.spesification.LuceneSpecification;
import fi.thl.termed.util.StrictLangValue;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

public class ResourcesByTextAttributeValuePrefix
    extends AbstractSpecification<ResourceId, Resource>
    implements LuceneSpecification<ResourceId, Resource> {

  private final TextAttributeId attributeId;
  private final String value;

  public ResourcesByTextAttributeValuePrefix(TextAttributeId attributeId, String value) {
    this.attributeId = attributeId;
    this.value = value;
  }

  public TextAttributeId getAttributeId() {
    return attributeId;
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean accept(ResourceId resourceId, Resource resource) {
    Preconditions.checkArgument(Objects.equal(resourceId, new ResourceId(resource)));

    if (Objects.equal(new ClassId(resourceId), attributeId.getDomainId())) {
      for (Map.Entry<String, StrictLangValue> entry : resource.getProperties().entries()) {
        StrictLangValue attributeValue = entry.getValue();

        if (Objects.equal(entry.getKey(), attributeId.getId()) &&
            attributeValue.getValue().startsWith(value)) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public Query luceneQuery() {
    ClassId domainId = attributeId.getDomainId();
    BooleanQuery query = new BooleanQuery();
    query.add(new TermQuery(new Term("scheme.id", domainId.getSchemeId().toString())), MUST);
    query.add(new TermQuery(new Term("type.id", domainId.getId())), MUST);
    query.add(new PrefixQuery(new Term(attributeId.getId(), value)), MUST);
    return query;
  }

}
