package fi.thl.termed.repository.dao;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.TextAttributeId;
import fi.thl.termed.util.LangValue;

public interface TextAttributePropertyValueDao
    extends Dao<PropertyValueId<TextAttributeId>, LangValue> {

}
