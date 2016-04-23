package blog.mmap_and_jvm_crash;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import static blog.mmap_and_jvm_crash.MMap_Crash.file;

/**
 * JVM crash if there is a write into closed MappedByteBuffer
 */
public class MMap_crash_write_into_closed_MappedByteBuffer {


    public static void main(String[] args) throws IOException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //a
        byte[] buffer = new byte[1024*1024];

        FileChannel channel = FileChannel.open(file.toPath(),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.READ);

        MappedByteBuffer mappedBuffer = channel.map(
                FileChannel.MapMode.READ_WRITE,
                0,
                buffer.length);

        //write into buffer
        mappedBuffer.rewind();
        mappedBuffer.put(buffer, 0, buffer.length);

        //close MappedByteBuffer
        unmap(mappedBuffer);

        //now write into closed buffer, JVM will crash
        mappedBuffer.rewind();
        mappedBuffer.put(buffer, 0, buffer.length);
        //z
    }

    static void unmap(ByteBuffer b) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // need to dispose old direct buffer, see bug
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724038
        Method cleanerMethod = b.getClass().getMethod("cleaner", new Class[0]);
        cleanerMethod.setAccessible(true);
        if(cleanerMethod!=null){
            Object cleaner = cleanerMethod.invoke(b);
            if(cleaner!=null){
                Method clearMethod = cleaner.getClass().getMethod("clean", new Class[0]);
                if(clearMethod!=null) {
                    clearMethod.invoke(cleaner);
                }
            }else{
                //cleaner is null, try fallback method for readonly buffers
                Method attMethod = b.getClass().getMethod("attachment", new Class[0]);
                attMethod.setAccessible(true);
                Object att = attMethod.invoke(b);
                unmap((MappedByteBuffer) att);
            }
        }
    }
}
