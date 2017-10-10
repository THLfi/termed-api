package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;

public interface SequenceService<K extends Serializable> {

  int getAndAdvance(K sequenceId, User user);

  int getAndAdvance(K sequenceId, int count, User user);

}
