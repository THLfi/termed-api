package fi.thl.termed.service.node.select;

import static com.google.common.collect.ImmutableList.of;
import static com.google.common.collect.Sets.union;
import static fi.thl.termed.util.UUIDs.lenientFromString;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import fi.thl.termed.domain.Attribute;
import fi.thl.termed.domain.ReferenceAttribute;
import fi.thl.termed.domain.ReferenceAttributeId;
import fi.thl.termed.domain.TextAttribute;
import fi.thl.termed.domain.Type;
import fi.thl.termed.domain.TypeId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import fi.thl.termed.util.query.AbstractSelectQualified;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Resolves and qualifies selects. For example: Select 'foo' -> Select 'GraphA.TypeA.foo',
 * 'GraphA.TypeB.foo'.
 */
public class SelectQualifier implements BiFunction<Set<Type>, Set<Select>, Set<Select>> {

  private final Map<TypeId, Type> allTypesById;
  private final Map<TypeId, List<ReferenceAttribute>> allReferenceAttributesByRange;

  SelectQualifier(List<Type> allTypes) {
    this.allTypesById = allTypes.stream().collect(toMap(Type::identifier, t -> t));
    this.allReferenceAttributesByRange = allTypes.stream()
        .flatMap(t -> t.getReferenceAttributes().stream())
        .collect(groupingBy(ReferenceAttribute::getRange));
  }

  @Override
  public Set<Select> apply(Set<Type> domainTypes, Set<Select> selects) {
    Tuple2<Set<AbstractSelectQualified>, Set<Select>> qualifiedAndNonQualified =
        toQualifiedAndNonQualified(expandSelectAll(selects));

    return ImmutableSet.<Select>builder()
        .addAll(typeQualifyRecursively(domainTypes, qualifiedAndNonQualified._1, 1))
        .addAll(qualifiedAndNonQualified._2)
        .build();
  }

  private Set<Select> expandSelectAll(Set<Select> selects) {
    return !selects.contains(new SelectAll())
        ? selects
        : ImmutableSet.<Select>builder()
            .addAll(selects)
            .add(new SelectAllProperties(), new SelectAllReferences(), new SelectAllReferrers())
            .build();
  }

  // split selects into two sets based on select type
  private Tuple2<Set<AbstractSelectQualified>, Set<Select>> toQualifiedAndNonQualified(
      Set<Select> selects) {
    return selects.stream().collect(
        () -> Tuple.of(new HashSet<>(), new HashSet<>()),
        (results, select) -> {
          if (select instanceof AbstractSelectQualified) {
            results._1.add((AbstractSelectQualified) select);
          } else {
            results._2.add(select);
          }
        },
        (left, right) -> {
          left._1.addAll(right._1);
          left._2.addAll(right._2);
        }
    );
  }

  private Set<AbstractSelectTypeQualified> typeQualifyRecursively(
      Set<Type> types, Set<AbstractSelectQualified> selects, int depth) {

    Preconditions.checkArgument(selects.stream()
            .filter(s -> s instanceof SelectWithDepth)
            .map(s -> (SelectWithDepth) s)
            .allMatch(s -> s.getDepth() >= depth),
        "All depth limited selects should have depth >= current depth.");

    if (types.isEmpty()) {
      return ImmutableSet.of();
    }

    return union(
        typeQualify(types, selects),
        typeQualifyRecursively(
            nextDomainTypes(types, selects),
            nextSelects(selects, depth),
            depth + 1));
  }

