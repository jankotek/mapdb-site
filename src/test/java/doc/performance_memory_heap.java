package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;


public class performance_memory_heap {

    @Test
    public void run() throws IOException {
        //a
        DB db = DBMaker
            .heapDB()
            .make();
        //z
    }
}
