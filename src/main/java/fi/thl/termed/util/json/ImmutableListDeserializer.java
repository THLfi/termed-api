package fi.thl.termed.util.json;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;

public class ImmutableListDeserializer implements JsonDeserializer<ImmutableList<?>> {

  @Override
  @SuppressWarnings("unchecked")
  public ImmutableList<?> deserialize(JsonElement json, Type type,
      JsonDeserializationContext context) throws JsonParseException {
    TypeToken<ImmutableList<?>> typeToken = (TypeToken<ImmutableList<?>>) TypeToken.of(type);
    List<?> list = context.deserialize(json, typeToken.getSupertype(List.class).getType());
    return ImmutableList.copyOf(list);
  }

}
