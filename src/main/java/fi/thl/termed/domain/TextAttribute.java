package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import fi.thl.termed.util.RegularExpressions;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Objects;

public class TextAttribute extends Attribute implements Identifiable<TextAttributeId> {

  private final String regex;

  public TextAttribute(String id, TypeId domain, String regex, String uri, Integer index,
      Multimap<String, Permission> permissions,
      Multimap<String, LangValue> properties) {
    super(id, domain, uri, index, permissions, properties);
    this.regex = regex;
  }

  public static IdBuilder builder() {
    return new IdBuilder();
  }

  public static Builder builderFromCopyOf(TextAttribute attribute) {
    Builder builder = new Builder(attribute.getId(), attribute.getDomain(), attribute.getRegex());
    builder.copyOptionalsFrom(attribute);
    return builder;
  }

  @Override
  public TextAttributeId identifier() {
    return new TextAttributeId(getDomain(), getId());
  }

  public String getRegex() {
    return MoreObjects.firstNonNull(regex, RegularExpressions.ALL);
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
    TextAttribute that = (TextAttribute) o;
    return Objects.equals(regex, that.regex);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), regex);
  }

  @Override
  public MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("regex", regex);
  }

  public static class IdBuilder {

    IdBuilder() {
    }

    public RegexBuilder id(String id, TypeId domain) {
      return new RegexBuilder(id, domain);
    }

  }

  public static class RegexBuilder {

    private final String id;
    private final TypeId domain;

    RegexBuilder(String id, TypeId domain) {
      this.id = id;
      this.domain = domain;
    }

    public Builder regex(String regex) {
      return new Builder(id, domain, regex);
    }

    public Builder regexAll() {
      return new Builder(id, domain, RegularExpressions.ALL);
    }

  }

  public static class Builder {

    private final String id;
    private final TypeId domain;
    private final String regex;

    private String uri;
    private Integer index;

    private Multimap<String, Permission> permissions;
    private Multimap<String, LangValue> properties;

    Builder(String id, TypeId domain, String regex) {
      this.id = requireNonNull(id);
      this.domain = requireNonNull(domain);
      this.regex = requireNonNull(regex);
    }

    public Builder copyOptionalsFrom(TextAttribute attribute) {
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

    public TextAttribute build() {
      return new TextAttribute(id, domain, regex, uri, index, permissions, properties);
    }

  }

}
