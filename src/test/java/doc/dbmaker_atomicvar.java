package doc;

import org.mapdb.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public class dbmaker_atomicvar {

    static class Person{
        public static final Serializer<Person> SERIALIZER = new Serializer<Person>() {
            @Override
            public void serialize(DataOutput2 out, Person value) throws IOException {

            }

            @Override
            public Person deserialize(DataInput2 in, int available) throws IOException {
                return new Person();
            }
        } ;
    }

    public static void main(String[] args) {
        DB db = DBMaker
                .memoryDB()
                .make();
        //#a
        Atomic.Var<Person> var = db.atomicVar("mainPerson",Person.SERIALIZER).make();
        //#z
    }
}
