package fi.thl.termed.util.rdf;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public final class JenaUtils {

  public static Model fromRdfXmlString(String rdfXml) {
    Reader stringReader = new StringReader(rdfXml);
    Model model = ModelFactory.createDefaultModel();
    model.read(stringReader, "", "RDF/XML");
    return model;
  }

  public static String toRdfXmlString(Model model) {
    Writer stringWriter = new StringWriter();
    model.write(stringWriter, "RDF/XML");
    return stringWriter.toString();
  }

  public static String toRdfTtlString(Model model) {
    Writer stringWriter = new StringWriter();
    model.write(stringWriter, "TTL");
    return stringWriter.toString();
  }

}
