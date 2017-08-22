package fi.thl.termed.util;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import fi.thl.termed.util.collect.ListUtils;
import java.util.List;
import java.util.function.Function;
import org.junit.Test;

public class ListUtilsTest {

  @Test
  public void shouldFlattenNestedLists() {
    List<List<String>> listOfLists = Lists.newArrayList();
    listOfLists.add(Lists.newArrayList("a", "b"));
    listOfLists.add(Lists.newArrayList("c", "d"));

    assertEquals(Lists.newArrayList("a", "b", "c", "d"), ListUtils.flatten(listOfLists));
  }

  @Test
  public void shouldTransformNestedLists() {
    List<List<String>> listOfLists = Lists.newArrayList();
    listOfLists.add(Lists.newArrayList("a", "b"));
    listOfLists.add(Lists.newArrayList("c", "d"));

    listOfLists = ListUtils.transformNested(listOfLists, new Function<String, String>() {
      @Override
      public String apply(String input) {
        return input.toUpperCase();
      }
    });

    assertEquals(Lists.newArrayList("A", "B", "C", "D"), ListUtils.flatten(listOfLists));
  }

  @Test
  public void shouldApplyDistribution() {
    List<List<String>> input = asList(
        asList("a", "b"),
        asList("x", "y", "z"));

    assertEquals(asList(
        asList("a", "x"), asList("a", "y"), asList("a", "z"),
        asList("b", "x"), asList("b", "y"), asList("b", "z")),
        ListUtils.distribute(input));
  }

}
