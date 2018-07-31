package fi.thl.termed.domain;

import java.util.Date;
import java.util.Objects;

public final class ObjectRevision<E> {

  private final Long number;
  private final String author;
  private final Date date;
  private final RevisionType type;
  private final E object;

  public ObjectRevision(Revision revision, RevisionType type, E object) {
    this(revision.getNumber(), revision.getAuthor(), revision.getDate(), type, object);
  }

  public ObjectRevision(Long number, String author, Date date, RevisionType type, E object) {
    this.number = number;
    this.author = author;
    this.date = date;
    this.type = type;
    this.object = object;
  }

  public Long getNumber() {
    return number;
  }

  public String getAuthor() {
    return author;
  }

  public Date getDate() {
    return date;
  }

  public RevisionType getType() {
    return type;
  }

  public E getObject() {
    return object;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ObjectRevision<?> that = (ObjectRevision<?>) o;
    return Objects.equals(number, that.number) &&
        Objects.equals(author, that.author) &&
        Objects.equals(date, that.date) &&
        Objects.equals(type, that.type) &&
        Objects.equals(object, that.object);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number, author, date, type, object);
  }

}
