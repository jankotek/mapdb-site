Sorted Table Map
================

`SortedTableMap` is inspired by Sorted String Tables from Cassandra. It stores keys in file (or memory store) in fixed size table, and uses binary search. There are some tricks to support variable-length entries and to decrease space usage. Compared to `BTreeMap` it is faster, has zero fragmentation, but is readonly.

`SortedTableMap` is read-only and does not support updates. Changes should be applied by creating new Map with Data Pump. Usually one places change into secondary map, and periodically merges two maps into new `SortedTableMap`.

`SortedTableMap` is read-only. Its created and filled with content by Data Pump and Consumer:

<!--- #file#doc/sortedtablemap_init.java--->
```java
//create memory mapped volume
Volume volume = MappedFileVol.FACTORY.makeVolume(file, false);

//open consumer which will feed map with content
SortedTableMap.Sink<Integer,String> sink =
        SortedTableMap.create(
                volume,
                Serializer.INTEGER,
                Serializer.STRING
        ).createFromSink();

//feed content into consumer
for(int key=0; key<100000; key++){
    sink.put(key, "value"+key);
}

// finally open created map
SortedTableMap<Integer, String> map = sink.create();
```
Once file is created, it can be reopened:

<!--- #file#doc/sortedtablemap_reopen.java--->
```java
//open existing  memory-mapped file in read-only mode
Volume volume = MappedFileVol.FACTORY.makeVolume(file, true);
                                                         //read-only=true

SortedTableMap<Integer,String> map =
        SortedTableMap.open(
                volume,
                Serializer.INTEGER,
                Serializer.STRING
                );
```
Binary search
-------------

Storage is split into pages. Page size is power of two, with maximal size 1MB. First key on each page is stored on-heap.

Each page contains several nodes composed of keys and values. Those are very similar to BTreeMap Leaf nodes. Node offsets are known, so fast seek to beginning of node is used.

Each node contains several key-value pairs (by default 32). Their organization depends on serializer, but typically are compressed together (delta compression, LZF..) to save space. So to find single entry, one has to load/traverse entire node. Some fixed-length serializer (Serializer.LONG...) do not have to load entire node to find single entry.

Binary search on `SortedTableMap` is performed in three steps:

-   First key for each page is stored on-heap in an array. So perform binary search to find page.
-   First key on each node can by loaded without decompressing entire node. So perform binary search over first keys on each node
-   Now we know node, so perform binary search over node keys. This depends on Serializer. Usually entire node is loaded, but other options are possible TODO link to serializer binary search.

Parameters
----------

`SortedTableMap` takes key **serializer** and value serializers. The keys and values are stored together inside Value Array TODO link to serializers. They can be compressed together to save space. Serializer is trade-off between space usage and performance.

Another setting is **Page Size**. Default and maximal value is 1MB. Its value must be power of two, other values are rounded up to nearest power of two. Smaller value typically means faster access. But for each page one key is stored on-heap, smaller Page Size also means larger memory usage.

And finally there is **Node Size**. It has similar implications as BTreeMap node size. Larger node means better compression, since large chunks are better compressible. But it also means slower access times, since more entries are loaded to get single entry. Default node size is 32 entries, it should be lowered for large values.

Parameters are set following way

<!--- #file#doc/sortedtablemap_params.java--->
```java
//create memory mapped volume
Volume volume = MappedFileVol.FACTORY.makeVolume(file, false);

//open consumer which will feed map with content
SortedTableMap.Sink<Integer,String> sink =
        SortedTableMap.create(
                volume,
                Serializer.INTEGER, // key serializer
                Serializer.STRING   // value serializer
        )
                .pageSize(64*1024) // set Page Size to 64KB
                .nodeSize(8)       // set Node Size to 8 entries
                .createFromSink();

//feed content into consumer
for(int key=0; key<100000; key++){
    sink.put(key, "value"+key);
}

// finally open created map
SortedTableMap<Integer, String> map = sink.create();
volume.close();

// Existing SortedTableMap can be reopened.
// In that case only Serializers needs to be set,
// other params are stored in file
volume = MappedFileVol.FACTORY.makeVolume(file, true);
                                                     // read-only=true
map = SortedTableMap.open(volume, Serializer.INTEGER, Serializer.STRING);
```
Volume
------

`SortedTableMap` does not use `DB` object, but operates directly on `Volume` (MapDB abstraction over ByteBuffer). Following example show how to construct various `Volume` using in-memory byte array or memory-mapped file:

<!--- #file#doc/sortedtablemap_volume.java--->
```java
//create in-memory volume over byte[]
Volume byteArrayVolume = ByteArrayVol.FACTORY.makeVolume(null, false);

//create in-memory volume in direct memory using DirectByteByffer
Volume offHeapVolume = ByteBufferMemoryVol.FACTORY.makeVolume(null, false);

File file = File.createTempFile("mapdb","mapdb");
//create memory mapped file volume
Volume mmapVolume = MappedFileVol.FACTORY.makeVolume(file.getPath(), false);

//or if data were already imported, create it read-only
mmapVolume.close();
mmapVolume = MappedFileVol.FACTORY.makeVolume(file.getPath(), true);
                                                                  //read-only=true
```
Volume is than passed to `SortecTableMap` factory method as an parameter. It is recommended to open existing Volumes in read-only mode (last param is `true`) to minimize file locking and simplify your code.

Data Pump sync Volume content to disk, so file based `SortedTableMap` is durable once the `Consumer.finish()` method exits
