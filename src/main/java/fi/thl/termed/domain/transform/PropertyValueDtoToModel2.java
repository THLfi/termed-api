package fi.thl.termed.domain.transform;

import static fi.thl.termed.util.collect.StreamUtils.zipIndex;

import com.google.common.collect.Multimap;
import fi.thl.termed.domain.LangValue;
import fi.thl.termed.domain.PropertyValueId;
import fi.thl.termed.util.collect.Tuple;
import fi.thl.termed.util.collect.Tuple2;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

public class PropertyValueDtoToModel2<K extends Serializable> implements
    Function<Multimap<String, LangValue>, Stream<Tuple2<PropertyValueId<K>, LangValue>>> {

  private K subjectId;

  public PropertyValueDtoToModel2(K subjectId) {
    this.subjectId = subjectId;
  }

  @Override
  public Stream<Tuple2<PropertyValueId<K>, LangValue>> apply(Multimap<String, LangValue> input) {
    Stream<Entry<String, Collection<LangValue>>> entries = input.asMap().entrySet().stream();

    return entries.flatMap(e -> zipIndex(e.getValue().stream().distinct(),
        (value, i) -> Tuple.of(new PropertyValueId<>(subjectId, e.getKey(), i), value)));
  }

}
