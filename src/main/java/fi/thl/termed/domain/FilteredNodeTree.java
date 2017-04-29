package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.filterKeys;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class FilteredNodeTree implements NodeTree {

  private NodeTree source;

  private Set<String> selectedTextAttributes;
  private Set<String> selectedReferenceAttributes;
  private Set<String> selectedReferrerAttributes;

  public FilteredNodeTree(NodeTree source,
      Set<String> selectedTextAttributes,
      Set<String> selectedReferenceAttributes,
      Set<String> selectedReferrerAttributes) {
    this.source = source;
    this.selectedTextAttributes = selectedTextAttributes;
    this.selectedReferenceAttributes = selectedReferenceAttributes;
    this.selectedReferrerAttributes = selectedReferrerAttributes;
  }

  @Override
  public UUID getId() {
    return source.getId();
  }

  @Override
  public String getCode() {
    return source.getCode();
  }

  @Override
  public String getUri() {
    return source.getUri();
  }

  @Override
  public String getCreatedBy() {
    return source.getCreatedBy();
  }

  @Override
  public Date getCreatedDate() {
    return source.getCreatedDate();
  }

  @Override
  public String getLastModifiedBy() {
    return source.getLastModifiedBy();
  }

  @Override
  public Date getLastModifiedDate() {
    return source.getLastModifiedDate();
  }

  @Override
  public TypeId getType() {
    return source.getType();
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    return filterKeys(source.getProperties(), selectedTextAttributes::contains);
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferences() {
    return transformValues(
        filterKeys(source.getReferences(), selectedReferenceAttributes::contains),
        filtered -> new FilteredNodeTree(filtered,
            selectedTextAttributes,
            selectedReferenceAttributes,
            selectedReferrerAttributes));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    return transformValues(
        filterKeys(source.getReferrers(), selectedReferrerAttributes::contains),
        filtered -> new FilteredNodeTree(filtered,
            selectedTextAttributes,
            selectedReferenceAttributes,
            selectedReferrerAttributes));
  }

}
