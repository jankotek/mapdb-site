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
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class btreemap_multimap {

    @Test
    public void multimap() {
        DB db = DBMaker.memoryDB().make();
        //a
        // initialize multimap: Map<String,List<Integer>>
        NavigableSet<Object[]> multimap = db.treeSet("towns")
                //set tuple serializer
                .serializer(new SerializerArrayTuple(Serializer.STRING, Serializer.INTEGER))
                .createOrOpen();

        // populate, key is first component in tuple (array), value is second
        multimap.add(new Object[]{"John",1});
        multimap.add(new Object[]{"John",2});
        multimap.add(new Object[]{"Anna",1});

        // print all values associated with John:
        Set johnSubset = multimap.subSet(
                new Object[]{"John"},         // lower interval bound
                new Object[]{"John", null});  // upper interval bound, null is positive infinity
        //z
        assertEquals(2,johnSubset.size());
    }
}
