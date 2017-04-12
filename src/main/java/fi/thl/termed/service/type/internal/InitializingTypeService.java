package fi.thl.termed.service.type.internal;

import static com.google.common.base.MoreObjects.firstNonNull;

import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.Service;
import java.util.List;
import java.util.Map;

/**
 * Set some default values for attributes
 */
public class InitializingTypeService extends ForwardingService<TypeId, Type> {

  public InitializingTypeService(Service<TypeId, Type> delegate) {
    super(delegate);
  }

  @Override
  public List<TypeId> save(List<Type> types, Map<String, Object> args, User currentUser) {
    types.forEach(this::initialize);
    return super.save(types, args, currentUser);
  }

  @Override
  public TypeId save(Type cls, Map<String, Object> args, User currentUser) {
    initialize(cls);
    return super.save(cls, args, currentUser);
  }

  private void initialize(Type cls) {
    for (TextAttribute textAttribute : cls.getTextAttributes()) {
      textAttribute.setDomain(new TypeId(cls));
      textAttribute.setRegex(firstNonNull(textAttribute.getRegex(), RegularExpressions.ALL));
    }

    for (ReferenceAttribute referenceAttribute : cls.getReferenceAttributes()) {
      referenceAttribute.setDomain(new TypeId(cls));

      if (referenceAttribute.getRange() == null) {
        referenceAttribute.setRange(new TypeId(cls));
      }
      if (referenceAttribute.getRange().getGraph() == null) {
        referenceAttribute.setRange(
            new TypeId(referenceAttribute.getRangeId(), cls.getGraph()));
      }
    }
  }

}
