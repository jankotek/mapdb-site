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
public class Channel_Crash {

    public static void main(String[] args) throws IOException {
        byte[] buffer = new byte[1024*1024];

        //a
        FileChannel channel = FileChannel.open(file.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.READ);

        ByteBuffer mappedBuffer = channel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                buffer.length);

        //write into buffer in infinitive cycle
        while(true){
            mappedBuffer.rewind();
            //this causes JVM crash if delayed write fails
            mappedBuffer.put(buffer, 0, buffer.length);
        }
        //z
    }
}
