package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;

import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;

public class btreemap_tuple_array {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();
    //#a
    //create new map
    BTreeMap<Object[], Integer> persons = db
      .treeMap("persons", new SerializerArrayTuple(2), Serializer.INTEGER)
      .createOrOpen();

    //insert three person into map
    persons.put(new Object[]{"Smith", "John"}, 45);
    persons.put(new Object[]{"Smith", "Peter"}, 37);
    persons.put(new Object[]{"Doe", "John"}, 70);

    //now lets get map which contains all Smiths
    NavigableMap<Object[],Integer> smiths =
      persons.prefixSubMap(
        new Object[]{"Smith"}
      );
    //#z

    assertEquals(2, smiths.size());
  }
}
