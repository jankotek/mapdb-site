package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;

import static org.junit.Assert.assertEquals;

public class btreemap_create_from_iterator {

    @Test
    public void create_from_iterator() {

      DB db = DBMaker.memoryDB().make();

      //#a
      // note that source data are sorted
      List<Integer> source = Arrays.asList(1,2,3,4,5,7,8);

      //create map with content from source
      NavigableSet<Integer> set = db.treeSet("set")
        .serializer(Serializer.INTEGER)
        .createFrom(source); //use `createFrom` instead of `create`
      //#z
      assertEquals(7, set.size());
    }
}
