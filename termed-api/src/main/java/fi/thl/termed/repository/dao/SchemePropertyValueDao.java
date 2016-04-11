package fi.thl.termed.repository.dao;

import java.util.UUID;

import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.LangValue;

public interface SchemePropertyValueDao extends Dao<PropertyValueId<UUID>, LangValue> {

}
