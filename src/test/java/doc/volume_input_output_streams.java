package doc;

import org.junit.Test;
import org.mapdb.volume.ByteArrayVol;
import org.mapdb.volume.Volume;

import java.io.*;

public class volume_input_output_streams {

    @Test
    public void run() throws IOException {
        //a
        InputStream input = new FileInputStream("pom.xml");

        Volume volume = new ByteArrayVol();
        volume.copyFrom(input);

        OutputStream out = new ByteArrayOutputStream();
        volume.copyTo(out);
        //z
    }

}
