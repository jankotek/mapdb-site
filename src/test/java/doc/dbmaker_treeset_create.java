package doc;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.NavigableSet;


public class dbmaker_treeset_create {

    public static void main(String[] args) {
        DB db = DBMaker
                .memoryDB()
                .make();
        //#a
        NavigableSet<String> treeSet = db
                .treeSet("treeSet")
                .maxNodeSize(112)
                .serializer(Serializer.STRING)
                .make();
        //#z
    }
}
