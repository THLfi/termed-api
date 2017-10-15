package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Objects;
import java.util.UUID;

public class ReferenceAttribute extends Attribute implements Identifiable<ReferenceAttributeId> {

  private final TypeId range;

  public ReferenceAttribute(String id, TypeId domain, TypeId range, String uri, Integer index,
      Multimap<String, Permission> permissions,
      Multimap<String, LangValue> properties) {
    super(id, domain, uri, index, permissions, properties);
    this.range = range;
  }

  public static IdBuilder builder() {
    return new IdBuilder();
  }

  public static Builder builderFromCopyOf(ReferenceAttribute attribute) {
    Builder builder = new Builder(attribute.getId(), attribute.getDomain(), attribute.getRange());
    builder.copyOptionalsFrom(attribute);
    return builder;
  }

  @Override
  public ReferenceAttributeId identifier() {
    return new ReferenceAttributeId(getDomain(), getId());
  }

  public TypeId getRange() {
    return range;
  }

  public String getRangeId() {
    return range != null ? range.getId() : null;
  }

  public UUID getRangeGraphId() {
    return range != null ? range.getGraphId() : null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ReferenceAttribute that = (ReferenceAttribute) o;
    return Objects.equals(range, that.range);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), range);
  }

  @Override
  public MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("range", range);
  }


  public static class IdBuilder {

    IdBuilder() {
    }

    public RangeBuilder id(String id, TypeId domain) {
      return new RangeBuilder(id, domain);
    }

  }

  public static class RangeBuilder {

    private final String id;
    private final TypeId domain;

    RangeBuilder(String id, TypeId domain) {
      this.id = id;
      this.domain = domain;
    }

    public Builder range(TypeId range) {
      return new Builder(id, domain, range);
    }

  }

  public static class Builder {

    private final String id;
    private final TypeId domain;
    private final TypeId range;

    private String uri;
    private Integer index;

    private Multimap<String, Permission> permissions;
    private Multimap<String, LangValue> properties;

    Builder(String id, TypeId domain, TypeId range) {
      this.id = requireNonNull(id);
      this.domain = requireNonNull(domain);
      this.range = requireNonNull(range);
    }

    public Builder copyOptionalsFrom(ReferenceAttribute attribute) {
      this.uri = attribute.uri;
      this.index = attribute.index;
      this.permissions = attribute.permissions;
      this.properties = attribute.properties;
      return this;
    }

    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder index(Integer index) {
      this.index = index;
      return this;
    }

    public Builder permissions(Multimap<String, Permission> permissions) {
      this.permissions = permissions;
      return this;
    }

    public Builder properties(Multimap<String, LangValue> properties) {
      this.properties = properties;
      return this;
    }

    public Builder properties(String k0, LangValue v0) {
      this.properties = ImmutableMultimap.of(k0, v0);
      return this;
    }

    public Builder properties(String k0, LangValue v0, String k1, LangValue v1) {
      this.properties = ImmutableMultimap.of(k0, v0, k1, v1);
      return this;
    }

    public Builder properties(String k0, LangValue v0, String k1, LangValue v1, String k2,
        LangValue v2) {
      this.properties = ImmutableMultimap.of(k0, v0, k1, v1, k2, v2);
      return this;
    }

    public ReferenceAttribute build() {
      return new ReferenceAttribute(id, domain, range, uri, index, permissions, properties);
    }

  }

}
