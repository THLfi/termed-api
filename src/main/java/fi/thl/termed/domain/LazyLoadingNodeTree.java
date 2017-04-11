package fi.thl.termed.domain;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class LazyLoadingNodeTree implements NodeTree {

  private Node source;

  private ImmutableMultimap<String, NodeTree> treeReferences;
  private ImmutableMultimap<String, NodeTree> treeReferrers;

  private ImmutableSet<NodeId> referencePath;
  private ImmutableSet<NodeId> referrerPath;

  private BiFunction<Node, String, List<Node>> referenceProvider;
  private BiFunction<Node, String, List<Node>> referrerProvider;

  public LazyLoadingNodeTree(Node source,
      BiFunction<Node, String, List<Node>> referenceProvider,
      BiFunction<Node, String, List<Node>> referrerProvider) {
    this(source,
        ImmutableSet.of(source.identifier()),
        ImmutableSet.of(source.identifier()),
        referenceProvider,
        referrerProvider);
  }

  private LazyLoadingNodeTree(Node source,
      ImmutableSet<NodeId> referencePath,
      ImmutableSet<NodeId> referrerPath,
      BiFunction<Node, String, List<Node>> referenceProvider,
      BiFunction<Node, String, List<Node>> referrerProvider) {
    this.source = source;
    this.referencePath = referencePath;
    this.referrerPath = referrerPath;
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
    if (treeReferences == null) {
      treeReferences = loadReferences();
    }
    return treeReferences;
  }

  private ImmutableMultimap<String, NodeTree> loadReferences() {
    ImmutableMultimap.Builder<String, NodeTree> rs = ImmutableMultimap.builder();

    for (String attributeId : source.getReferences().keySet()) {
      for (Node reference : referenceProvider.apply(source, attributeId)) {
        if (!referencePath.contains(reference.identifier())) {
          rs.put(attributeId, newLazyLoadingReferenceTree(reference));
        }
      }
    }

    return rs.build();
  }

  private LazyLoadingNodeTree newLazyLoadingReferenceTree(Node reference) {
    return new LazyLoadingNodeTree(reference,
        copyAndAppend(referencePath, reference.identifier()),
        referrerPath,
        referenceProvider,
        referrerProvider);
  }

  @Override
  public Multimap<String, NodeTree> getReferrers() {
    if (treeReferrers == null) {
      treeReferrers = loadReferrers();
    }
    return treeReferrers;
  }

  private ImmutableMultimap<String, NodeTree> loadReferrers() {
    ImmutableMultimap.Builder<String, NodeTree> rs = ImmutableMultimap.builder();

    for (String attributeId : source.getReferrers().keySet()) {
      for (Node referrer : referrerProvider.apply(source, attributeId)) {
        if (!referrerPath.contains(referrer.identifier())) {
          rs.put(attributeId, newLazyLoadingReferrerTree(referrer));
        }
      }
    }

    return rs.build();
  }

  private LazyLoadingNodeTree newLazyLoadingReferrerTree(Node referrer) {
    return new LazyLoadingNodeTree(referrer,
        referencePath,
        copyAndAppend(referrerPath, referrer.identifier()),
        referenceProvider,
        referrerProvider);
  }

  private <T> ImmutableSet<T> copyAndAppend(ImmutableSet<T> set, T last) {
    return ImmutableSet.<T>builder().addAll(set).add(last).build();
  }

}
