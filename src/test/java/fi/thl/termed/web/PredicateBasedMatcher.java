package fi.thl.termed.web;

import java.util.function.Predicate;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

final class PredicateBasedMatcher<T> extends TypeSafeMatcher<T> {

  private final String descriptionText;
  private final Predicate<T> matcher;

  private PredicateBasedMatcher(String descriptionText, Predicate<T> matcher) {
    super();
    this.descriptionText = descriptionText;
    this.matcher = matcher;
  }

  private PredicateBasedMatcher(Class<T> cls, String descriptionText, Predicate<T> matcher) {
    super(cls);
    this.descriptionText = descriptionText;
    this.matcher = matcher;
  }

  public static <T> Matcher<T> of(String descriptionText, Predicate<T> matcher) {
    return new PredicateBasedMatcher<>(descriptionText, matcher);
  }

  public static <T> Matcher<T> of(Class<T> cls, String descriptionText, Predicate<T> matcher) {
    return new PredicateBasedMatcher<>(cls, descriptionText, matcher);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(descriptionText);
  }

  @Override
  protected boolean matchesSafely(T item) {
    return matcher.test(item);
  }

}
