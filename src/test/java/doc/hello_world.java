package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

public class hello_world {

    @Test
    public void run() throws IOException {
        //#a
        //import org.mapdb.*
        DB db = DBMaker.memoryDB().make();
        ConcurrentMap map = db.hashMap("map").createOrOpen();
        map.put("something", "here");
        //#z
    }

}
