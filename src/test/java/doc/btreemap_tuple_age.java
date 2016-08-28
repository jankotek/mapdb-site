package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.tuple.Tuple2;

import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;

public class btreemap_tuple_age {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();
    //#a
    //create new map
    BTreeMap<Tuple2, Integer> persons = db
      .treeMap("persons", Tuple2.class, Integer.class)
      .createOrOpen();

    //insert three persons into map
    persons.put(new Tuple2("Smith","John"), 45);
    persons.put(new Tuple2("Smith","Peter"), 37);
    persons.put(new Tuple2("Doe","John"), 70);

    //now lets get map which contains all Smiths
    NavigableMap<Tuple2,Integer> smiths =
      persons.prefixSubMap(
        new Tuple2("Smith", null)  //null indicates wildcard for range query
      );
    //#z

    assertEquals(2, smiths.size());
  }
}
