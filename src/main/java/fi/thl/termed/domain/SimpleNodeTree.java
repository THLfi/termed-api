package fi.thl.termed.domain;

import static com.google.common.collect.ImmutableMultimap.copyOf;
import static com.google.common.collect.Multimaps.transformValues;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Multimap;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class SimpleNodeTree implements NodeTree {

  private UUID id;
  private String code;
  private String uri;

  private String createdBy;
  private Date createdDate;
  private String lastModifiedBy;
  private Date lastModifiedDate;

  private TypeId type;

  private Multimap<String, StrictLangValue> properties;
  private Multimap<String, SimpleNodeTree> references;
  private Multimap<String, SimpleNodeTree> referrers;

  public SimpleNodeTree(NodeTree tree) {
    this.id = tree.getId();
    this.code = tree.getCode();
    this.uri = tree.getUri();
    this.createdBy = tree.getCreatedBy();
    this.createdDate = tree.getCreatedDate();
    this.lastModifiedBy = tree.getLastModifiedBy();
    this.lastModifiedDate = tree.getLastModifiedDate();
    this.type = tree.getType();
    // copy transformed map to get a concrete serializable map instead of lazily transformed view
    this.properties = copyOf(tree.getProperties());
    this.references = copyOf(transformValues(tree.getReferences(), SimpleNodeTree::new));
    this.referrers = copyOf(transformValues(tree.getReferrers(), SimpleNodeTree::new));
  }

  @Override
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  @Override
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  @Override
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  @Override
  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(String lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  @Override
  public Date getLastModifiedDate() {
    return lastModifiedDate;
  }

  public void setLastModifiedDate(Date lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }

  @Override
  public TypeId getType() {
    return type;
  }

  public void setType(TypeId type) {
    this.type = type;
  }

  @Override
  public Multimap<String, StrictLangValue> getProperties() {
    return properties;
  }

  public void setProperties(Multimap<String, StrictLangValue> properties) {
    this.properties = properties;
  }

  @Override
  public Multimap<String, SimpleNodeTree> getReferences() {
    return references;
  }

  public void setReferences(Multimap<String, SimpleNodeTree> references) {
    this.references = references;
  }

  @Override
  public Multimap<String, SimpleNodeTree> getReferrers() {
    return referrers;
  }

  public void setReferrers(Multimap<String, SimpleNodeTree> referrers) {
    this.referrers = referrers;
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
        .hash(id, code, uri, createdBy, createdDate, lastModifiedBy, lastModifiedDate, type,
            properties, references);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("id", id)
        .add("code", code)
        .add("uri", uri)
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
