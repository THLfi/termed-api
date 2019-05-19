package fi.thl.termed.util.index.lucene;

import java.io.IOException;
import org.apache.lucene.codecs.compressing.CompressionMode;
import org.apache.lucene.codecs.compressing.Compressor;
import org.apache.lucene.codecs.compressing.Decompressor;
import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.BytesRef;

/**
 * Compression mode that does not compress.
 */
public class UncompressedCompressionMode extends CompressionMode {

  @Override
  public Compressor newCompressor() {
    return new Compressor() {
      @Override
      public void compress(byte[] bytes, int off, int len, DataOutput out) throws IOException {
        out.writeBytes(bytes, off, len);
      }

      @Override
      public void close() {
      }
    };
  }

  @Override
  public Decompressor newDecompressor() {
    return new Decompressor() {

      @Override
      public void decompress(DataInput in, int originalLength, int offset, int length,
          BytesRef bytes)
          throws IOException {
        bytes.bytes = ArrayUtil.grow(bytes.bytes, offset + length);
        in.readBytes(bytes.bytes, 0, offset + length);
        bytes.offset = offset;
        bytes.length = length;
      }

      @Override
      public Decompressor clone() {
        return newDecompressor();
      }
    };
  }

}
