package doc;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;


public class dbmaker_basic_option {

    public static void main(String[] args) {
        //a
        DB db = DBMaker
                .fileDB("/some/file")
                //TODO encryption API
                //.encryptionEnable("password")
                .make();
        //z
    }
}
