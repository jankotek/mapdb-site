package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayDelta;

import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;

public class btreemap_tuple_array_val {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();
    //#a
    //create new map
    BTreeMap<Object[], Integer> persons = db
      .treeMap("persons", new SerializerArrayDelta(), Serializer.INTEGER)
      .createOrOpen();

    //insert three persons into map
    persons.put(new Object[]{"Smith", "John"}, 45);
    persons.put(new Object[]{"Smith", "Peter"}, 37);
    persons.put(new Object[]{"Doe", "John"}, 70);


    NavigableMap<Object[],Integer> smiths = persons.subMap(
      new Object[]{"Smith"}, //lower bound
      new Object[]{"Smith", null} //upper bound, null is positive infinity in this serializer
    );
    //#z

    assertEquals(2, smiths.size());
  }
}
