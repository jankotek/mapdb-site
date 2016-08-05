package doc;

import kotlin.Pair;
import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class btreemap_create_map_from_iterator {

    @Test
    public void create_from_iterator() {

      DB db = DBMaker.memoryDB().make();

      //#a
      // source data, first entry in pair is key, second is value
      // note that source data are sorted
      List<Pair<Integer,Integer>> source =
          Arrays.asList(new Pair(1,2),new Pair(3,4),new Pair(5,7));

      //create map with content from source
      BTreeMap<Integer, Integer> map = db.treeMap("map")
        .keySerializer(Serializer.INTEGER)
        .valueSerializer(Serializer.INTEGER)
        .createFrom(source); //use `createFrom` instead of `create`

      //we can also use another source map as a source
      db.treeMap("map2")
        .keySerializer(Serializer.INTEGER)
        .valueSerializer(Serializer.INTEGER)
        .createFrom(map);
      //#z
      assertEquals(3, map.size());
    }
}
