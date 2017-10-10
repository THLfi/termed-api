package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.Dao;
import java.io.Serializable;
import java.util.Optional;

public class DaoSequenceService<K extends Serializable> implements SequenceService<K> {

  private Dao<K, Integer> nodeSequenceDao;

  public DaoSequenceService(Dao<K, Integer> nodeSequenceDao) {
    this.nodeSequenceDao = nodeSequenceDao;
  }

  public int getAndAdvance(K sequenceId, User user) {
    return getAndAdvance(sequenceId, 1, user);
  }

  public int getAndAdvance(K sequenceId, int count, User user) {
    Optional<Integer> optionalValue = nodeSequenceDao.get(sequenceId, user);

    if (!optionalValue.isPresent()) {
      nodeSequenceDao.insert(sequenceId, count, user);
    } else {
      nodeSequenceDao.update(sequenceId, optionalValue.get() + count, user);
    }

    return optionalValue.orElse(0) + 1;
  }

}
