package doc;

import org.junit.Test;
import org.mapdb.volume.ByteArrayVol;
import org.mapdb.volume.MappedFileVol;
import org.mapdb.volume.Volume;

import java.io.IOException;

public class volume_load_from_file {

    @Test
    public void run() throws IOException {
        //a
        //open file volume
        String file = "pom.xml";
        Volume fileVolume = MappedFileVol.FACTORY.makeVolume(file,false);

        //create in memory volume
        Volume memoryVolume = new ByteArrayVol();

        //and copy content from file to memory
        fileVolume.copyTo(memoryVolume);
        //z
        fileVolume.close();
    }

}
