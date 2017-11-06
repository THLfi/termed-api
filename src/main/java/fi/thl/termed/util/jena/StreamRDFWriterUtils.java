package fi.thl.termed.util.jena;

import java.io.OutputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.util.iterator.ExtendedIterator;

public final class StreamRDFWriterUtils {

  private StreamRDFWriterUtils() {
  }

  public static void writeAndCloseIterator(OutputStream out, ExtendedIterator<Triple> triples,
      Lang lang) {
    try {
      StreamRDF rdfStream = StreamRDFWriter.getWriterStream(out, lang);
      rdfStream.start();
      while (triples.hasNext()) {
        rdfStream.triple(triples.next());
      }
      rdfStream.finish();
    } finally {
      triples.close();
    }
  }

}
