package fi.thl.termed.domain;

import static com.google.common.collect.Multimaps.filterKeys;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class DepthLimitedNodeTree implements NodeTree {

  private NodeTree source;

  private int currentReferenceDepth;
  private int currentReferrerDepth;

  private Map<String, Integer> maxReferenceAttributeDepth;
  private Map<String, Integer> maxReferrerAttributeDepth;

  public DepthLimitedNodeTree(NodeTree source,
      Map<String, Integer> maxReferenceAttributeDepth,
      Map<String, Integer> maxReferrerAttributeDepth) {
    this(source, 0, 0, maxReferenceAttributeDepth, maxReferrerAttributeDepth);
  }

  private DepthLimitedNodeTree(NodeTree source,
      int currentReferenceDepth,
      int currentReferrerDepth,
      Map<String, Integer> maxReferenceAttributeDepth,
      Map<String, Integer> maxReferrerAttributeDepth) {
    this.source = source;
    this.currentReferenceDepth = currentReferenceDepth;
    this.currentReferrerDepth = currentReferrerDepth;
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
            ref -> currentReferenceDepth < maxReferenceAttributeDepth.getOrDefault(ref, 1)),
        filtered -> new DepthLimitedNodeTree(filtered,
            currentReferenceDepth + 1,
            currentReferrerDepth,
            maxReferenceAttributeDepth,
            maxReferrerAttributeDepth));
  }

  @Override
  public Multimap<String, ? extends NodeTree> getReferrers() {
    return transformValues(
        filterKeys(source.getReferrers(),
            ref -> currentReferrerDepth < maxReferrerAttributeDepth.getOrDefault(ref, 1)),
        filtered -> new DepthLimitedNodeTree(filtered,
            currentReferenceDepth,
            currentReferrerDepth + 1,
            maxReferenceAttributeDepth,
            maxReferrerAttributeDepth));
  }

}