  private Set<AbstractSelectTypeQualified> typeQualify(Set<Type> types,
      Set<AbstractSelectQualified> selects) {
    return selects.stream()
        .flatMap(select -> {
          if (select instanceof SelectAllProperties) {
            SelectAllProperties s = (SelectAllProperties) select;
            return textAttributes(types.stream())
                .filter(attributeDomainFilteringPredicate(s.getQualifier()))
                .map(a -> new SelectTypeQualifiedProperty(a.identifier()));
          }
          if (select instanceof SelectProperty) {
            SelectProperty s = (SelectProperty) select;
            return textAttributes(types.stream())
                .filter(attributeDomainFilteringPredicate(s.getQualifier(), s.getField()))
                .map(a -> new SelectTypeQualifiedProperty(a.identifier()));
          }
          if (select instanceof SelectAllReferences) {
            SelectAllReferences s = (SelectAllReferences) select;
            return referenceAttributes(types.stream())
                .filter(attributeDomainFilteringPredicate(s.getQualifier()))
                .map(a -> new SelectTypeQualifiedReference(a.identifier()));
          }
          if (select instanceof SelectAllReferrers) {
            SelectAllReferrers s = (SelectAllReferrers) select;
            return referringAttributes(types.stream())
                .filter(attributeRangeFilteringPredicate(s.getQualifier()))
                .map(a -> new SelectTypeQualifiedReferrer(
                    new ReferenceAttributeId(a.getRange(), a.getId())));
          }
          if (select instanceof SelectReference) {
            SelectReference s = (SelectReference) select;
            return referenceAttributes(types.stream())
                .filter(attributeDomainFilteringPredicate(s.getQualifier(), s.getField()))
                .map(a -> new SelectTypeQualifiedReference(a.identifier(), s.getDepth()));
          }
          if (select instanceof SelectReferrer) {
            SelectReferrer s = (SelectReferrer) select;
            return referringAttributes(types.stream())
                .filter(attributeRangeFilteringPredicate(s.getQualifier(), s.getField()))
                .map(a -> new SelectTypeQualifiedReferrer(
                    new ReferenceAttributeId(a.getRange(), a.getId()), s.getDepth()));
          }

          throw new IllegalStateException("Unexpected select: " + select);
        })
        .collect(toSet());
  }

  // find next domain types for current types' reference/referrer target types filtered with selects
  private Set<Type> nextDomainTypes(Set<Type> types, Set<AbstractSelectQualified> selects) {
    return selects.stream()
        .flatMap(select -> {
          // selecting properties does not result into selecting new domains
          if (select instanceof SelectAllProperties) {
            return Stream.empty();
          }
          if (select instanceof SelectProperty) {
            return Stream.empty();
          }
          if (select instanceof SelectAllReferences) {
            SelectAllReferences s = (SelectAllReferences) select;
            return attributeRangeTypes(referenceAttributes(types.stream())
                .filter(attributeDomainFilteringPredicate(s.getQualifier())));
          }
          if (select instanceof SelectAllReferrers) {
            SelectAllReferrers s = (SelectAllReferrers) select;
            return attributeDomainTypes(referringAttributes(types.stream())
                .filter(attributeRangeFilteringPredicate(s.getQualifier())));
          }
          if (select instanceof SelectReference) {
            SelectReference s = (SelectReference) select;
            return attributeRangeTypes(referenceAttributes(types.stream())
                .filter(attributeDomainFilteringPredicate(s.getQualifier(), s.getField())));
          }
          if (select instanceof SelectReferrer) {
            SelectReferrer s = (SelectReferrer) select;
            return attributeDomainTypes(referringAttributes(types.stream())
                .filter(attributeRangeFilteringPredicate(s.getQualifier(), s.getField())));
          }

          throw new IllegalStateException("Unexpected select: " + select);
        })
        .collect(Collectors.toSet());
  }

  // find next selects by filtering current selects by depth
  private Set<AbstractSelectQualified> nextSelects(Set<AbstractSelectQualified> selects,
      int depth) {
    return selects.stream()
        .filter(select -> {
          // property selects are valid on each depth
          if (select instanceof SelectAllProperties) {
            return true;
          }
          if (select instanceof SelectProperty) {
            return true;
          }
          // select all references/referrers are applicable only once at root level
          if (select instanceof SelectAllReferences) {
            return false;
          }
          if (select instanceof SelectAllReferrers) {
            return false;
          }
          // single selects on references/referrers are depth limited
          if (select instanceof SelectReference) {
            SelectReference selectRef = (SelectReference) select;
            return selectRef.getDepth() >= (depth + 1);
          }
          if (select instanceof SelectReferrer) {
            SelectReferrer selectRef = (SelectReferrer) select;
            return selectRef.getDepth() >= (depth + 1);
          }

          throw new IllegalStateException("Unexpected select: " + select);
        })
        .collect(Collectors.toSet());
  }

