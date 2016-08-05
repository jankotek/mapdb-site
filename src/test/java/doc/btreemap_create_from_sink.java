package doc;

import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import static org.junit.Assert.assertEquals;

public class btreemap_create_from_sink {

    @Test
    public void create_from_iterator() {

      DB db = DBMaker.memoryDB().make();

      //#a
      //create sink
      DB.TreeMapSink<Integer,String> sink = db
        .treeMap("map", Serializer.INTEGER,Serializer.STRING)
        .createFromSink();

      //loop and pass data into map
      for(int lineNum=0;lineNum<10000;lineNum++){
        String line = "some text from file"+lineNum;
        //add key and value into sink, keys must be added in ascending order
        sink.put(lineNum, line);
      }
      // Sink is populated, map was created on background
      // Close sink and return populated map
      BTreeMap<Integer,String> map = sink.create();

      //#z
      assertEquals(10000, map.size());
    }
}
