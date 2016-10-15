package fi.thl.termed.configuration;

import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fi.thl.termed.domain.Resource;
import fi.thl.termed.domain.ResourceId;
import fi.thl.termed.util.index.Index;
import fi.thl.termed.util.index.lucene.LuceneIndex;
import fi.thl.termed.service.resource.ResourceDocumentConverter;

/**
 * Configures indexes.
 */
@Configuration
public class Indexes {

  @Bean
  public Index<ResourceId, Resource> resourceIndex(
      @Value("${fi.thl.termed.index:}") String indexPath, Gson gson) {
    return new LuceneIndex<ResourceId, Resource>(indexPath, new ResourceDocumentConverter(gson));
  }

}
