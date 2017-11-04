package fi.thl.termed.service.type.internal;

import static com.google.common.base.MoreObjects.firstNonNull;
import static fi.thl.termed.util.collect.DequeUtils.addFirst;
import static fi.thl.termed.util.collect.DequeUtils.newArrayDeque;
import static fi.thl.termed.util.collect.StreamUtils.zipWithIndex;
import static java.util.stream.Collectors.toList;

import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.domain.User;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.service.ForwardingService;
import fi.thl.termed.util.service.SaveMode;
import fi.thl.termed.util.service.Service;
import fi.thl.termed.util.service.WriteOptions;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Set default values for types and attributes: ascending indices, range and regex
 */
public class InitializingTypeService extends ForwardingService<TypeId, Type> {

  public InitializingTypeService(Service<TypeId, Type> delegate) {
    super(delegate);
  }

  @Override
  public List<TypeId> save(List<Type> types, SaveMode mode, WriteOptions opts, User user) {
    return super.save(init(types), mode, opts, user);
  }

  @Override
  public TypeId save(Type type, SaveMode mode, WriteOptions opts, User user) {
    return super.save(init(type, type.getIndex().orElse(0)), mode, opts, user);
  }

  @Override
  public List<TypeId> saveAndDelete(List<Type> saves, List<TypeId> deletes, SaveMode mode,
      WriteOptions opts, User user) {
    return super.saveAndDelete(init(saves), deletes, mode, opts, user);
  }

  private List<Type> init(List<Type> types) {
    return zipWithIndex(types.stream(), this::init).collect(toList());
  }

  private Type init(Type type, int index) {
    return Type.builderFromCopyOf(type)
        .index(index)
        .textAttributes(initTextAttrs(type.identifier(), type.getTextAttributes()))
        .referenceAttributes(initRefAttrs(type.identifier(), type.getReferenceAttributes()))
        .build();
  }

  private List<TextAttribute> initTextAttrs(TypeId domain, List<TextAttribute> attributesList) {
    if (attributesList.isEmpty()) {
      return attributesList;
    }
    Deque<TextAttribute> attributes = new ArrayDeque<>(attributesList);
    return new ArrayList<>(initTextAttrs(domain, -1, attributes.removeFirst(), attributes));
  }

  // ensure that indices are ascending, add default regex if missing
  private Deque<TextAttribute> initTextAttrs(TypeId domain, int previousIndex,
      TextAttribute attribute, Deque<TextAttribute> rest) {

    int index = attribute.getIndex().orElse(0);
    int ascendingIndex = index > previousIndex ? index : previousIndex + 1;
    String regex = firstNonNull(attribute.getRegex(), RegularExpressions.ALL);

    TextAttribute result = TextAttribute.builder()
        .id(attribute.getId(), domain).regex(regex)
        .copyOptionalsFrom(attribute)
        .index(ascendingIndex)
        .build();

    return rest.isEmpty() ? newArrayDeque(result)
        : addFirst(result, initTextAttrs(domain, ascendingIndex, rest.removeFirst(), rest));
  }

  private List<ReferenceAttribute> initRefAttrs(TypeId domain,
      List<ReferenceAttribute> attributesList) {
    if (attributesList.isEmpty()) {
      return attributesList;
    }
    Deque<ReferenceAttribute> attributes = new ArrayDeque<>(attributesList);
    return new ArrayList<>(initRefAttrs(domain, -1, attributes.removeFirst(), attributes));
  }

  // ensure that indices are ascending, add range if missing
  private Deque<ReferenceAttribute> initRefAttrs(TypeId domain, int previousIndex,
      ReferenceAttribute attribute, Deque<ReferenceAttribute> rest) {

    int index = attribute.getIndex().orElse(0);
    int ascendingIndex = index > previousIndex ? index : previousIndex + 1;
    TypeId range = TypeId.of(
        firstNonNull(attribute.getRangeId(), domain.getId()),
        firstNonNull(attribute.getRangeGraphId(), domain.getGraphId()));

    ReferenceAttribute result = ReferenceAttribute.builder()
        .id(attribute.getId(), domain).range(range)
        .copyOptionalsFrom(attribute)
        .index(ascendingIndex)
        .build();

    return rest.isEmpty() ? newArrayDeque(result)
        : addFirst(result, initRefAttrs(domain, ascendingIndex, rest.removeFirst(), rest));
  }

}
