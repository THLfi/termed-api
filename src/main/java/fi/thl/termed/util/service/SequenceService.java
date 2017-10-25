package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;

/**
 * Defines interface for sequences
 */
public interface SequenceService {

  Long getAndAdvance(User user);

}
