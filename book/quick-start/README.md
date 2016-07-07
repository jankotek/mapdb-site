Quick Introduction
==================

MapDB is flexible, with many configuration options. But in most cases, it is configured with just a few lines of code.

*TODO more resources: cheat sheet, examples, KATA...*

Get it
------

MapDB binaries are hosted in Maven Central repository. Here is dependency fragment for MapDB.

```xml
<dependency>
    <groupId>org.mapdb</groupId>
    <artifactId>mapdb</artifactId>
    <version>VERSION</version>
</dependency>
```

`VERSION` is the last version number from [Maven Central](http://mvnrepository.com/artifact/org.mapdb/mapdb). You can also find the current version on this image:

<img src="https://maven-badges.herokuapp.com/maven-central/org.mapdb/mapdb/badge.svg"/>
Daily builds are in snapshot repository. The version number for the latest snapshot is [here](https://oss.sonatype.org/content/repositories/snapshots/org/mapdb/mapdb/).

```xml
<repositories>
    <repository>
        <id>sonatype-snapshots</id>
        <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.mapdb</groupId>
        <artifactId>mapdb</artifactId>
        <version>VERSION</version>
    </dependency>
</dependencies>
```

You can also download MapDB jar files directly from \[Maven Central\](<https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22mapdb%22>). In that case keep on mind that MapDB depends on Eclipse Collections, Guava, Kotlin library and some other libraries. Here is \[full list of dependencies\](<http://mvnrepository.com/artifact/org.mapdb/mapdb>) for each version.

Hello World
-----------

Hereafter is a simple example. It opens in-memory HashMap, it uses off-heap store and it is not limited by Garbage Collection:

<!--- #file#hello_world.java--->
```java
//import org.mapdb.*
DB db = DBMaker.memoryDB().make();
ConcurrentMap map = db.hashMap("map").make();
map.put("something", "here");
```
HashMap (and other collections) can be also stored in file. In this case the content can be preserved between JVM restarts. It is necessary to call `DB.close()` to protect file from data corruption. Other option is to enable transactions with write ahead log.

<!--- #file#hello_world_file.java--->
```java
DB db = DBMaker.fileDB("file.db").make();
ConcurrentMap map = db.hashMap("map").make();
map.put("something", "here");
db.close();
```
*TODO Hello World examples do not cover the commits.*

By default, MapDB uses generic serialization, which can serialize any data type. It is faster and more memory efficient to use specialized serializers. Also we can enable faster memory-mapped files on 64bit operating systems:

<!--- #file#hello_world_serializers.java--->
```java
DB db = DBMaker
        .fileDB("file.db")
        //TODO memory mapped files enable here
        .make();
ConcurrentMap<String,Long> map = db
        .hashMap("map", Serializer.STRING, Serializer.LONG)
        .make();
map.put("something", 111L);

db.close();
```
Example projects
----------------

*TODO example projects*

Quick Tips
----------

-   Memory mapped files are much faster and should be enabled on 64bit systems for better performance.
-   MapDB has Pump for fast bulk import of collections. It is much faster than to `Map.put()`
-   Transactions have a performance overhead, but without them the store gets corrupted if not closed properly.
-   Data stored in MapDB (keys and values) should be immutable. MapDB serializes objects on background.
-   MapDB needs compaction sometimes. Run `DB.compact()` or see background compaction options.

