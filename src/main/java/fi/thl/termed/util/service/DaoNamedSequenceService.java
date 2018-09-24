package fi.thl.termed.util.service;

import static com.google.common.base.Preconditions.checkArgument;

import fi.thl.termed.domain.User;
import fi.thl.termed.util.dao.Dao;
import java.io.Serializable;
import java.util.Optional;

public class DaoNamedSequenceService<K extends Serializable> implements NamedSequenceService<K> {

  private Dao<K, Long> nameSequenceDao;

  public DaoNamedSequenceService(Dao<K, Long> nameSequenceDao) {
    this.nameSequenceDao = nameSequenceDao;
  }

  @Override
  public Long get(K sequenceId, User user) {
    return nameSequenceDao.get(sequenceId, user).orElse(0L);
  }

  @Override
  public Long getAndAdvance(K sequenceId, User user) {
    return getAndAdvance(sequenceId, 1L, user);
  }

  @Override
  public Long getAndAdvance(K sequenceId, Long count, User user) {
    checkArgument(count > 0);

    Optional<Long> optionalValue = nameSequenceDao.get(sequenceId, user);

    if (!optionalValue.isPresent()) {
      nameSequenceDao.insert(sequenceId, count, user);
    } else {
      nameSequenceDao.update(sequenceId, optionalValue.get() + count, user);
    }

    return optionalValue.orElse(0L);
  }

  @Override
  public void set(K sequenceId, Long value, User user) {
    if (nameSequenceDao.exists(sequenceId, user)) {
      nameSequenceDao.update(sequenceId, value, user);
    } else {
      nameSequenceDao.insert(sequenceId, value, user);
    }
  }

}
