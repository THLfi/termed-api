package fi.thl.termed.util.service;

import static org.junit.Assert.assertEquals;

import fi.thl.termed.domain.User;
import org.junit.Test;

public class CachedNamedSequenceServiceTest {

  @Test
  public void shouldCacheSequence() {
    String seqName = "A";
    User user = User.newUser("example");

    NamedSequenceService<String> actualSeq = new MemoryBasedNamedSequence<>();
    CachedNamedSequenceService<String> cachedSeq = new CachedNamedSequenceService<>(actualSeq, 3);

    assertEquals((Long) 0L, cachedSeq.get(seqName, user));
    assertEquals((Long) 0L, actualSeq.get(seqName, user));

    assertEquals((Long) 0L, cachedSeq.getAndAdvance(seqName, user));
    assertEquals((Long) 1L, cachedSeq.get(seqName, user));
    assertEquals((Long) 3L, actualSeq.get(seqName, user));

    assertEquals((Long) 1L, cachedSeq.getAndAdvance(seqName, user));
    assertEquals((Long) 3L, actualSeq.get(seqName, user));

    assertEquals((Long) 2L, cachedSeq.getAndAdvance(seqName, user));
    assertEquals((Long) 3L, actualSeq.get(seqName, user));

    assertEquals((Long) 3L, cachedSeq.getAndAdvance(seqName, user));
    assertEquals((Long) 6L, actualSeq.get(seqName, user));

    cachedSeq.close();

    assertEquals((Long) 4L, cachedSeq.get(seqName, user));
    assertEquals((Long) 4L, actualSeq.get(seqName, user));
  }

}
