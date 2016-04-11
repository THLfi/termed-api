package fi.thl.termed.util;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GraphUtilsTest {

  @Test
  public void shouldFindAllPathsFromTree() {
    Node tree = new Node("1",
                         new Node("1.1",
                                  new Node("1.1.1")),
                         new Node("1.2",
                                  new Node("1.2.1"), new Node("1.2.2"), new Node("1.2.3")));

    List<List<Node>> paths = GraphUtils.collectPaths(tree, new NodeNeighbourFunction());

    assertEquals(4, paths.size());
    assertEquals("[1.1.1, 1.1, 1]", paths.get(0).toString());
    assertEquals("[1.2.1, 1.2, 1]", paths.get(1).toString());
    assertEquals("[1.2.2, 1.2, 1]", paths.get(2).toString());
    assertEquals("[1.2.3, 1.2, 1]", paths.get(3).toString());
  }

  @Test
  public void shouldFindAllPathsFromDAG() {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    Node d = new Node("d");

    a.neigbours = Lists.newArrayList(b, c);
    b.neigbours = Lists.newArrayList(d);
    c.neigbours = Lists.newArrayList(d);

    List<List<Node>> paths = GraphUtils.collectPaths(a, new NodeNeighbourFunction());

    assertEquals(2, paths.size());
    assertEquals("[d, b, a]", paths.get(0).toString());
    assertEquals("[d, c, a]", paths.get(1).toString());
  }

  @Test
  public void shouldFindAllPathsFromGraph() {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    Node d = new Node("d");

    a.neigbours = Lists.newArrayList(b, c);
    b.neigbours = Lists.newArrayList(d);
    c.neigbours = Lists.newArrayList(a, d);

    List<List<Node>> paths = GraphUtils.collectPaths(a, new NodeNeighbourFunction());

    assertEquals(3, paths.size());
    assertEquals("[d, b, a]", paths.get(0).toString());
    assertEquals("[c, a]", paths.get(1).toString());
    assertEquals("[d, c, a]", paths.get(2).toString());
  }

  @Test
  public void shouldFindAllReachableNodes() {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    Node d = new Node("d");

    a.neigbours = Lists.newArrayList(b, c);
    b.neigbours = Lists.newArrayList(d);
    c.neigbours = Lists.newArrayList(a, d);

    assertEquals("[a, b, d, c]",
                 GraphUtils.collectNodes(a, new NodeNeighbourFunction()).toString());
    assertEquals("[b, d]",
                 GraphUtils.collectNodes(b, new NodeNeighbourFunction()).toString());
    assertEquals("[c, a, b, d]",
                 GraphUtils.collectNodes(c, new NodeNeighbourFunction()).toString());
  }

  @Test
  public void shouldPrettyPrintDAG() {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    Node d = new Node("d");

    a.neigbours = Lists.newArrayList(b, c);
    b.neigbours = Lists.newArrayList(d);
    c.neigbours = Lists.newArrayList(d);

    String tree = GraphUtils.prettyPrintTree(GraphUtils.toTree(a, new NodeNeighbourFunction()));

    assertEquals(" - a\n"
                 + "\t - b\n"
                 + "\t\t - d\n"
                 + "\t - c\n"
                 + "\t\t - d\n", tree);
  }

  @Test
  public void shouldPrettyPrintGraph() {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    Node d = new Node("d");

    a.neigbours = Lists.newArrayList(b, c);
    b.neigbours = Lists.newArrayList(d);
    c.neigbours = Lists.newArrayList(a, d);

    String tree = GraphUtils.prettyPrintTree(GraphUtils.toTree(a, new NodeNeighbourFunction()));

    assertEquals(" - a\n"
                 + "\t - b\n"
                 + "\t\t - d\n"
                 + "\t - c\n"
                 + "\t\t - d\n", tree);
  }

  @Test
  public void shouldPrettyPrintAnotherGraph() {
    Node a = new Node("a");
    Node b = new Node("b");
    Node c = new Node("c");
    Node d = new Node("d");
    Node e = new Node("e");
    Node f = new Node("f");

    a.neigbours = Lists.newArrayList(b, c, e, f);
    b.neigbours = Lists.newArrayList(d, e, c);
    c.neigbours = Lists.newArrayList(a, d);
    d.neigbours = Lists.newArrayList(a, d);
    e.neigbours = Lists.newArrayList(a, d);
    f.neigbours = Lists.newArrayList(a, d);

    String tree = GraphUtils.prettyPrintTree(GraphUtils.toTree(a, new NodeNeighbourFunction()));

    assertEquals(" - a\n"
                 + "\t - b\n"
                 + "\t\t - d\n"
                 + "\t\t - e\n"
                 + "\t\t\t - d\n"
                 + "\t\t - c\n"
                 + "\t\t\t - d\n"
                 + "\t - c\n"
                 + "\t\t - d\n"
                 + "\t - e\n"
                 + "\t\t - d\n"
                 + "\t - f\n"
                 + "\t\t - d\n", tree);
  }

  private class NodeNeighbourFunction implements Function<Node, List<Node>> {

    @Override
    public List<Node> apply(Node input) {
      return input.neigbours;
    }
  }

  private class Node {

    private String id;
    private List<Node> neigbours;

    public Node(String id) {
      this.id = id;
    }

    public Node(String id, Node... neigbours) {
      this.id = id;
      this.neigbours = Arrays.asList(neigbours);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Node node = (Node) o;
      return Objects.equal(id, node.id);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id);
    }

    @Override
    public String toString() {
      return id;
    }
  }

}
