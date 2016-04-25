package fi.thl.termed.index.lucene;

import com.google.common.base.Strings;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.FieldInfo;

import java.util.UUID;

/**
 * Defined for convenience for codes and ids etc. Similar to Lucene's {@code StringField} but
 * transforms value into lower case and does not store it. Also accepts UUID and null values. Nulls
 * are replaced by empty strings.
 */
public class LowerCaseCodeField extends Field {

  public static final FieldType TYPE = new FieldType();

  static {
    TYPE.setIndexed(true);
    TYPE.setOmitNorms(true);
    TYPE.setIndexOptions(FieldInfo.IndexOptions.DOCS_ONLY);
    TYPE.setStored(false);
    TYPE.setTokenized(false);
    TYPE.freeze();
  }

  public LowerCaseCodeField(String name, UUID value) {
    this(name, value != null ? value.toString() : "");
  }

  public LowerCaseCodeField(String name, String value) {
    super(name, Strings.nullToEmpty(value).toLowerCase(), TYPE);
  }

}
