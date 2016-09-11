package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentMap;

public class hello_world_serializers {

    @Test
    public void run() throws IOException {
        //#a
        DB db = DBMaker
                .fileDB("file.db")
                .fileMmapEnable()
                .make();
        ConcurrentMap<String,Long> map = db
                .hashMap("map", Serializer.STRING, Serializer.LONG)
                .createOrOpen();
        map.put("something", 111L);

        db.close();
        //#z
        //cleanup, not part of example
        new File("file.db").delete();
    }

}
