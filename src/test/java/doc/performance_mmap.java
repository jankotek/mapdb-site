package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Store;

import java.io.File;
import java.io.IOException;


public class performance_mmap {

    @Test
    public void run() throws IOException {
        File file = File.createTempFile("mapdb","mapdb");
        file.delete();
        //#a
        DB db = DBMaker
            .fileDB(file)
            .fileMmapEnable()            // Always enable mmap
            .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
            .fileMmapPreclearDisable()   // Make mmap file faster

                // Unmap (release resources) file when its closed.
                // That can cause JVM crash if file is accessed after it was unmapped
                // (there is possible race condition).
            .cleanerHackEnable()
            .make();

        //optionally preload file content into disk cache
        db.getStore().fileLoad();
        //#z
    }
}
