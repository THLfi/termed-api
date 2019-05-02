package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.service.node.select.SelectTypeQualifiedProperty;
import fi.thl.termed.service.node.select.SelectTypeQualifiedReference;
import fi.thl.termed.service.node.select.SelectTypeQualifiedReferrer;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.util.Set;
import java.util.UUID;

/**
 * Removes id and type if they are not selected. Other fields are selected on previous layers.
 */
public final class FilteredNodeTree extends ForwardingNodeTree {

  private final ImmutableSet<Select> s;

  public FilteredNodeTree(NodeTree source, Set<Select> selects) {
    super(source);
    this.s = ImmutableSet.copyOf(selects);
  }

  @Override
  public UUID getId() {
    return s.contains(new SelectAll()) || s.contains(new SelectId()) ? super.getId() : null;
  }

  @Override
  public TypeId getType() {
    return s.contains(new SelectAll()) || s.contains(new SelectType()) ? super.getType() : null;
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    if (s.stream().noneMatch(select -> select instanceof SelectTypeQualifiedProperty)) {
      return null;
    }

    return super.getProperties();
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferences() {
    if (s.stream().noneMatch(select -> select instanceof SelectTypeQualifiedReference)) {
      return null;
    }

    return transformValues(super.getReferences(), r -> new FilteredNodeTree(r, s));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    if (s.stream().noneMatch(select -> select instanceof SelectTypeQualifiedReferrer)) {
      return null;
    }

    return transformValues(super.getReferrers(), r -> new FilteredNodeTree(r, s));
  }

}
