package doc;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.mapdb.tuple.Tuple2;
import org.mapdb.tuple.Tuple2Serializer;

import java.io.Serializable;
import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;

public class btreemap_tuple_age2 {

  static class Firstname implements Serializable, Comparable<Firstname> {
    final String firstname;

    Firstname(String firstname) {
      this.firstname = firstname;
    }


    @Override
    public int compareTo(Firstname o) {
      return this.firstname.compareTo(o.firstname);
    }
  }

  static class Surname implements Serializable, Comparable<Surname>{
    final String surname;

    Surname(String surname) {
      this.surname = surname;
    }

    @Override
    public int compareTo(@NotNull Surname o) {
      return this.surname.compareTo(o.surname);
    }
  }

  @Test
  public void test(){
    DB db = DBMaker.memoryDB().make();
    //#a
    //create new map
    BTreeMap<Tuple2<Surname, Firstname>, Integer> persons = db
      .treeMap("persons")
      .keySerializer(new Tuple2Serializer()) //specialized tuple serializer
      .valueSerializer(Serializer.INTEGER)
      .createOrOpen();

    //insert three person into map
    persons.put(new Tuple2(new Surname("Smith"),new Firstname("John")), 45);
    persons.put(new Tuple2(new Surname("Smith"),new Firstname("Peter")), 37);
    persons.put(new Tuple2(new Surname("Doe"),new Firstname("John")), 70);

    //now lets get map which contains all Smiths
    NavigableMap<Tuple2<Surname, Firstname>,Integer> smiths =
      persons.prefixSubMap(
        new Tuple2(new Surname("Smith"), null)  //null indicates
      );
    //#z

    assertEquals(2, smiths.size());
  }
}
