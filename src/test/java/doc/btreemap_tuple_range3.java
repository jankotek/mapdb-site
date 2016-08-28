package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.tuple.Tuple;
import org.mapdb.tuple.Tuple3;

public class btreemap_tuple_range3 {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();

    BTreeMap<Tuple3, Integer> persons = db
      .treeMap("persons", Tuple3.class, Integer.class)
      .createOrOpen();
    //#a
    //WRONG!! null is in middle position
    persons.prefixSubMap(new Tuple3("Smith",null,11));

    //same but submap
    //WRONG!! infinity is in middle
    persons.subMap(
      new Tuple3("Smith", null,     11),
      new Tuple3("Smith", Tuple.HI, 11)
    );
    //#z
  }
}
