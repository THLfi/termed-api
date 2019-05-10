package fi.thl.termed.domain;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.LazyLoadingMultimap;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

public final class LazyLoadingNodeTree implements NodeTree {

  private final Node source;

  private final BiFunction<Node, String, ImmutableList<Node>> referenceProvider;
  private final BiFunction<Node, String, ImmutableList<Node>> referrerProvider;

  public LazyLoadingNodeTree(Node source,
      BiFunction<Node, String, ImmutableList<Node>> referenceProvider,
      BiFunction<Node, String, ImmutableList<Node>> referrerProvider) {
    this.source = source;
    this.referenceProvider = referenceProvider;
    this.referrerProvider = referrerProvider;
  }

  @Override
  public UUID getId() {
    return source.getId();
  }

  @Override
  public Optional<String> getCode() {
    return source.getCode();
  }

  @Override
  public Optional<String> getUri() {
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
  public LocalDateTime getCreatedDate() {
    return source.getCreatedDate();
  }

  @Override
  public String getLastModifiedBy() {
    return source.getLastModifiedBy();
  }

  @Override
  public LocalDateTime getLastModifiedDate() {
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
        attrId -> referenceProvider.apply(source, attrId).stream()
            .map(value -> new LazyLoadingNodeTree(value, referenceProvider, referrerProvider))
            .collect(toImmutableList()));
  }

  @Override
  public Multimap<String, NodeTree> getReferrers() {
    return new LazyLoadingMultimap<>(source.getReferrers().keySet(),
        attrId -> referrerProvider.apply(source, attrId).stream()
            .map(value -> new LazyLoadingNodeTree(value, referenceProvider, referrerProvider))
            .collect(toImmutableList()));
  }

}
