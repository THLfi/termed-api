package fi.thl.termed.util.index.lucene;

import java.io.IOException;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.StoredFieldsReader;
import org.apache.lucene.codecs.StoredFieldsWriter;
import org.apache.lucene.codecs.compressing.CompressingStoredFieldsFormat;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;

public class TermedStoredFieldsFormat extends StoredFieldsFormat {

  @Override
  public StoredFieldsReader fieldsReader(Directory directory, SegmentInfo si, FieldInfos fn,
      IOContext context) throws IOException {
    return newStoredFieldsFormat().fieldsReader(directory, si, fn, context);
  }

  @Override
  public StoredFieldsWriter fieldsWriter(Directory directory, SegmentInfo si, IOContext context)
      throws IOException {
    return newStoredFieldsFormat().fieldsWriter(directory, si, context);
  }

  private StoredFieldsFormat newStoredFieldsFormat() {
    return new CompressingStoredFieldsFormat(
        "TermedStoredFieldsUncompressed",
        new UncompressedCompressionMode(),
        1 << 14, 128, 1024);
  }

}
