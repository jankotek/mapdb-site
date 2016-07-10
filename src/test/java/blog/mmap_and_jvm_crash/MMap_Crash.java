package blog.mmap_and_jvm_crash;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * Demonstrates JVM crash on continuous allocation
 * when free disk space runs out.
 */
public class MMap_Crash {

    static File file = new File("/disk/fs/store");

    public static void main(String[] args) throws IOException {

        //#a
        byte[] buffer = new byte[1024*1024];

        FileChannel channel = FileChannel.open(file.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.READ);

        //allocate in infinite cycle
        for(long offset=0; ; offset+=buffer.length){
            ByteBuffer mappedBuffer = channel.map(
                    FileChannel.MapMode.READ_WRITE,
                    offset,
                    offset+buffer.length);

            //this causes JVM crash if there is no free disk space and delayed write fails
            mappedBuffer.put(buffer, 0, buffer.length);
        }
        //#z
    }
}
