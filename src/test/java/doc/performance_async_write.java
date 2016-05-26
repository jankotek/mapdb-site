package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;


public class performance_async_write {

    @Test
    public void run(){
        //a
        DB db = DBMaker
            .memoryDB()
            //TODO async writes example
            //.asyncWriteEnable()
            //.asyncWriteQueueSize(10000) //optionally change queue size
            .executorEnable()   //enable background threads to flush data
            .make();
        //z
    }
}
