package fi.thl.termed.util.rdf;

import org.apache.jena.rdf.model.Model;

import java.io.StringWriter;
import java.io.Writer;

public final class JenaUtils {

  private JenaUtils() {
  }

  public static String toRdfXmlString(Model model) {
    Writer stringWriter = new StringWriter();
    model.write(stringWriter, "RDF/XML");
    return stringWriter.toString();
  }

}
