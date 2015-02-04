MapDB
========

MapDB is a database engine. But its flexibility and performance makes it usable in many
non traditional situations:

 * in-memory performance is comparable to ``java.util`` collections, so it can be use used as drop-in replacement for those

 * one could use it as alternative memory model to Jave Heap and program business logic outside of limits imposed by Garbage Collections

 * TODO streaming

 * TODO data forklift

 * TODO serialization

TODO not magic solution disclaimer

Collections
~~~~~~~~~~~~~

MapDB provides following collections: Maps, Sets and Queues.
TODO: clarify if queues will move to separate project
TODO: BlockingDequeue with List implementation based on LinkedList?

BTreeMap and HTreeMap
---------------------

MapDB provides two index implementations: btree and hash tree. Both have distinctive features
and excel in different scenarios.

``BTreeMap`` provides TreeMap and TreeSet. It is excellent for huge collections with small keys.
It also has lock-free design, great for parallel updates on multi-core systems. It has wide range of
compression options with almost zero overhead (delta, common prefix) to minimize data consumption.
Compared to ``HTreeMap`` it also has faster iteration and Data Pump streaming.

``HTreeMap`` provides HashMap and HashSet. Unlike other hash based Map implementations it uses
auto-expanding hash table and  never performs rehashing to grow table.It works great with large keys.
Also its better for entry removals compared to ``BTreeMap``. Finally ``HTreeMap`` has various
options for entry expiration based on maximal size, time to live, storage usage and so on.


Queues
---------
TODO decide if lock-free queues will be kept


Atomic variables
-------------------

MapDB has atomic variables similar to ``java.util.concurrent.atomic`` package. Their state is stored in storage
and preserved between JVM restarts. They support compare-and-swap as expected, but also fully respect transactions.

Atomic variables makes it very simple to write business logic completely stored in MapDB storage not affected
by Garbage Collector overhead. Also they respect traditional java concurrency such as locks, CAS, semaphores etc.

There is usual cohort of data types such as ``AtomicLong`s, `AtomicString``s etc. But there is also
``Atomic.Var`` which can store any class. Its content gets serialized and preserved in database. All it needs
is correct ``equals`` method implementation for CAS and to be immutable.



TODO links to chapters
