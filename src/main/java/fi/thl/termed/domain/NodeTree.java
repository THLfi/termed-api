package fi.thl.termed.domain;

import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.UUID;

public interface NodeTree {

  UUID getId();

  String getCode();

  String getUri();

  String getCreatedBy();

  Date getCreatedDate();

  String getLastModifiedBy();

  Date getLastModifiedDate();

  TypeId getType();

  Multimap<String, StrictLangValue> getProperties();

  Multimap<String, ? extends NodeTree> getReferences();

  Multimap<String, ? extends NodeTree> getReferrers();

}
