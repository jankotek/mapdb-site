package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerArrayTuple;

import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class btreemap_prefix_submap {

    @Test
    public void multimap() {
        DB db = DBMaker.memoryDB().make();
        //#a
        BTreeMap<byte[], Integer> map = db
                .treeMap("towns", Serializer.BYTE_ARRAY, Serializer.INTEGER)
                .createOrOpen();

        map.put("New York".getBytes(), 1);
        map.put("New Jersey".getBytes(), 2);
        map.put("Boston".getBytes(), 3);

        //get all New* cities
        Map<byte[], Integer> newCities = map.prefixSubMap("New".getBytes());
        //#z
        assertEquals(2,newCities.size());
    }
}
