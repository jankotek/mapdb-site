DB and DBMaker
==============

MapDB is pluggable like Lego. There are two classes that act like the glue between the different pieces, namely the `DBMaker` and the `DB` classes.

The [DBMaker](http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-d-b-maker/index.html) class handles database configuration, creation and opening. MapDB has several modes and configuration options. Most of those can be set using this class.

A [DB](http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-d-b/index.html) instance represents an opened database (or a single transaction session). It can be used to create and open collection storages. It can also handle the database's lifecycle with methods such as `commit()`, `rollback()` and `close()`.

To open (or create) a store, use one of the many `*DB` static method such as `DBMaker.fileDB()`. MapDB has more formats and modes, whereby each `xxxDB()` uses different modes: `memoryDB()` opens an in-memory database backed by a `byte[]` array, `appendFileDB()` opens a database which uses append-only log files and so on.

A `xxxDB()` method is followed by one or several configuration options and finally a `make()` method which applies all options, opens the selected storage and returns a `DB` object. This example opens a file storage with encryption enabled:

<!--- #file#doc/dbmaker_basic_option.java--->
```java
DB db = DBMaker
        .fileDB("/some/file")
        //TODO encryption API
        //.encryptionEnable("password")
        .make();
```

Open and create collection
------------------------------

Once you have DB you may open a collection or other record. DB uses builder style configuration.
It starts with type of collection (`hashMap`, `treeSet`...) and name, followed by configuration is applied and finally
by operation indicator

This example opens (or creates new) TreeSet named 'example' 

<!--- #file#doc/dbmaker_treeset.java--->
```java
NavigableSet treeSet = db.treeSet("example").createOrOpen();
```

You could also apply additional configuration:

<!--- #file#doc/dbmaker_treeset_create.java--->
```java
NavigableSet<String> treeSet = db
        .treeSet("treeSet")
        .maxNodeSize(112)
        .serializer(Serializer.STRING)
        .create();
```
The builder can end with three different methods: 

 - `create()` will create new collection, and throws an exception if collection already exists
 - `open()` opens existing collection, and throws an exception if it does not exist
 - `createOrOpen()` opens existing collection if it exists, or else creates it. 

`DB` is not limited to collections, but creates other type of records such as Atomic Records:

<!--- #file#doc/dbmaker_atomicvar.java--->
```java
Atomic.Var<Person> var = db.atomicVar("mainPerson",Person.SERIALIZER).make();
```
 
Transactions
--------------

`DB` has methods to handle a transaction lifecycle: `commit()`, `rollback()` and `close()`.

One `DB` object represents single transaction. The example above uses single global transaction per store, which is sufficient for some usages:

<!--- #file#doc/dbmaker_basic_tx.java--->
```java
ConcurrentNavigableMap<Integer,String> map = db
        .treeMap("collectionName", Serializer.INTEGER, Serializer.STRING)
        .make();

map.put(1,"one");
map.put(2,"two");
//map.keySet() is now [1,2] even before commit

db.commit();  //persist changes into disk

map.put(3,"three");
//map.keySet() is now [1,2,3]
db.rollback(); //revert recent changes
//map.keySet() is now [1,2]

db.close();
```
<!--
TODO reimplement TxMaker

Concurrent transactions are supported as well, with full serializable isolation, optimistic locking and MVCC snapshots. For concurrent transactions we need one extra factory to create transactions: `TxMaker`. We use `DBMaker` to create it, but instead of `make()` we call `makeTxMaker()`

A single `TxMaker` represents an opened store. `TxMaker` is used to create multiple `DB` objects, each representing a single transaction. In that case `DB.close()` closes one transaction, but storage remains open by `TxMaker`:
-->
