package fi.thl.termed.util.index.lucene;

import org.apache.lucene.codecs.FilterCodec;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;

public class TermedCodec extends FilterCodec {

  public TermedCodec() {
    super("TermedCodec", new Lucene80Codec());
  }

  @Override
  public StoredFieldsFormat storedFieldsFormat() {
    return new TermedStoredFieldsFormat();
  }

}
