MapDB internals
==============================

This chapter gives a quick  introduction to MapDB internal
architecture. The rest of this manual assumes that you are familiar with this
chapter.

MapDB originally evolved as a store for astronomical desktop applications.
Over time it grew into a full database engine with concurrent access, durability etc.
But it evolved differently from most DBs, Pentium at 200MHz with 128 MB RAM does not give much space.
The major goal was tight integration with Java and to minimize overhead of any sort
(garbage collection, memory, CPU, stack trace length...).

What makes MapDB most different is that the serialization lifecycle is very different here.
In most DB engines the user has to serialize data himself and pass binary data into db. API
that looks similar to this:

.. code:: java

        engine.update(long recid, byte[] data);

But MapDB serializes data itself by using a user supplied serializer:

.. code:: java

        engine.update(long recid, Person data, Serializer<Person> serializer);

So serialization lifecycle is driven by MapDB rather than by the user. This
small detail is the reason why MapDB is so flexible. For example the ``update``
method could pass the data-serializer pair to
`background-writer <http://www.mapdb.org/apidocs/org/mapdb/AsyncWriteEngine.html>`__ thread
and return almost instantly. Or ``Person`` instance could be stored in
`instance cache <http://www.mapdb.org/apidocs/org/mapdb/Caches.html>`__, to minimise
deserilization overhead on multiple reads. ``Person`` does not even have
to be serialized, but could be stored in ``Map<Long,Person>`` map `on
heap <http://www.mapdb.org/apidocs/org/mapdb/StoreHeap.html>`__, in this case MapDB has speed
comparable to Java Collections.

Colons can be used to align columns.

Dictionary
----------

============================    ==========================================================================================================================================================================
Term                            Explanation
============================    ==========================================================================================================================================================================
Record                          Atomically stored value. Usually tree node or similar. 
                                Transaction conflicts and locking is usually per record.
                                
Index Table                     Table which translates recid into real offset and size in physical file.

Engine                          Primitive key-value store used by collections for storage.

Store                           Engine implementation which actually persist data. Is wrapped by other Engines.

Volume                          Abstraction over ByteBuffer or other raw data store. Used for files, memory, partition etc..

Slice                           Non overlapping pages used in Volume. Slice size is 1MB. Older name was 'chunk'

Direct mode                     With disabled transactions, data are written directly into file. It is fast,
                                but store is not protected from corruption during crashes.

WAL                             Write Ahead Log, way to protect store from corruption if db crashes during write.

RAF                             Random Access File, way to access data on disk. Safer but slower method.

MMap file                       memory mapped file. On 32bit platform has size limit around 2GB. Faster than RAF.

index file                      contains mapping between recid (index file offset) and record size and offset in
                                physical file (index value). Is organized as sequence of 8-byte longs

index value                     single 8-byte number from index file. Usually contains record offset in physical file.

recid                           record identificator, a unique 8-byte long number which identifies record.
                                Recid is offset in index file. After record is deleted, its recid may be reused for
                                newly inserted record.

record                          atomical value stored in storage (Engine) identified by record identifier (recid).
                                In collections Record corresponds to tree nodes. In Maps record may not correspond to
                                Key->Value pair, as multiple keys may be stored inside single node.

physical file                   Contains record binary data

cache (or instance cache)       caches object instances (created with 'new' keyword).
                                MapDB does not have traditional fixes-size-buffer cache for binary pages
                                (it relies on OS to do this). Instead deserialized objects are cached on heap to
                                minimise deserialization overhead.
                                Instance cache is main reason why your keys/values must be immutable.

BTreeMap                        tree implementation behind TreeMap and TreeSet provided by MapDB

HTreeMap                        tree implementation behind HashMap and HashSet provided by MapDB

delta packing                   compression method to minimalise space used by keys in BTreeMap. Keys are sorted,
                                so only difference between keys needs to be stored. You need to provide specialized
                                serializer to enable delta packing.

append file db                  alternative storage format. In this case no existing data are modified, but all changes
                                are appended to end of file. This may improve write speed and durability,
                                but introduces some tradeoffs.

temp map/set...                 collection backed by file in temporary directory. Is usually configured to delete file
                                after close or on JVM exit. Data written into temp collection are not persisted between JVM restarts.

async write                     writes may be queued and written into file on background thread. This does not affect commit
                                durability (it blocks until queue is empty).