  private Predicate<? super Attribute> attributeDomainFilteringPredicate(String qualifier) {
    String[] qualifierParts = qualifier.split("\\.");

    return attribute -> {
      if (qualifier.isEmpty()) {
        return true;
      }

      if (qualifierParts.length == 1) {
        return Objects.equals(attribute.getDomainId(), qualifierParts[0]);
      }

      if (qualifierParts.length == 2) {
        return Objects.equals(attribute.getDomainGraphId(), lenientFromString(qualifierParts[0]))
            && Objects.equals(attribute.getDomainId(), qualifierParts[1]);
      }

      return false;
    };
  }

  private Predicate<? super Attribute> attributeDomainFilteringPredicate(String qualifier,
      String field) {
    String[] qualifierParts = qualifier.split("\\.");

    return attribute -> {
      if (qualifier.isEmpty()) {
        return Objects.equals(attribute.getId(), field);
      }

      if (qualifierParts.length == 1) {
        return Objects.equals(attribute.getDomainId(), qualifierParts[0])
            && Objects.equals(attribute.getId(), field);
      }

      if (qualifierParts.length == 2) {
        return Objects.equals(attribute.getDomainGraphId(), lenientFromString(qualifierParts[0]))
            && Objects.equals(attribute.getDomainId(), qualifierParts[1])
            && Objects.equals(attribute.getId(), field);
      }

      return false;
    };
  }

  private Predicate<ReferenceAttribute> attributeRangeFilteringPredicate(String qualifier) {
    String[] qualifierParts = qualifier.split("\\.");

    return attribute -> {
      if (qualifier.isEmpty()) {
        return true;
      }

      if (qualifierParts.length == 1) {
        return Objects.equals(attribute.getRangeId(), qualifierParts[0]);
      }

      if (qualifierParts.length == 2) {
        return Objects.equals(attribute.getRangeGraphId(), lenientFromString(qualifierParts[0]))
            && Objects.equals(attribute.getRangeId(), qualifierParts[1]);
      }

      return false;
    };
  }

  private Predicate<ReferenceAttribute> attributeRangeFilteringPredicate(String qualifier,
      String field) {
    String[] qualifierParts = qualifier.split("\\.");

    return attribute -> {
      if (qualifier.isEmpty()) {
        return Objects.equals(attribute.getId(), field);
      }

      if (qualifierParts.length == 1) {
        return Objects.equals(attribute.getRangeId(), qualifierParts[0])
            && Objects.equals(attribute.getId(), field);
      }

      if (qualifierParts.length == 2) {
        return Objects.equals(attribute.getRangeGraphId(), lenientFromString(qualifierParts[0]))
            && Objects.equals(attribute.getRangeId(), qualifierParts[1])
            && Objects.equals(attribute.getId(), field);
      }

      return false;
    };
  }

  private Stream<TextAttribute> textAttributes(Stream<Type> types) {
    return types
        .flatMap(t -> t.getTextAttributes().stream());
  }

  private Stream<ReferenceAttribute> referenceAttributes(Stream<Type> types) {
    return types
        .flatMap(t -> t.getReferenceAttributes().stream());
  }

  private Stream<ReferenceAttribute> referringAttributes(Stream<Type> types) {
    return types
        .map(Type::identifier)
        .flatMap(id -> allReferenceAttributesByRange.getOrDefault(id, of()).stream());
  }

  private Stream<Type> attributeRangeTypes(Stream<ReferenceAttribute> refAttrs) {
    return refAttrs
        .map(ReferenceAttribute::getRange)
        .map(allTypesById::get);
  }

  private Stream<Type> attributeDomainTypes(Stream<ReferenceAttribute> refAttrs) {
    return refAttrs
        .map(ReferenceAttribute::getDomain)
        .map(allTypesById::get);
  }

}
