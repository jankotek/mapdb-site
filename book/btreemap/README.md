BTreeMap
========

`BTreeMap` provides `TreeMap` and `TreeSet` for MapDB. It is based on lock-free concurrent B-Linked-Tree. It offers great performance for small keys and has good vertical scalability.

TODO explain compressions

TODO describe B-Linked-Tree


Parameters
----------

BTreeMap has optional parameters which can be specified with the use of a maker.:

The most important among them are **serializers**. General serialization has some guessing and overhead, so better performance is always achieved if more specific serializers are used. To specify the key and value serializer, use the code bellow. There are dozens ready to use serializers available as static fields on `Serializer` interface:

<!--- #file#doc/btreemap_serializer.java--->
```java
BTreeMap<Long, String> map = db.treeMap("map")
        .keySerializer(Serializer.LONG)
        .valueSerializer(Serializer.STRING)
        .createOrOpen();
```
Another useful parameter is the **size counter**. By default, a BTreeMap does not keep track of its size and calling `map.size()` requires a linear scan to count all entries. If you enable size counter, in that case `map.size()` is instant, but there is some overhead on the inserts.

BTrees store all their keys and values as part of a btree node. Node size affects the performance a lot. A large node means that many keys have to be deserialized on lookup. A smaller node loads faster, but makes large BTrees deeper and requires more operations. The default maximal node size is 32 entries and it can be changed in this way:

<!--- #file#doc/btreemap_counter.java--->
```java
BTreeMap<Long, String> map = db
        .treeMap("map", Serializer.LONG, Serializer.STRING)
        .counterEnable()
        .createOrOpen();
```
Values are also stored as part of BTree leaf nodes. Large value means huge overhead and on a single `map.get("key")` 32 values are deserialized, but only a single value returned. In this case, it is better to store the values outside the leaf node, in a separate record. In this case, the leaf node only has a 6 byte recid pointing to the value.

Large values can also be compressed to save space. This example stores values
outside BTree Leaf Node and applies compression on each value:

<!--- #file#doc/btreemap_compressed.java--->
```java
BTreeMap<Long, String> map = db.treeMap("map")
        .valuesOutsideNodesEnable()
        .valueSerializer(new SerializerCompressionWrapper(Serializer.STRING))
        .createOrOpen();
```
BTreeMap needs to sort its key somehow. By default it relies on the `Comparable` interface implemented by most Java classes. In case this interface is not implemented, a key serializer must be provided. One can for example compare Object arrays:

<!--- #file#doc/btreemap_object_array.java--->
```java
BTreeMap<Object[], Long> map = db.treeMap("map")
        // use array serializer for unknown objects
        // TODO db.getDefaultSerializer()
        .keySerializer(new SerializerArray(Serializer.JAVA))
        // or use serializer for specific objects such as String
        .keySerializer(new SerializerArray(Serializer.STRING))
        .createOrOpen();
```
Also primitive arrays can be used as keys. One can replace `String` by `byte[]`, which directly leads to better performance:

<!--- #file#doc/btreemap_byte_array.java--->
```java
BTreeMap<byte[], Long> map = db.treeMap("map")
        .keySerializer(Serializer.BYTE_ARRAY)
        .valueSerializer(Serializer.LONG)
        .createOrOpen();
```
Key serializers
---------------

BTreeMap owns its performance to the way it handles keys. Let's illustrate this on an example with `Long` keys.

A long key occupies 8 bytes after serialization. To minimize the space usage one could pack this value to make it smaller. So the number 10 will occupy a single byte, 300 will take 2 bytes, 10000 three bytes etc. To make keys even more packable, we need to store them in even smaller values. The keys are sorted, so lets use delta compression. This will store the first value in full form and then only the differences between consecutive numbers.

Another improvement is to make the deserialization faster. In normal `TreeMap` the keys are stored in awrapped form, such as `Long[]`. That has a huge overhead, as each key requires a new pointer, class header... BTreeMap will store keys in primitive array `long[]`. And finally if keys are small enough it can even fit into `int[]`. And because an array has better memory locality, there is a huge performance increase on binary searches.

It is simple to do such optimisation for numbers. But BTreeMap also applies that on other keys, such as `String` (common prefix compression,single `byte[]` with offsets), `byte[]`, `UUID`, `Date` etc.

This sort of optimization is used automatically. All you have to do is provide the specialized key serializer: `.keySerializer(Serializer.LONG)`.

There are several options and implementations to pack keys. Have a look at static fields with `_PACK` suffix in [Serializer](http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-serializer/index.html) class for more details.

TODO this is a major feature, document details and add benchmarks

Data Pump
---------

TODO data pump

Fragmentation
-------------

A trade-off for lock-free design is fragmentation after deletion. The B-Linked-Tree does not delete btree nodes after entry removal, once they become empty. If you fill a BTreeMap and then remove all entries, about 40% of space will not be released. Any value updates (keys are kept) are not affected by this fragmentation.

