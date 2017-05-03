package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.filterKeys;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.Multimap;
import fi.thl.termed.service.node.select.Select;
import fi.thl.termed.service.node.select.SelectAll;
import fi.thl.termed.service.node.select.SelectAllProperties;
import fi.thl.termed.service.node.select.SelectAllReferences;
import fi.thl.termed.service.node.select.SelectAllReferrers;
import fi.thl.termed.service.node.select.SelectCode;
import fi.thl.termed.service.node.select.SelectCreatedBy;
import fi.thl.termed.service.node.select.SelectCreatedDate;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectLastModifiedBy;
import fi.thl.termed.service.node.select.SelectLastModifiedDate;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.service.node.select.SelectReference;
import fi.thl.termed.service.node.select.SelectReferrer;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.service.node.select.SelectUri;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class FilteredNodeTree implements NodeTree {

  private NodeTree source;
  private Set<Select> s;

  public FilteredNodeTree(NodeTree source, Set<Select> selects) {
    this.source = source;
    this.s = selects;
  }

  @Override
  public UUID getId() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectId.INSTANCE) ?
        source.getId() : null;
  }

  @Override
  public String getCode() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectCode.INSTANCE) ?
        source.getCode() : null;
  }

  @Override
  public String getUri() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectUri.INSTANCE) ?
        source.getUri() : null;
  }

  @Override
  public String getCreatedBy() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectCreatedBy.INSTANCE) ?
        source.getCreatedBy() : null;
  }

  @Override
  public Date getCreatedDate() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectCreatedDate.INSTANCE) ?
        source.getCreatedDate() : null;
  }

  @Override
  public String getLastModifiedBy() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectLastModifiedBy.INSTANCE) ?
        source.getLastModifiedBy() : null;
  }

  @Override
  public Date getLastModifiedDate() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectLastModifiedDate.INSTANCE) ?
        source.getLastModifiedDate() : null;
  }

  @Override
  public TypeId getType() {
    return s.contains(SelectAll.INSTANCE) || s.contains(SelectType.INSTANCE) ?
        source.getType() : null;
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    if (s.contains(SelectAll.INSTANCE) || s.contains(SelectAllProperties.INSTANCE)) {
      return source.getProperties();
    }

    return filterKeys(source.getProperties(), key -> s.contains(new SelectProperty(key)));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferences() {
    if (s.contains(SelectAll.INSTANCE) || s.contains(SelectAllReferences.INSTANCE)) {
      return source.getReferences();
    }

    return transformValues(
        filterKeys(source.getReferences(), key -> s.contains(new SelectReference(key))),
        reference -> new FilteredNodeTree(reference, s));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    if (s.contains(SelectAll.INSTANCE) || s.contains(SelectAllReferrers.INSTANCE)) {
      return source.getReferrers();
    }

    return transformValues(
        filterKeys(source.getReferrers(), key -> s.contains(new SelectReferrer(key))),
        referrer -> new FilteredNodeTree(referrer, s));
  }

}
