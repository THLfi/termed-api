package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.transformEntries;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

public class LazyLoadingNodeTree implements NodeTree {

  private Node source;

  private BiFunction<Node, String, List<Node>> referenceProvider;
  private BiFunction<Node, String, List<Node>> referrerProvider;

  private Table<String, NodeId, NodeTree> referenceCache = HashBasedTable.create();
  private Table<String, NodeId, NodeTree> referrerCache = HashBasedTable.create();

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
    return transformEntries(source.getReferences(), this::loadReference);
  }

  private NodeTree loadReference(String attributeId, NodeId valueId) {
    if (!referenceCache.contains(attributeId, valueId)) {
      referenceProvider.apply(source, attributeId)
          .forEach(value -> referenceCache.put(attributeId, value.identifier(),
              new LazyLoadingNodeTree(value, referenceProvider, referrerProvider)));
    }
    return referenceCache.get(attributeId, valueId);
  }

  @Override
  public Multimap<String, NodeTree> getReferrers() {
    return transformEntries(source.getReferrers(), this::loadReferrer);
  }

  private NodeTree loadReferrer(String attributeId, NodeId valueId) {
    if (!referrerCache.contains(attributeId, valueId)) {
      referrerProvider.apply(source, attributeId)
          .forEach(value -> referrerCache.put(attributeId, value.identifier(),
              new LazyLoadingNodeTree(value, referenceProvider, referrerProvider)));
    }
    return referrerCache.get(attributeId, valueId);
  }

}
