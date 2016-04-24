package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.RandomAccessFileVol;
import org.mapdb.volume.Volume;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

public class volume_db {

    @Test
    public void run() throws IOException {
        //a
        File f = File.createTempFile("some","file");
        Volume volume = MappedFileVol.FACTORY.makeVolume(f.getPath(),false);

        boolean contentAlreadyExists = false;
        DB db = DBMaker.volumeDB(volume, contentAlreadyExists).make();
        //z
    }

}
