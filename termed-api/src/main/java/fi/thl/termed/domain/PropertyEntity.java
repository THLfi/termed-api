package fi.thl.termed.domain;

import com.google.common.collect.Multimap;

import fi.thl.termed.util.LangValue;

public interface PropertyEntity {

  Multimap<String, LangValue> getProperties();

  void setProperties(Multimap<String, LangValue> properties);

}
