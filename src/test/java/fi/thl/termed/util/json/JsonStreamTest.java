package fi.thl.termed.util.json;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.Test;

public class JsonStreamTest {

  private Gson gson = new Gson();

  @Test
  public void shouldParseJsonFromInputStreamToJavaStream() {
    String exampleJson = "[{'value':'A'},{'value':'B'},{'value':'C'}]";
    InputStream in = new ByteArrayInputStream(exampleJson.getBytes(UTF_8));

    Stream<Example> stream = JsonStream.readArray(gson, Example.class, in);

    assertEquals(
        asList(new Example("A"), new Example("B"), new Example("C")),
        stream.collect(toList()));
  }

  private class Example {

    private String value;

    public Example(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Example example = (Example) o;
      return Objects.equals(value, example.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }

  }

}