package fi.thl.termed.domain;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import fi.thl.termed.util.collect.Identifiable;
import java.util.Date;
import java.util.Objects;

public final class Revision implements Identifiable<Long> {

  private final Long number;
  private final String author;
  private final Date date;

  private Revision(Long number, String author, Date date) {
    this.number = requireNonNull(number);
    this.author = requireNonNull(author);
    this.date = requireNonNull(date);
  }

  public static Revision of(Long number, String author, Date date) {
    return new Revision(number, author, date);
  }

  @Override
  public Long identifier() {
    return number;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Revision revision = (Revision) o;
    return Objects.equals(number, revision.number) &&
        Objects.equals(author, revision.author) &&
        Objects.equals(date, revision.date);
  }

  @Override
  public int hashCode() {
    return Objects.hash(number, author, date);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("number", number)
        .add("author", author)
        .add("date", date)
        .toString();
  }

}
