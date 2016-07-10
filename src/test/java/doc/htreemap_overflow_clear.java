package doc;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


public class htreemap_overflow_clear {

    @Test
    public void run() throws IOException {

        Map onDisk = new HashMap(); //fake on disk map

        DB dbMemory = DBMaker
                .memoryDB()
                .make();

        // fast in-memory collection with limited size
        HTreeMap inMemory = dbMemory
                .hashMap("inMemory")
                .expireAfterCreate(1, TimeUnit.DAYS) //very long expiration
                .expireOverflow(onDisk)
                .create();
        //#a
        inMemory.put(1,11);
        inMemory.put(2,11);

        //expire entire content of inMemory Map
        inMemory.clearWithExpire();

        //#z
        assertEquals(0, inMemory.size());
        assertEquals(2, onDisk.size());
    }
}
