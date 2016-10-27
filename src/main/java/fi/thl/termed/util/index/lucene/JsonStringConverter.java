package fi.thl.termed.util.index.lucene;

import com.google.gson.Gson;

import fi.thl.termed.util.Converter;

public class JsonStringConverter<E> extends Converter<E, String> {

  private Gson gson;
  private Class<E> type;

  public JsonStringConverter(Class<E> type) {
    this(new Gson(), type);
  }

  public JsonStringConverter(Gson gson, Class<E> type) {
    this.gson = gson;
    this.type = type;
  }

  @Override
  public String apply(E e) {
    return gson.toJson(e);
  }

  @Override
  public E applyInverse(String e) {
    return gson.fromJson(e, type);
  }

}