This fragmentation is different from storage fragmentation, so `DB.compact()` will not help. A solution is to move all the content into a new `BTreeMap`. As it is very fast with Data Pump streaming, the new Map will have zero fragmentation and better node locality (in theory disk cache friendly).

TODO provide utils to move BTreeMap content TODO provide statistics to calculate BTreeMap fragmentation

In the future, we will provide BTreeMap wrapper, which will do this compaction automatically. It will use three collections: the first `BTreeMap` will be read-only and will also contain the data. The second small map will contain updates. Periodically a third map will be produced as a merge of the first two, and will be swapped with the primary. `SSTable`'s in Cassandra and other databases work in a similar way.

TODO provide wrapper to compact/merge BTreeMap content automatically.

Prefix submaps
--------------

For array based keys (tuples, Strings, or arrays) MapDB provides prefix submap. It uses intervals, so prefix submap is lazy, it does not load all keys. Here as example which uses prefix on `byte[]` keys:

<!--- #file#doc/btreemap_prefix_submap.java--->
```java
BTreeMap<byte[], Integer> map = db
        .treeMap("towns", Serializer.BYTE_ARRAY, Serializer.INTEGER)
        .createOrOpen();

map.put("New York".getBytes(), 1);
map.put("New Jersey".getBytes(), 2);
map.put("Boston".getBytes(), 3);

//get all New* cities
Map<byte[], Integer> newCities = map.prefixSubMap("New".getBytes());
```
TODO key serializer must provide `nextValue` for prefix submaps. Implement it on more serializers

Composite keys and tuples
-------------------------

MapDB allows composite keys in the form of `Object[]`. Interval submaps can be used to fetch tuple subcomponents, or to create a simple form of multimap. Object array is not comparable, so you need to use specialized serializer which provides comparator.

Here is an example which creates `Map<Tuple3<String, String, Integer>, Double>` in the form of Object\[\]. First component is town, second is street and third component is house number. It has more parts, source code is on [github](https://github.com/jankotek/mapdb-site/blob/master/src/test/java/doc/btreemap_composite_keys.java) To serialize and compare tuples use`SerializerArrayTuple` which takes serializer for each tuple component as s constructor parameter:

<!--- #file#doc/btreemap_composite_keys.java#java#1--->
```java
// initialize db and map
DB db = DBMaker.memoryDB().make();
BTreeMap<Object[], Integer> map = db.treeMap("towns")
        .keySerializer(new SerializerArrayTuple(
                Serializer.STRING, Serializer.STRING, Serializer.INTEGER))
        .valueSerializer(Serializer.INTEGER)
        .createOrOpen();
```
Once map is populated we can get all houses in the town of Cong by using prefix submap (town is first component in tuple):

<!--- #file#doc/btreemap_composite_keys.java#java#2--->
```java
//get all houses in Cong (town is primary component in tuple)
Map<Object[], Integer> cong =
        map.prefixSubMap(new Object[]{"Cong"});
```
The prefix submap is equal to the range query which uses submap method:

Interval submap can only filter components on the left side. To get components in the middle we have to combine the submap with a forloop:

<!--- #file#doc/btreemap_composite_keys.java#java#3--->
```java
cong = map.subMap(
        new Object[]{"Cong"},           //shorter array is 'negative infinity'
        new Object[]{"Cong",null,null} // null is positive infinity'
);
```
Submaps are modifiable, so we could delete all the houses within a town by calling clear() on submap etc..

Multimap
--------

Multimap is a Map which associates multiple values with a single key. An example can be found in [Guava](http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/collect/Multimap.html) or in [Eclipse Collections](https://www.eclipse.org/collections/javadoc/7.0.0/org/eclipse/collections/api/multimap/Multimap.html) It can be written as Map&lt;Key,List&lt;Value&gt;&gt;, but that does not work well in MapDB, we need keys and values to be immutable, and List is not immutable.

There is a plan to implement Multimap from Guava and EC directly in MapDB. But until then there is an option to use SortedSet in combination with tuples and interval subsets. Here is an example which constructs Set, inserts some data and gets all values (second tuple component) associated with key (first tuple component):

<!--- #file#doc/btreemap_multimap.java--->
```java
// initialize multimap: Map<String,List<Integer>>
NavigableSet<Object[]> multimap = db.treeSet("towns")
        //set tuple serializer
        .serializer(new SerializerArrayTuple(Serializer.STRING, Serializer.INTEGER))
        .counterEnable()
        .counterEnable()
        .counterEnable()
        .createOrOpen();

// populate, key is first component in tuple (array), value is second
multimap.add(new Object[]{"John",1});
multimap.add(new Object[]{"John",2});
multimap.add(new Object[]{"Anna",1});

// print all values associated with John:
Set johnSubset = multimap.subSet(
        new Object[]{"John"},         // lower interval bound
        new Object[]{"John", null});  // upper interval bound, null is positive infinity
```
TODO delta packing for Tuples

TODO MapDB will soon implement multimap from Guava

Compared to HTreeMap
--------------------

BTreeMap is better for smaller keys, such as numbers and short strings.

TODO compare to HTreeMap
