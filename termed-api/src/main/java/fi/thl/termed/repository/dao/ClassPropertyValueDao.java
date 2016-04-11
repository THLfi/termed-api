package fi.thl.termed.repository.dao;

import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.LangValue;

public interface ClassPropertyValueDao extends Dao<PropertyValueId<ClassId>, LangValue> {

}
