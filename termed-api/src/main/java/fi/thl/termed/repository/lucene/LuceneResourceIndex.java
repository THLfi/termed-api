package fi.thl.termed.repository.lucene;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.repository.ResourceIndex;

@Repository
public class LuceneResourceIndex extends LuceneIndex<Resource, ResourceId>
    implements ResourceIndex {

  @Autowired
  public LuceneResourceIndex(@Value("${fi.thl.termed.index:}") String indexPath, Gson gson) {
    super(indexPath, new ResourceDocumentConverter(gson));
  }

}
