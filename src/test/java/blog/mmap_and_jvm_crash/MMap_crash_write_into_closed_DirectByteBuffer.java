package blog.mmap_and_jvm_crash;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;


import static blog.mmap_and_jvm_crash.MMap_crash_write_into_closed_MappedByteBuffer.unmap;

/**
 * JVM crash if there is a write into closed MappedByteBuffer
 */
public class MMap_crash_write_into_closed_DirectByteBuffer {


    public static void main(String[] args) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        //a
        byte[] buffer = new byte[1024*1024];

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(buffer.length);

        //write into buffer
        directBuffer.rewind();
        directBuffer.put(buffer, 0, buffer.length);

        //close ByteBuffer
        unmap(directBuffer);

        //now write into closed buffer, JVM will crash
        directBuffer.rewind();
        directBuffer.put(buffer, 0, buffer.length);
        //z
    }

}
