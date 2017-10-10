package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.filterKeys;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DepthLimitedNodeTree implements NodeTree {

  private NodeTree source;

  private int depth;

  private Map<String, Integer> maxReferenceAttributeDepth;
  private Map<String, Integer> maxReferrerAttributeDepth;

  public DepthLimitedNodeTree(NodeTree source,
      Map<String, Integer> maxReferenceAttributeDepth,
      Map<String, Integer> maxReferrerAttributeDepth) {
    this(source, 0, maxReferenceAttributeDepth, maxReferrerAttributeDepth);
  }

  private DepthLimitedNodeTree(NodeTree source,
      int depth,
      Map<String, Integer> maxReferenceAttributeDepth,
      Map<String, Integer> maxReferrerAttributeDepth) {
    this.source = source;
    this.depth = depth;
    this.maxReferenceAttributeDepth = maxReferenceAttributeDepth;
    this.maxReferrerAttributeDepth = maxReferrerAttributeDepth;
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
  public Integer getNumber() {
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
  public Multimap<String, ? extends NodeTree> getReferences() {
    return transformValues(
        filterKeys(source.getReferences(),
            ref -> depth < maxReferenceAttributeDepth.getOrDefault(ref, 1)),
        filtered -> new DepthLimitedNodeTree(filtered,
            depth + 1,
            maxReferenceAttributeDepth,
            maxReferrerAttributeDepth));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    return transformValues(
        filterKeys(source.getReferrers(),
            ref -> depth < maxReferrerAttributeDepth.getOrDefault(ref, 1)),
        filtered -> new DepthLimitedNodeTree(filtered,
            depth + 1,
            maxReferenceAttributeDepth,
            maxReferrerAttributeDepth));
  }

}
