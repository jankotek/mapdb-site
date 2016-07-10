package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.IOException;


public class performance_memory_direct {

    @Test
    public void run() throws IOException {
        //#a
        // run with: java -XX:MaxDirectMemorySize=10G
        DB db = DBMaker
            .memoryDirectDB()
            .make();
        //#z
    }
}
