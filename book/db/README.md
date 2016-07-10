DB and DBMaker
==============

MapDB is pluggable like Lego. There are two classes that act like the glue between the different pieces, namely the `DBMaker` and the `DB` classes.

The [DBMaker](http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-d-b-maker/index.html) class handles database configuration, creation and opening. MapDB has several modes and configuration options. Most of those can be set using this class.

A [DB](http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-d-b/index.html) instance represents an opened database (or a single transaction session). It can be used to create and open collection storages. It can also handle the database's lifecycle with methods such as `commit()`, `rollback()` and `close()`.

To open (or create) a store, use one of the `DBMaker.xxxDB()` static methods. MapDB has more formats and modes, whereby each `xxxDB()` uses different modes: `memoryDB()` opens an in-memory database backed by a `byte[]` array, `appendFileDB()` opens a database which uses append-only log files and so on.

A `xxxDB()` method is followed by one or several configuration options and finally a `make()` method which applies all options, opens the selected storage and returns a `DB` object. This example opens a file storage with encryption enabled:

<!--- #file#doc/dbmaker_basic_option.java--->
```java
DB db = DBMaker
        .fileDB("/some/file")
        //TODO encryption API
        //.encryptionEnable("password")
        .make();
```le#dbmaker_basic_option.java