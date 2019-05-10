package fi.thl.termed.domain;

import com.google.common.collect.Multimap;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface NodeTree {

  UUID getId();

  Optional<String> getCode();

  Optional<String> getUri();

  Long getNumber();

  String getCreatedBy();

  LocalDateTime getCreatedDate();

  String getLastModifiedBy();

  LocalDateTime getLastModifiedDate();

  TypeId getType();

  Multimap<String, StrictLangValue> getProperties();

  Multimap<String, ? extends NodeTree> getReferences();

  Multimap<String, ? extends NodeTree> getReferrers();

}
