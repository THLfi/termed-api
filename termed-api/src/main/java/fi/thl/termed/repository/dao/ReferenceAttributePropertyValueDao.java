package fi.thl.termed.repository.dao;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.util.LangValue;

public interface ReferenceAttributePropertyValueDao
    extends Dao<PropertyValueId<ReferenceAttributeId>, LangValue> {

}
