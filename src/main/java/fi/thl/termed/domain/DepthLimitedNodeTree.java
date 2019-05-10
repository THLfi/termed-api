package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.filterKeys;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DepthLimitedNodeTree implements NodeTree {

  private final NodeTree source;

  private final int depth;

  private final ImmutableMap<Tuple2<TypeId, String>, Integer> maxReferenceAttributeDepth;
  private final ImmutableMap<Tuple2<TypeId, String>, Integer> maxReferrerAttributeDepth;

  public DepthLimitedNodeTree(NodeTree source,
      Map<Tuple2<TypeId, String>, Integer> maxReferenceAttributeDepth,
      Map<Tuple2<TypeId, String>, Integer> maxReferrerAttributeDepth) {
    this(source, 0, maxReferenceAttributeDepth, maxReferrerAttributeDepth);
  }

  private DepthLimitedNodeTree(NodeTree source,
      int depth,
      Map<Tuple2<TypeId, String>, Integer> maxReferenceAttributeDepth,
      Map<Tuple2<TypeId, String>, Integer> maxReferrerAttributeDepth) {
    this.source = source;
    this.depth = depth;
    this.maxReferenceAttributeDepth = ImmutableMap.copyOf(maxReferenceAttributeDepth);
    this.maxReferrerAttributeDepth = ImmutableMap.copyOf(maxReferrerAttributeDepth);
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
  public Multimap<String, ? extends NodeTree> getReferences() {
    return transformValues(
        filterKeys(source.getReferences(),
            attr -> depth < maxReferenceAttributeDepth.getOrDefault(Tuple.of(getType(), attr), 1)),
        filtered -> new DepthLimitedNodeTree(filtered,
            depth + 1,
            maxReferenceAttributeDepth,
            maxReferrerAttributeDepth));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    return transformValues(
        filterKeys(source.getReferrers(),
            attr -> depth < maxReferrerAttributeDepth.getOrDefault(Tuple.of(getType(), attr), 1)),
        filtered -> new DepthLimitedNodeTree(filtered,
            depth + 1,
            maxReferenceAttributeDepth,
            maxReferrerAttributeDepth));
  }

}
