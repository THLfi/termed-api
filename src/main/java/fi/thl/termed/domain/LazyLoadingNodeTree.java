package fi.thl.termed.domain;

import static fi.thl.termed.util.collect.FunctionUtils.partialApply;

import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.LazyLoadingMultimap;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class LazyLoadingNodeTree implements NodeTree {

  private Node source;

  private BiFunction<Node, String, List<Node>> referenceProvider;
  private BiFunction<Node, String, List<Node>> referrerProvider;

  public LazyLoadingNodeTree(Node source,
      BiFunction<Node, String, List<Node>> referenceProvider,
      BiFunction<Node, String, List<Node>> referrerProvider) {
    this.source = source;
    this.referenceProvider = referenceProvider;
    this.referrerProvider = referrerProvider;
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
  public Long getNumber() {
    return source.getNumber();
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
    return source.getProperties();
  }

  @Override
  public Multimap<String, NodeTree> getReferences() {
    return new LazyLoadingMultimap<>(source.getReferences().keySet(),
        attrId -> partialApply(referenceProvider, source).apply(attrId).stream()
            .map(value -> new LazyLoadingNodeTree(value, referenceProvider, referrerProvider))
            .collect(Collectors.toList()));
  }

  @Override
  public Multimap<String, NodeTree> getReferrers() {
    return new LazyLoadingMultimap<>(source.getReferrers().keySet(),
        attrId -> partialApply(referrerProvider, source).apply(attrId).stream()
            .map(value -> new LazyLoadingNodeTree(value, referenceProvider, referrerProvider))
            .collect(Collectors.toList()));
  }

}
