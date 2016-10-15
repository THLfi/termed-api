package fi.thl.termed.util;

import java.util.function.Function;
import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.List;

import fi.thl.termed.util.collect.ListUtils;

import static org.junit.Assert.assertEquals;

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

}
