package blog.mmap_and_jvm_crash;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import static blog.mmap_and_jvm_crash.MMap_Crash.file;

/**
 * Demonstrates JVM crash on continuous allocation
 * when free disk space runs out.
 */
public class MMap_Crash_Preclear {

    public static void main(String[] args) throws IOException {
        //#a
        byte[] buffer = new byte[1024*1024];

        FileChannel channel = FileChannel.open(file.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.READ);

        // allocate in infinite cycle
        for(long offset=0; ; offset+=buffer.length){

            //preclear space before it is mapped, so there is no sparse file
            ByteBuffer preclearBuffer = ByteBuffer.wrap(buffer);
            while(preclearBuffer.remaining()>0) {
                channel.write(preclearBuffer, offset);
            }
            //and allocate extra a bit of extra space,
            preclearBuffer = ByteBuffer.wrap(buffer, 0, 16);
            while(preclearBuffer.remaining()>0) {
                channel.write(preclearBuffer, offset+buffer.length);
            }

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
