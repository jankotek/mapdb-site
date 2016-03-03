package doc;

import org.junit.Test;
import org.mapdb.volume.ByteArrayVol;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class sortedtablemap_volume {

    @Test
    public void run() throws IOException {
        //a
        //create in-memory volume over byte[]
        Volume byteArrayVolume = ByteArrayVol.FACTORY.makeVolume(null, false);

        //create in-memory volume in direct memory using DirectByteByffer
        Volume offHeapVolume = Volume.MemoryVol.FACTORY.makeVolume(null, false);

        File file = File.createTempFile("mapdb","mapdb");
        //create memory mapped file volume
        Volume mmapVolume = MappedFileVol.FACTORY.makeVolume(file.getPath(), false);

        //or if data were already imported, create it read-only
        mmapVolume.close();
        mmapVolume = MappedFileVol.FACTORY.makeVolume(file.getPath(), true);
                                                                          //read-only=true
        //z
    }

}
