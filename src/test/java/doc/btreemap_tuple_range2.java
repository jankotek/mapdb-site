package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.tuple.Tuple;
import org.mapdb.tuple.Tuple2;

public class btreemap_tuple_range2 {

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();

    BTreeMap<Tuple2, Integer> persons = db
      .treeMap("persons", Tuple2.class, Integer.class)
      .createOrOpen();
    //#a
    persons.subMap(
      new Tuple2("Smith", null),
      new Tuple2("Smith", Tuple.HI)
    );
    //#z
  }
}
