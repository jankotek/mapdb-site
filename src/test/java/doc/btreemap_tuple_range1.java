package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.tuple.Tuple2;

import java.util.NavigableMap;

public class btreemap_tuple_range1 {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();

    BTreeMap<Tuple2, Integer> persons = db
      .treeMap("persons", Tuple2.class, Integer.class)
      .createOrOpen();
    //#a
    NavigableMap<Tuple2,Integer> smiths =
      persons.prefixSubMap(new Tuple2("Smith", null));

    // is equivalent to
    smiths = persons.subMap(
      new Tuple2("Smith", Integer.MIN_VALUE), true,
      new Tuple2("Smith", Integer.MAX_VALUE), true
    );
    //#z
  }
}
