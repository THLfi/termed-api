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

  public Long getAndAdvance(K sequenceId, User user) {
    return getAndAdvance(sequenceId, 1L, user);
  }

  public Long getAndAdvance(K sequenceId, Long count, User user) {
    checkArgument(count > 0);

    Optional<Long> optionalValue = nameSequenceDao.get(sequenceId, user);

    if (!optionalValue.isPresent()) {
      nameSequenceDao.insert(sequenceId, count, user);
    } else {
      nameSequenceDao.update(sequenceId, optionalValue.get() + count, user);
    }

    return optionalValue.orElse(0L) + 1;
  }

}
