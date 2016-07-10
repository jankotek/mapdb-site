package doc;

import org.junit.Test;
import org.mapdb.*;
import org.mapdb.serializer.SerializerArrayTuple;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class btreemap_composite_keys {

    @Test
    public void irish_towns() {

        //#a1
        // initialize db and map
        DB db = DBMaker.memoryDB().make();
        BTreeMap<Object[], Integer> map = db.treeMap("towns")
                .keySerializer(new SerializerArrayTuple(
                        Serializer.STRING, Serializer.STRING, Serializer.INTEGER))
                .valueSerializer(Serializer.INTEGER)
                .createOrOpen();
        //#z1

        //initial values
        String[] towns = {"Galway", "Ennis", "Gort", "Cong", "Tuam"};
        String[] streets = {"Main Street", "Shop Street", "Second Street", "Silver Strands"};
        int[] houseNums = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

        for (String town : towns)
            for (String street : streets)
                for (int house : houseNums) {
                    int income = 30000;
                    map.put(new Object[]{town, street, house}, income);
                }

        //#a2
        //get all houses in Cong (town is primary component in tuple)
        Map<Object[], Integer> cong =
                map.prefixSubMap(new Object[]{"Cong"});
        //#z2
        assertEquals(houseNums.length*streets.length, cong.size());

        //#a3
        cong = map.subMap(
                new Object[]{"Cong"},           //shorter array is 'negative infinity'
                new Object[]{"Cong",null,null} // null is positive infinity'
        );
        //#z3
        assertEquals(houseNums.length*streets.length, cong.size());

        //#a4
        int total = 0;
        for(String town:towns){ //first loop iterates over towns
            for(Integer salary: //second loop iterates over all houses on main street
                    map.prefixSubMap(new Object[]{town, "Main Street"}).values()){
                total+=salary; //and calculate sum
            }
        }
        System.out.println("Salary sum for all Main Streets is: "+total);
        //#z4
        assertEquals(30000*towns.length*houseNums.length, total);
    }
}
