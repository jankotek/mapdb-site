package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;


public class htreemap_noserializer {

    @Test public void run() {
        DB db = DBMaker.memoryDB().make();
        //#a
        HTreeMap map = db
                .hashMap("name_of_map")
                .create();
        //#z
    }
}
