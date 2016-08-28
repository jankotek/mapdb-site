package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.tuple.Tuple2;
import org.mapdb.tuple.Tuple2Serializer;

import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;

public class btreemap_tuple_age_bytearray {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();
    //#a
    //create new map
    BTreeMap<Tuple2<byte[], byte[]>, Integer> persons = db
      .treeMap("persons")
      .keySerializer(new Tuple2Serializer(Serializer.BYTE_ARRAY, Serializer.BYTE_ARRAY))
      .valueSerializer(Serializer.INTEGER)
      .createOrOpen();

    persons.put(new Tuple2("Smith".getBytes(),"John".getBytes()), 45);

    NavigableMap<Tuple2,Integer> smiths =
      persons.prefixSubMap(
        new Tuple2("Smith".getBytes(), null)
      );
    //#z

    assertEquals(1, smiths.size());
  }
}
