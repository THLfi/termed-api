package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.filterKeys;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import fi.thl.termed.service.node.select.SelectAllProperties;
import fi.thl.termed.service.node.select.SelectAllReferences;
import fi.thl.termed.service.node.select.SelectAllReferrers;
import fi.thl.termed.service.node.select.SelectCode;
import fi.thl.termed.service.node.select.SelectCreatedBy;
import fi.thl.termed.service.node.select.SelectCreatedDate;
import fi.thl.termed.service.node.select.SelectId;
import fi.thl.termed.service.node.select.SelectLastModifiedBy;
import fi.thl.termed.service.node.select.SelectLastModifiedDate;
import fi.thl.termed.service.node.select.SelectNumber;
import fi.thl.termed.service.node.select.SelectProperty;
import fi.thl.termed.service.node.select.SelectReference;
import fi.thl.termed.service.node.select.SelectReferrer;
import fi.thl.termed.service.node.select.SelectType;
import fi.thl.termed.service.node.select.SelectUri;
import fi.thl.termed.util.query.Select;
import fi.thl.termed.util.query.SelectAll;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class FilteredNodeTree implements NodeTree {

  private final NodeTree source;
  private final ImmutableSet<Select> s;

  public FilteredNodeTree(NodeTree source, Set<Select> selects) {
    this.source = source;
    this.s = ImmutableSet.copyOf(selects);
  }

  @Override
  public UUID getId() {
    return s.contains(new SelectAll()) || s.contains(new SelectId()) ? source.getId() : null;
  }

  @Override
  public String getCode() {
    return s.contains(new SelectAll()) || s.contains(new SelectCode()) ? source.getCode() : null;
  }

  @Override
  public String getUri() {
    return s.contains(new SelectAll()) || s.contains(new SelectUri()) ? source.getUri() : null;
  }

  @Override
  public Long getNumber() {
    return s.contains(new SelectAll()) || s.contains(new SelectNumber()) ?
        source.getNumber() : null;
  }

  @Override
  public String getCreatedBy() {
    return s.contains(new SelectAll()) || s.contains(new SelectCreatedBy()) ?
        source.getCreatedBy() : null;
  }

  @Override
  public Date getCreatedDate() {
    return s.contains(new SelectAll()) || s.contains(new SelectCreatedDate()) ?
        source.getCreatedDate() : null;
  }

  @Override
  public String getLastModifiedBy() {
    return s.contains(new SelectAll()) || s.contains(new SelectLastModifiedBy()) ?
        source.getLastModifiedBy() : null;
  }

  @Override
  public Date getLastModifiedDate() {
    return s.contains(new SelectAll()) || s.contains(new SelectLastModifiedDate()) ?
        source.getLastModifiedDate() : null;
  }

  @Override
  public TypeId getType() {
    return s.contains(new SelectAll()) || s.contains(new SelectType()) ? source.getType() : null;
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    if (s.contains(new SelectAll()) || s.contains(new SelectAllProperties())) {
      return source.getProperties();
    }

    return filterKeys(source.getProperties(), key -> s.contains(new SelectProperty(key)));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferences() {
    if (s.contains(new SelectAll()) || s.contains(new SelectAllReferences())) {
      return source.getReferences();
    }

    return transformValues(
        filterKeys(source.getReferences(), key -> s.contains(new SelectReference(key))),
        reference -> new FilteredNodeTree(reference, s));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    if (s.contains(new SelectAll()) || s.contains(new SelectAllReferrers())) {
      return source.getReferrers();
    }

    return transformValues(
        filterKeys(source.getReferrers(), key -> s.contains(new SelectReferrer(key))),
        referrer -> new FilteredNodeTree(referrer, s));
  }

}
