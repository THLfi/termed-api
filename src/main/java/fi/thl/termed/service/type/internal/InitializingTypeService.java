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
  public List<TypeId> save(List<Type> types, Map<String, Object> args, User user) {
    types.forEach(this::initialize);
    return super.save(types, args, user);
  }

  @Override
  public TypeId save(Type cls, Map<String, Object> args, User user) {
    initialize(cls);
    return super.save(cls, args, user);
  }

  @Override
  public List<TypeId> deleteAndSave(List<TypeId> deletes, List<Type> saves,
      Map<String, Object> args, User user) {
    saves.forEach(this::initialize);
    return super.deleteAndSave(deletes, saves, args, user);
  }

  private void initialize(Type type) {
    for (TextAttribute textAttribute : type.getTextAttributes()) {
      textAttribute.setDomain(new TypeId(type));
      textAttribute.setRegex(firstNonNull(textAttribute.getRegex(), RegularExpressions.ALL));
    }

    for (ReferenceAttribute referenceAttribute : type.getReferenceAttributes()) {
      referenceAttribute.setDomain(new TypeId(type));
      referenceAttribute.setRange(new TypeId(
          firstNonNull(referenceAttribute.getRangeId(), type.getId()),
          firstNonNull(referenceAttribute.getRangeGraphId(), type.getGraphId())));
    }
  }

}
