package fi.thl.termed.util.service;

import fi.thl.termed.domain.User;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MemoryBasedNamedSequence<K extends Serializable> implements NamedSequenceService<K> {

  private final Map<K, Long> sequences;

  public MemoryBasedNamedSequence() {
    this(new HashMap<>());
  }

  public MemoryBasedNamedSequence(Map<K, Long> sequences) {
    this.sequences = sequences;
  }

  @Override
  public Long get(K sequenceId, User user) {
    return sequences.computeIfAbsent(sequenceId, (seq) -> 0L);
  }

  @Override
  public Long getAndAdvance(K sequenceId, User user) {
    return getAndAdvance(sequenceId, 1L, user);
  }

  @Override
  public Long getAndAdvance(K sequenceId, Long count, User user) {
    Long value = get(sequenceId, user);
    sequences.compute(sequenceId, (seq, val) -> val + count);
    return value;
  }

  @Override
  public void set(K sequenceId, Long value, User user) {
    sequences.put(sequenceId, value);
  }

}
