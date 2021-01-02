package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;


public class performance_allocation {

    @Test
    public void run() throws IOException {
        File file = File.createTempFile("mapdb","mapdb");
        file.delete();
        //#a
        DB db = DBMaker
            .fileDB(file)
            .fileMmapEnable()
            .allocateStartSize( 10L * 1024*1024*1024)  // 10GB
            .allocateIncrement(512L * 1024*1024)       // 512MB
            .make();
        //#z
    }
}
