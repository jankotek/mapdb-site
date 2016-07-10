package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.io.IOException;


public class performance_filechannel {

    @Test
    public void run() throws IOException {
        File file = File.createTempFile("mapdb","mapdb");
        file.delete();
        //#a
        DB db = DBMaker
            .fileDB(file)
            .fileChannelEnable()
            .make();
        //#z
    }
}
