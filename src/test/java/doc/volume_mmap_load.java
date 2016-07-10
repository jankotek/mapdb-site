package doc;

import org.junit.Test;
import org.mapdb.volume.MappedFileVol;

import java.io.IOException;

public class volume_mmap_load {

    @Test
    public void run() throws IOException {
        //#a
        //open memory mapped file
        String file = "pom.xml";
        MappedFileVol fileVolume = (MappedFileVol) MappedFileVol.FACTORY.makeVolume(file,false);

        //and ensure its content is cached in memory
        fileVolume.fileLoad();
        //#z
        fileVolume.close();
    }

}
