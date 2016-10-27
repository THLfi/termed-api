package fi.thl.termed.service.class_.internal;

import java.util.List;

import fi.thl.termed.domain.Class;
import fi.thl.termed.domain.ClassId;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Set some default values for attributes
 */
public class InitializingClassService extends ForwardingService<ClassId, Class> {

  public InitializingClassService(Service<ClassId, Class> delegate) {
    super(delegate);
  }

  @Override
  public List<ClassId> save(List<Class> classes, User currentUser) {
    classes.forEach(this::initialize);
    return super.save(classes, currentUser);
  }

  @Override
  public ClassId save(Class cls, User currentUser) {
    initialize(cls);
    return super.save(cls, currentUser);
  }

  private void initialize(Class cls) {
    for (TextAttribute textAttribute : cls.getTextAttributes()) {
      textAttribute.setDomain(new ClassId(cls));
      textAttribute.setRegex(firstNonNull(textAttribute.getRegex(), RegularExpressions.ALL));
    }

    for (ReferenceAttribute referenceAttribute : cls.getReferenceAttributes()) {
      referenceAttribute.setDomain(new ClassId(cls));

      if (referenceAttribute.getRange() == null) {
        referenceAttribute.setRange(new ClassId(cls));
      }
      if (referenceAttribute.getRange().getScheme() == null) {
        referenceAttribute.setRange(
            new ClassId(referenceAttribute.getRangeId(), cls.getScheme()));
      }
    }
  }

}