TX                              equals to Concurrent Transaction.

LongMap                         specialized map which uses primitive long for keys. It minimises boxing overhead.

DB                              API class exposed by MapDB. It is an abstraction over Engine which manages MapDB collections
                                and storage.

DMaker                          Builder style factory class, which opens and configures DB instances.

Collection Binding              MapDB mechanism to keep two collections synchronized. It provides secondary keys and values,
                                aggregations etc.. known from SQL and other databases. All functions are provided as static
                                methods in Bind class.

Data Pump                       Tool to import and manipulate large collections and storages.
============================    ==========================================================================================================================================================================

DBMaker and DB
--------------

90% of users will only need two classes from MapDB.
`DBMaker <http://www.mapdb.org/apidocs/org/mapdb/DBMaker.html>`__ is a builder style
configurator which opens the database. `DB <http://www.mapdb.org/apidocs/org/mapdb/DB.html>`__
represents the store, it creates and opens collections, commits or rollbacks
data.

MapDB collections use ``Engine`` (simple key-value store) to persist its
data and state. Most of the functionality comes from mixing ``Engine``
implementations and wrappers. For example, off-heap store with
asynchronous writes and instance cache could be instantiated by this
pseudo-code:

.. code:: java

        //TODO this is obsolete, cache and async are integrated to Store

        Engine engine = new Caches.HashTable(         //instance cache
                            new AsyncWriteEngine(     //asynchronous writes
                             new StoreWAL(            //actual store with WAL transactions
                              new Volume.MemoryVol()  //raw buffer used for storage
                        )))

Reality is even more complex, since each wrapper takes extra parameters
and there are more levels. So ``DBMaker`` is a factory which takes
settings and wires all MapDB classes together.

``DB`` has a similar role. It is too hard to load and instantiate
collections manually (for example ``HTreeMap`` constructor takes 14
parameters). So ``DB`` stores all the settings in the Named Catalog and handles
collections. Named Catalog is a ``Map<String,Object>`` which is persisted
in store at fixed recid and contains parameters for all other named
collections and named records. In order to rename a collection one just has to
rename the relevant keys in the Named Catalog.

Layers
------

MapDB stack is a little bit different from most DBs. It integrates
instance cache and serialization usually found in ORM frameworks. On the
other hand MapDB eliminated fixed-size page and disk cache.

From raw-files to ``Map`` interface it has the following layers:

1) **Volume** - an ``ByteBuffer`` like abstraction over raw store. There
   are implementations for in-memory buffers or files.

2) **Store** - primitive key-value store (implementation of ``Engine``).
   Key is offset on index table, value is variable length data. It has
   single transaction. Implementations are Direct, WAL, append-only and
   Heap (which does not use serialization). It performs serialization,
   encryption and compression.

3) **AsyncWriterEngine** - is optional ``Store`` (or ``Engine``) wrapper
   which performs all modifications on background thread.

4) **Instance Cache** - is ``Engine`` wrapper which caches object
   instances. This minimises deserilization overhead.

5) **TxMaker** - is ``Engine`` factory which creates fake ``Engine`` for
   each transaction or snapshot. Dirty data are stored on heap.

6) **Collections** - such as TreeMap use ``Engine`` to store their data
   and state.

Volume
------

``ByteBuffer`` is the best raw buffer abstraction Java has. However, its size
is limited by 31 bits addressing to 2GB. For that purpose MapDB uses
``Volume`` as raw buffer abstraction. It takes multiple
``ByteBuffer``\ s and uses them together with 64bit addressing. Each
``ByteBuffer`` has 1GB size and represents a *slice*. The IO operations which
cross slice boundaries are not supported (``readLong(1GB-3)`` will throw
an exception at such offset). It is the responsibility of the higher layer ``Store`` to ensure
data do not overlap slice boundaries.

MapDB provides some Volume implementations: heap buffers, direct
(off-heap) buffers, memory mapped files and random access file. Each
implementation fits a different situation. For example memory mapped files
have great performance, however a 32bit desktop app will probably prefer
random access files. All implementations share the same format, so it is
possible to copy data (and entire store) between implementations.

Users can also supply their own ``Volume`` implementations. For example,
each 1Gb slice can be stored in a separate file on multiple disks, to
create software RAID. ``Volume`` could also handle duplication, binary
snapshots (MapDB snapshots are at different layer) or raw disks.

