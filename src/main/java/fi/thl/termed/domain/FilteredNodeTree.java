package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.transformValues;

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
 * Removes id, type if they are not selected. Also empty properties and references are nullified
 * (i.e. none of them is selected). Other fields are selected on previous layers.
 */
public final class FilteredNodeTree extends ForwardingNodeTree {

  private final boolean selectId;
  private final boolean selectType;

  private final boolean selectProps;
  private final boolean selectRefs;
  private final boolean selectReferrers;

  public FilteredNodeTree(NodeTree source, Set<Select> selects) {
    super(source);
    this.selectId = selects.contains(new SelectId()) || selects.contains(new SelectAll());
    this.selectType = selects.contains(new SelectType()) || selects.contains(new SelectAll());
    this.selectProps = selects.stream().anyMatch(s -> s instanceof SelectTypeQualifiedProperty);
    this.selectRefs = selects.stream().anyMatch(s -> s instanceof SelectTypeQualifiedReference);
    this.selectReferrers = selects.stream().anyMatch(s -> s instanceof SelectTypeQualifiedReferrer);
  }

  private FilteredNodeTree(NodeTree delegate,
      boolean selectId,
      boolean selectType,
      boolean selectProps,
      boolean selectRefs,
      boolean selectReferrers) {
    super(delegate);
    this.selectId = selectId;
    this.selectType = selectType;
    this.selectProps = selectProps;
    this.selectRefs = selectRefs;
    this.selectReferrers = selectReferrers;
  }

  @Override
  public UUID getId() {
    return selectId ? super.getId() : null;
  }

  @Override
  public TypeId getType() {
    return selectType ? super.getType() : null;
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    return selectProps ? super.getProperties() : null;
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferences() {
    return selectRefs ? transformValues(super.getReferences(),
        r -> new FilteredNodeTree(r, selectId, selectType,
            selectProps, true, selectReferrers)) : null;
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    return selectReferrers ? transformValues(super.getReferrers(),
        r -> new FilteredNodeTree(r, selectId, selectType,
            selectProps, selectRefs, true)) : null;
  }

}
