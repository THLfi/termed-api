package fi.thl.termed.domain;

import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class ForwardingNodeTree implements NodeTree {

  private NodeTree delegate;

  public ForwardingNodeTree(NodeTree delegate) {
    this.delegate = delegate;
  }

  @Override
  public UUID getId() {
    return delegate.getId();
  }

  @Override
  public Optional<String> getCode() {
    return delegate.getCode();
  }

  @Override
  public Optional<String> getUri() {
    return delegate.getUri();
  }

  @Override
  public Long getNumber() {
    return delegate.getNumber();
  }

  @Override
  public String getCreatedBy() {
    return delegate.getCreatedBy();
  }

  @Override
  public Date getCreatedDate() {
    return delegate.getCreatedDate();
  }

  @Override
  public String getLastModifiedBy() {
    return delegate.getLastModifiedBy();
  }

  @Override
  public Date getLastModifiedDate() {
    return delegate.getLastModifiedDate();
  }

  @Override
  public TypeId getType() {
    return delegate.getType();
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    return delegate.getProperties();
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferences() {
    return delegate.getReferences();
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    return delegate.getReferrers();
  }

}