Store
-----

`Engine <http://www.mapdb.org/apidocs/org/mapdb/Engine.html>`__ (and
`Store <http://www.mapdb.org/apidocs/org/mapdb/Store.html>`__) is a primitive key-value store
which maps recid (8-byte long record id) to some data (record). It has 4
methods for CRUD operations and 2 transaction methods:

.. code:: java

        long put(A, Serializer<A>)
        A get(long, Serializer<A>)
        void update(long, A, Serializer<A>)
        void delete(long, Serializer<A>)

        void commit()
        void rollback()

By default MapDB stack supports only a single transaction. However there
is the wrapper ``TxMaker``, which stores un-commited data on heap and
provides concurrent ACID transactions.

`DB <http://www.mapdb.org/apidocs/org/mapdb/DB.html>`__ is a low level implementation of the
``Engine``, which stores data on raw ``Volume``. It usually has two files
(or Volumes): index table and physical file. Recid (record ID) is
a usually fixed offset in the index table, which contains a pointer to the physical
file.

MapDB has multiple ``Store`` implementations, which differ in speed and
durability guarantees. User can also supply their own implementation.

First (and default) is `StoreWAL <http://www.mapdb.org/apidocs/org/mapdb/StoreWAL.html>`__.
In this case the Index Table contains the record size and offset in a physical
file. Large records are stored as a linked list. StoreWAL has free space
management, so released space is reused. However, over time it may
require compaction. StoreWAL stores modifications in *Write Ahead Log*,
which is a sequence of simple instructions, such as *write byte at this
offset*. On commit (or reopen) WAL is replayed into the main store, and
discarded after successful file sync. On rollback the WAL is discarded.

`StoreDirect <http://www.mapdb.org/apidocs/org/mapdb/StoreDirect.html>`__ shares the same
file format with ``StoreWAL``, however it does not use write ahead log.
Instead, it writes data directly into files and performs file sync
on commit and close. This implementation trades any sort of data
protection for speed, so data are usually lost if ``StoreDirect`` is not
closed correctly (or synced after last write). Because there is no WAL,
this store does not support rollback. This store is used if transactions
are disabled.

The third implementation is ``StoreAppend``, which provides append-only file
store. Because data are never overwritten, it is very solid and stable.
However space usage skyrockets, since it stores all modifications ever
made. TODO This store is not finished yet, so for example advanced
compaction is missing. TODO Also all possibilities of this store are not
explored (and documented yet). This store reads all data in sequence, in
order to build Index Table which points to newest version of each
record. The Index Table is stored on heap.

TxMaker
-------

MapDB ``Store`` support only a single transaction. So concurrent
transactions need to be serialized and commited one by one. For this
there is `TxMaker <http://www.mapdb.org/apidocs/org/mapdb/TxMaker.html>`__. It is a factory
which creates a fake ``Engine`` for each transaction. Dirty (uncommited)
data are stored on heap. Optimistic concurrency control is used to
detect conflicts.
`TxRollbackException <http://www.mapdb.org/apidocs/org/mapdb/TxRollbackException.html>`__ is
thrown on write or commit, if the current transaction was rolled back thanks
to an conflict.

TxMaker has a Serializable Isolation level and this level supports highest
guarantees. Other isolation levels are not implemented, since the author
does not want to support (and explain) isolation problems.

TODO Current TxMaker uses global lock, so concurrent performance sucks.
It will be rewritten after 1.0 release.

Collections
-----------

MapDB collection uses ``Engine`` as its parameter. There are two basic
indexes:

`BTreeMap <http://www.mapdb.org/apidocs/org/mapdb/BTreeMap.html>`__ is an ordered B-Linked-Tree.
It offers great concurrent performance. It is best for small sized keys.

`HTreeMap <http://www.mapdb.org/apidocs/org/mapdb/HTreeMap.html>`__ is a segmented Hash-Tree.
It is good for large keys and values. It also supports entry expiration
based on maximal size or time-to-live.

There also also `Queues <http://www.mapdb.org/apidocs/org/mapdb/Queues.html>`__ and
`Atomic <http://www.mapdb.org/apidocs/org/mapdb/Atomic.html>`__ variables

TODO explain collections.

Serialization
-------------

MapDB contains its own serialization framework. TODO explain
serialization

Concurrency patterns
--------------------

TODO concurrency patterns.
