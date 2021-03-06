package fi.thl.termed.domain;

import static com.google.common.collect.ImmutableMultimap.copyOf;
import static com.google.common.collect.Multimaps.transformValues;
import static fi.thl.termed.util.collect.MultimapUtils.nullToEmpty;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class SimpleNodeTree implements NodeTree {

  private final UUID id;
  private final String code;
  private final String uri;
  private final Long number;

  private final String createdBy;
  private final LocalDateTime createdDate;
  private final String lastModifiedBy;
  private final LocalDateTime lastModifiedDate;

  private final TypeId type;

  private final ImmutableMultimap<String, StrictLangValue> properties;
  private final ImmutableMultimap<String, SimpleNodeTree> references;
  private final ImmutableMultimap<String, SimpleNodeTree> referrers;

  public SimpleNodeTree(NodeTree tree) {
    this.id = tree.getId();
    this.code = tree.getCode().orElse(null);
    this.uri = tree.getUri().orElse(null);
    this.number = tree.getNumber();
    this.createdBy = tree.getCreatedBy();
    this.createdDate = tree.getCreatedDate();
    this.lastModifiedBy = tree.getLastModifiedBy();
    this.lastModifiedDate = tree.getLastModifiedDate();
    this.type = tree.getType();
    // copy transformed map to get a concrete serializable map instead of lazily transformed view
    this.properties = copyOf(
        nullToEmpty(tree.getProperties()));
    this.references = copyOf(
        transformValues(nullToEmpty(tree.getReferences()), SimpleNodeTree::new));
    this.referrers = copyOf(
        transformValues(nullToEmpty(tree.getReferrers()), SimpleNodeTree::new));
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public Optional<String> getCode() {
    return Optional.ofNullable(code);
  }

  @Override
  public Optional<String> getUri() {
    return Optional.ofNullable(uri);
  }

  @Override
  public Long getNumber() {
    return number;
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  @Override
  public LocalDateTime getCreatedDate() {
    return createdDate;
  }

  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  @Override
  public LocalDateTime getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public TypeId getType() {
    return type;
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    return properties;
  }

  @Override
  public Multimap<String, SimpleNodeTree> getReferences() {
    return references;
  }

  @Override
  public Multimap<String, SimpleNodeTree> getReferrers() {
    return referrers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SimpleNodeTree that = (SimpleNodeTree) o;
    return Objects.equals(id, that.id) &&
        Objects.equals(code, that.code) &&
        Objects.equals(uri, that.uri) &&
        Objects.equals(number, that.number) &&
        Objects.equals(createdBy, that.createdBy) &&
        Objects.equals(createdDate, that.createdDate) &&
        Objects.equals(lastModifiedBy, that.lastModifiedBy) &&
        Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
        Objects.equals(type, that.type) &&
        Objects.equals(properties, that.properties) &&
        Objects.equals(references, that.references);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(id, code, uri, number, createdBy, createdDate, lastModifiedBy, lastModifiedDate, type,
            properties, references);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("code", code)
        .add("uri", uri)
        .add("number", number)
        .add("createdBy", createdBy)
        .add("createdDate", createdDate)
        .add("lastModifiedBy", lastModifiedBy)
        .add("lastModifiedDate", lastModifiedDate)
        .add("type", type)
        .add("properties", properties)
        .add("references", references)
        .toString();
  }

}
