package fi.thl.termed.service.node.select;

import static com.google.common.collect.ImmutableList.of;
import static fi.thl.termed.util.UUIDs.lenientFromString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.google.common.collect.ImmutableList;
import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.collect.TriFunction;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SelectQualifier implements TriFunction<List<Type>, List<Type>, List<Select>, List<Select>> {

  private static final Logger log = LoggerFactory.getLogger(SelectQualifier.class);

  /**
   * Add full type information to select clauses. This means that one non-qualified select can be
   * mapped to multiple selects. For example: select 'foo' -> qualified select 'TypeA.foo',
   * 'TypeB.foo'.
   */
  public List<Select> apply(List<Type> allTypes, List<Type> domainTypes, List<Select> selects) {
    log.trace("Qualify: {}", selects);

    Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange = allTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .collect(groupingBy(ReferenceAttribute::getRange));

    List<Type> transitiveDomainTypes = findTransitiveDomainTypes(allTypes, domainTypes, selects);

    Set<Select> qualified = new HashSet<>();

    // add specific selects
    qualified.addAll(selects.stream()
        .flatMap(s -> {
          if (s instanceof SelectProperty) {
            return qualifyProperty(transitiveDomainTypes, (SelectProperty) s);
          }
          if (s instanceof SelectReference) {
            return qualifyReference(transitiveDomainTypes, (SelectReference) s);
          }
          if (s instanceof SelectReferrer) {
            return qualifyReferrer(transitiveDomainTypes, (SelectReferrer) s);
          }
          return Stream.of(s);
        })
        .distinct()
        .collect(ImmutableList.toImmutableList()));

    // process catch-all selects after specific so they don't
    // overwrite specific ref selects with depths
    qualified.addAll(selects.stream()
        .flatMap(s -> {
          if (s instanceof SelectAll) {
            return qualifyAll(transitiveDomainTypes, allReferenceAttributesByRange);
          }
          if (s instanceof SelectAllProperties) {
            return qualifyAllProperties(transitiveDomainTypes);
          }
          if (s instanceof SelectAllReferences) {
            return qualifyAllReferences(transitiveDomainTypes);
          }
          if (s instanceof SelectAllReferrers) {
            return qualifyAllReferrers(transitiveDomainTypes, allReferenceAttributesByRange);
          }

          return Stream.of(s);
        })
        .distinct()
        .collect(ImmutableList.toImmutableList()));

    log.trace("Qualified to: {}", qualified);

    return new ArrayList<>(qualified);
  }

  private List<Type> findTransitiveDomainTypes(List<Type> allTypes, List<Type> domainTypes,
      List<Select> selects) {

    log.trace("Domain types: {}", domainTypes.stream().map(Type::getId).collect(toList()));

    Map<TypeId, Type> allTypesById = allTypes.stream().collect(toMap(Type::identifier, t -> t));
    Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange = allTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .collect(groupingBy(ReferenceAttribute::getRange));

    Set<Type> transitiveDomainTypes = new TreeSet<>(
        Comparator.comparing(Type::getGraphId).thenComparing(Type::getId));
    transitiveDomainTypes.addAll(domainTypes);

    // add referenced types to transitiveDomainTypes if select all or select all references
    if (selects.stream().anyMatch(
        s -> s instanceof SelectAll || s instanceof SelectAllReferences)) {
      domainTypes.stream()
          .flatMap(t -> t.getReferenceAttributes().stream())
          .map(ReferenceAttribute::getRange)
          .map(allTypesById::get)
          .forEach(transitiveDomainTypes::add);
    }

    // add referring types to transitiveDomainTypes if select all or select all referrers
    if (selects.stream().anyMatch(
        s -> s instanceof SelectAll || s instanceof SelectAllReferrers)) {
      domainTypes.stream()
          .flatMap(t -> allReferenceAttributesByRange.getOrDefault(t.identifier(), of()).stream())
          .map(Attribute::getDomain)
          .map(allTypesById::get)
          .forEach(transitiveDomainTypes::add);
    }

    log.trace("Directly referenced domain types: {}",
        transitiveDomainTypes.stream().map(Type::getId).collect(toList()));

    transitiveDomainTypes.addAll(
        findTransitiveReferencedRangeTypes(allTypesById, transitiveDomainTypes, selects.stream()
            .filter(s -> s instanceof SelectReference)
            .map(s -> (SelectReference) s)
            .collect(toList())));

    transitiveDomainTypes.addAll(
        findTransitiveReferredDomainTypes(allTypesById, allReferenceAttributesByRange,
            transitiveDomainTypes, selects.stream()
                .filter(s -> s instanceof SelectReferrer)
                .map(s -> (SelectReferrer) s)
                .collect(toList())));

    log.trace("Transitive domain types: {}", transitiveDomainTypes.stream()
        .map(Type::getId).collect(toList()));

    return new ArrayList<>(transitiveDomainTypes);
  }

  private Set<Type> findTransitiveReferencedRangeTypes(
      Map<TypeId, Type> allTypesById,
      Set<Type> domainTypes,
      List<SelectReference> selectedReferences) {

    Set<Type> transitiveDomainTypes = new HashSet<>(domainTypes);
    Set<Type> sourceTypes = domainTypes;

    while (true) {
      Set<Type> newTypes = findReferencedRangeTypes(
          allTypesById, sourceTypes, selectedReferences);

      if (transitiveDomainTypes.containsAll(newTypes)) {
        break;
      } else {
        transitiveDomainTypes.addAll(newTypes);
        sourceTypes = newTypes;
      }
    }

    return transitiveDomainTypes;
  }

  private Set<Type> findReferencedRangeTypes(
      Map<TypeId, Type> allTypesById,
      Collection<Type> domainTypes,
      Collection<SelectReference> selectedReferences) {

    return selectedReferences.stream()
        .flatMap(selectReference -> {
          Stream<ReferenceAttribute> allPossibleReferencingAttributes = domainTypes.stream()
              .flatMap(t -> t.getReferenceAttributes().stream());

          /*
          log.trace("Finding new range types for {}.{} from {}",
              selectReference.getQualifier(),
              selectReference.getField(),
              domainTypes.stream()
                  .flatMap(t -> t.getReferenceAttributes().stream())
                  .map(a -> a.getDomainId() + "." + a.getId() + " -> " + a.getRangeId())
                  .distinct()
                  .sorted()
                  .collect(toList()));
           */

          if (selectReference.getQualifier().isEmpty()) {
            return allPossibleReferencingAttributes
                .filter(a -> Objects.equals(a.getId(), selectReference.getField()))
                .map(ReferenceAttribute::getRange);
          }

          String[] parts = selectReference.getQualifier().split("\\.");

          if (parts.length == 1) {
            return allPossibleReferencingAttributes
                .filter(a -> Objects.equals(a.getDomainId(), parts[0]))
                .filter(a -> Objects.equals(a.getId(), selectReference.getField()))
                .map(ReferenceAttribute::getRange);
          }

          if (parts.length == 2) {
            return allPossibleReferencingAttributes
                .filter(a -> Objects.equals(a.getDomainGraphId(), lenientFromString(parts[0])))
                .filter(a -> Objects.equals(a.getDomainId(), parts[1]))
                .filter(a -> Objects.equals(a.getId(), selectReference.getField()))
                .map(ReferenceAttribute::getRange);
          }

          return Stream.empty();
        })
        .map(allTypesById::get)
        .collect(Collectors.toSet());
  }

  private Set<Type> findTransitiveReferredDomainTypes(
      Map<TypeId, Type> allTypesById,
      Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange,
      Set<Type> domainTypes,
      List<SelectReferrer> selectedReferrers) {

    Set<Type> transitiveDomainTypes = new HashSet<>(domainTypes);
    Set<Type> sourceTypes = domainTypes;

    while (true) {
      Set<Type> newTypes = findReferredDomainTypes(
          allTypesById, allReferenceAttributesByRange, sourceTypes, selectedReferrers);

      if (transitiveDomainTypes.containsAll(newTypes)) {
        break;
      } else {
        transitiveDomainTypes.addAll(newTypes);
        sourceTypes = newTypes;
      }
    }

    return transitiveDomainTypes;
  }

  private Set<Type> findReferredDomainTypes(
      Map<TypeId, Type> allTypesById,
      Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange,
      Collection<Type> domainTypes,
      Collection<SelectReferrer> selectedReferrers) {

    return selectedReferrers.stream()
        .flatMap(selectReferrer -> {
          Stream<ReferenceAttribute> allPossibleReferringAttributes = domainTypes.stream()
              .flatMap(t -> allReferenceAttributesByRange.getOrDefault(t.identifier(), of())
                  .stream());

          if (selectReferrer.getQualifier().isEmpty()) {
            return allPossibleReferringAttributes
                .filter(a -> Objects.equals(a.getId(), selectReferrer.getField()))
                .map(ReferenceAttribute::getDomain);
          }

          String[] parts = selectReferrer.getQualifier().split("\\.");

          if (parts.length == 1) {
            return allPossibleReferringAttributes
                .filter(a -> Objects.equals(a.getRangeId(), parts[0]))
                .filter(a -> Objects.equals(a.getId(), selectReferrer.getField()))
                .map(ReferenceAttribute::getDomain);
          }

          if (parts.length == 2) {
            return allPossibleReferringAttributes
                .filter(a -> Objects.equals(a.getRangeGraphId(), lenientFromString(parts[0])))
                .filter(a -> Objects.equals(a.getRangeId(), parts[1]))
                .filter(a -> Objects.equals(a.getId(), selectReferrer.getField()))
                .map(ReferenceAttribute::getDomain);
          }

          return Stream.empty();
        })
        .map(allTypesById::get)
        .collect(Collectors.toSet());
  }

  private Stream<AbstractSelectTypeQualified> qualifyAll(List<Type> possibleTypes,
      Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange) {
    return Stream.of(
        qualifyAllProperties(possibleTypes),
        qualifyAllReferences(possibleTypes),
        qualifyAllReferrers(possibleTypes, allReferenceAttributesByRange))
        .flatMap(s -> s);
  }

  private Stream<SelectTypeQualifiedProperty> qualifyAllProperties(
      List<Type> possibleTypes) {
    return possibleTypes.stream()
        .flatMap(t -> t.getTextAttributes().stream())
        .map(a -> new SelectTypeQualifiedProperty(a.identifier()));
  }

  private Stream<SelectTypeQualifiedReference> qualifyAllReferences(
      List<Type> possibleTypes) {
    return possibleTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .map(a -> new SelectTypeQualifiedReference(a.identifier()));
  }

  private Stream<SelectTypeQualifiedReferrer> qualifyAllReferrers(
      List<Type> possibleTypes,
      Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange) {

    return possibleTypes.stream()
        .flatMap(t -> allReferenceAttributesByRange.getOrDefault(t.identifier(), of()).stream())
        .map(a -> new SelectTypeQualifiedReferrer(
            new ReferenceAttributeId(a.getRange(), a.getId())));
  }

  private Stream<SelectTypeQualifiedProperty> qualifyProperty(
      List<Type> possibleTypes, SelectProperty selectProperty) {

    Stream<TextAttribute> possibleTextAttributes = possibleTypes.stream()
        .flatMap(t -> t.getTextAttributes().stream());

    if (selectProperty.getQualifier().isEmpty()) {
      return possibleTextAttributes
          .filter(a -> Objects.equals(a.getId(), selectProperty.getField()))
          .map(a -> new SelectTypeQualifiedProperty(a.identifier()));
    }

    String[] qualifierParts = selectProperty.getQualifier().split("\\.");

    if (qualifierParts.length == 1) {
      return possibleTextAttributes
          .filter(a -> Objects.equals(a.getDomainId(), qualifierParts[0]))
          .filter(a -> Objects.equals(a.getId(), selectProperty.getField()))
          .map(a -> new SelectTypeQualifiedProperty(a.identifier()));
    }

    if (qualifierParts.length == 2) {
      return possibleTextAttributes
          .filter(a -> Objects.equals(a.getDomainGraphId(), lenientFromString(qualifierParts[0])))
          .filter(a -> Objects.equals(a.getDomainId(), qualifierParts[1]))
          .filter(a -> Objects.equals(a.getId(), selectProperty.getField()))
          .map(a -> new SelectTypeQualifiedProperty(a.identifier()));
    }

    return Stream.empty();
  }

  private Stream<SelectTypeQualifiedReference> qualifyReference(
      List<Type> possibleTypes, SelectReference selectReference) {

    Stream<ReferenceAttribute> possibleRefAttributes = possibleTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream());

    int depth = selectReference.getDepth();

    if (selectReference.getQualifier().isEmpty()) {
      return possibleRefAttributes
          .filter(a -> Objects.equals(a.getId(), selectReference.getField()))
          .map(a -> new SelectTypeQualifiedReference(a.identifier(), depth));
    }

    String[] qualifierParts = selectReference.getQualifier().split("\\.");

    if (qualifierParts.length == 1) {
      return possibleRefAttributes
          .filter(a -> Objects.equals(a.getDomainId(), qualifierParts[0]))
          .filter(a -> Objects.equals(a.getId(), selectReference.getField()))
          .map(a -> new SelectTypeQualifiedReference(a.identifier(), depth));
    }

    if (qualifierParts.length == 2) {
      return possibleRefAttributes
          .filter(a -> Objects.equals(a.getDomainGraphId(), lenientFromString(qualifierParts[0])))
          .filter(a -> Objects.equals(a.getDomainId(), qualifierParts[1]))
          .filter(a -> Objects.equals(a.getId(), selectReference.getField()))
          .map(a -> new SelectTypeQualifiedReference(a.identifier(), depth));
    }

    return Stream.empty();
  }

  private Stream<SelectTypeQualifiedReferrer> qualifyReferrer(
      List<Type> possibleTypes, SelectReferrer selectReferrer) {

    Stream<ReferenceAttribute> possibleRefAttributes = possibleTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream());

    int depth = selectReferrer.getDepth();

    if (selectReferrer.getQualifier().isEmpty()) {
      return possibleRefAttributes
          .filter(a -> Objects.equals(a.getId(), selectReferrer.getField()))
          .map(a -> new SelectTypeQualifiedReferrer(
              new ReferenceAttributeId(a.getRange(), a.getId()), depth));
    }

    String[] qualifierParts = selectReferrer.getQualifier().split("\\.");

    if (qualifierParts.length == 1) {
      return possibleRefAttributes
          .filter(a -> Objects.equals(a.getRangeId(), qualifierParts[0]))
          .filter(a -> Objects.equals(a.getId(), selectReferrer.getField()))
          .map(a -> new SelectTypeQualifiedReferrer(
              new ReferenceAttributeId(a.getRange(), a.getId()), depth));
    }

    if (qualifierParts.length == 2) {
      return possibleRefAttributes
          .filter(a -> Objects.equals(a.getRangeGraphId(), lenientFromString(qualifierParts[0])))
          .filter(a -> Objects.equals(a.getRangeId(), qualifierParts[1]))
          .filter(a -> Objects.equals(a.getId(), selectReferrer.getField()))
          .map(a -> new SelectTypeQualifiedReferrer(
              new ReferenceAttributeId(a.getRange(), a.getId()), depth));
    }

    return Stream.empty();
  }

}
