Batch Import in BTreeMap
---------------------------

BTreeMap (as any other b-tree) suffers from write amplification. Single entry update has to traverse tree,
and modify entire tree node. Inserting many entries might be too slow.

There are two solutions for this problem. Write cache improves write amplification if updated entries are near each other. 
Tree node is updated many times in cache, but written only once when cache is flushed. 

Another solution is to import BTreeMap from sorted stream (in older versions called Data Pump).
It takes sorted stream of entries, and creates tree structure directly from stream.
 
Stream import does not use random IO, only sequential write. Nodes are never modified, only created. 
So in practice it imports BTreeMap at rate 50 MB/s. Also import speed does not degrade as btree becomes larger (`N*log(N)`), 
it can create multi-TB b-trees on spinning disk in just a few hours.  

Only downside is that imported data needs to be **sorted in ascending order**. Older MapDB version required data sorted in reversed descending order, that is solved in 3.0.

Here we create TreeSet from sorted iterator:
<!--- #file#doc/btreemap_create_from_iterator.java--->
```java
      // note that source data are sorted
      List<Integer> source = Arrays.asList(1,2,3,4,5,7,8);

      //create map with content from source
      NavigableSet<Integer> set = db.treeSet("set")
.serializer(Serializer.INTEGER)
.createFrom(source); //use `createFrom` instead of `create`
```

It is also possible to import SortedMap from an iterator. In this case we need to use iterator of `Pair(key,value)`,
or another sorted Map as a source.

<!--- #file#doc/btreemap_create_map_from_iterator.java--->
```java
      // source data, first entry in pair is key, second is value
      // note that source data are sorted
      List<Pair<Integer,Integer>> source =
  Arrays.asList(new Pair(1,2),new Pair(3,4),new Pair(5,7));

      //create map with content from source
      BTreeMap<Integer, Integer> map = db.treeMap("map")
.keySerializer(Serializer.INTEGER)
.valueSerializer(Serializer.INTEGER)
.createFrom(source); //use `createFrom` instead of `create`

      //we can also use another source map as a source
      db.treeMap("map2")
.keySerializer(Serializer.INTEGER)
.valueSerializer(Serializer.INTEGER)
.createFrom(map);
```

###Create using Sink

Some data are not available in collections or as an iterator, for example when you are receive data in packets or read file line by line. For that case MapDB provides a Sink, an callback class which accepts 
entries and has finish method. Here is an example which fills Map using for-loop:

<!--- #file#doc/btreemap_create_from_sink.java--->
```java
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

```