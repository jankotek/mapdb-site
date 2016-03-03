package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.mapdb.serializer.SerializerCompressionWrapper;


public class htreemap_compressed {

    @Test
    public void run() {

        DB db = DBMaker.memoryDB().make();
        //a
        HTreeMap<Long, String> map = db.hashMap("map")
                .valueSerializer(
                        new SerializerCompressionWrapper(Serializer.STRING))
                .create();
        //z
        //TODO add Serializer.compressed() method?
    }
}
