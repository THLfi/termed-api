package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;

/**
 * Defines interface for named sequences
 */
public interface NamedSequenceService<K extends Serializable> {

  Long getAndAdvance(K sequenceId, User user);

  Long getAndAdvance(K sequenceId, Long count, User user);

}
