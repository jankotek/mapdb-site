Caches
======

MapDB has several options to cache deserialized objects. Proper cache
configuration is crucial for good performance of your application. Many
performance problems can be fixed just by changing cache settings.

Most dbs and old generation of MapDB (JDBM) use fixed size to cache read
from disk. MapDB eliminated page layer, so it does not have regular
cache. Instead it used memory mapped files and relies on operating
system to do disk caching.

When we talk about cache in mapdb we mean *instance cache* .Instead of
pages, MapDB caches deserialized objects such as tree nodes, ``Long``,
``Person`` and so on. Instance cache helps to minimize deserialization
overhead. When you fetch an object twice, it will be deserialized only
once, second time it will be fetched from cache.

Because object instance may be stored in cache, yor data-model has to be
immutable. On every modification you must create copy of object and
persist new version. You also can not modify object fetched from db, an
example:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_right_and_wrong.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

MapDB offers 5 cache implementations. Some are unbounded and could cause
``OutOfMemoryError`` when used incorrectly.

Hash Table cache
----------------

This cache is fixed size hash table (an array), where elements are
placed by recid hash. Old entries are evicted by hash collisions. It has
almost zero performance overhead, but provides good results, so this
cache is recommended.

It does not have automatic entry eviction, so some records may remain in
cache (on heap) for very long time. This cache should be used in
combination with small records, there is no auto-removal and it could
cause ``OOME``

Default cache size is 2048 records, there is DBMaker parameter to
regulate its size. This cache is on by default, so it does not need to
be enabled in DBMaker. An example:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_hash_table.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Least-Recently-Used cache
-------------------------

LRU cache keeps track when records are used, and if cache would grow
beyond maximal size, it removes least recently used records. Compared to
HashTable cache it has better hit statistics, but more overhead. There
is overhead associated with maintaining LRU queue. It is recommended to
use this cache only if cost of deserialization cost is high and cache
miss is serious problem.

MRU queue is maintained on 'best effort', there are some shortcuts for
better performance. So the actual cache size oscillates a few records
around maximal size. Please consult ``LongConcurrentLRUMap`` source for
example.

This cache is activated by ``cacheLRUEnable()`` DBMaker parameter. You
can also change maximal size, default size is 2048 records. An example:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_lru.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Hard reference cache
--------------------

HardRef cache is unbounded cache which does not evict any of its
entries. This cahce is practically ``Map`` of recids and records:
``HashMap<recid, Record>``

If store is larger than heap, it will get filled and eventually cause
``OOME`` exception. It is great to get great performance from small
stores. After cache is warmed, it offers read-only performance
comparable to ``java.util`` collections.

MapDB also has weak/soft reference caches, but GC has some serious
overhead. So hard reference cache has lower overhead if there is enough
memory.

This cache is activated with ``cacheHardRefEnable()`` DBMaker parameter.
It does not have maximal size. An example:


.. literalinclude:: ../../mapdb/src/test/java/doc/cache_hardref.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

TODO is this cache cleared if free memory is bellow threshold?

No records are automatically removed from this cache. But you can still
clear all records manually: ``db.getEngine().clearCache();``


Soft and Weak reference cache
-----------------------------

Other option is to use weak or soft reference cache. In this cache
garbage collector removes records from cache. This cache is practically
``Map`` of recids and references to records:
``HashMap<recid, SoftRef<Record>>``.

Soft and Weak references differs by eagerness with which they are
garbage collected. Weak reference should be GC immediately after all
references are released. Soft reference should be only removed when free
heap is low. However practical implications depends on JVM settings. For
example you can still get ``OutOfMemoryError`` even with soft
references.

This caches are activated by ``cacheWeakRefEnable()`` and
``cacheSoftRefEnable()`` DBMaker parameters:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_weak_soft.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Disabled cache or reduce size
-----------------------------

Instance cache is disabled by default. On small devices you may not want to
disable it to save memory. On other side disabled cache hurts performance. So there are could be
better alternatives:

First you could clear cache after every operation. Cache can be cleared using: ``db.getEngine().clearCache()``

Other alternative is to reduce cache size. By default cache size is
2048 records, probably too much for most Android phones:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_size.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Clear cache
-----------

If you use MapDB in batch operations, you can reduce cache memory overhead,
by clearing cache at end of batch each batch. If cache is empty, it gets faster
populated by new content from new batch:

.. code:: java

        //do some heavy stuff with mapdb:
        map.getAll...

        //we are done clear cache
        db.getEngine().clearCache();

        //now do some other stuff, which does not use MapDB:
        save_my_files...

Cache hit and miss statistics
-----------------------------

Right now MapDB does not offer hit/miss statistics for any cache. This
feature requires a few lines of code and will be added soon.

TODO cache hit/miss statistics.

Cache priority
--------------

TODO Right now there is no record priority for cache.

However it is very easy to add this into MapDB. For example you could
give more preferential treatment to btree directory nodes, while leaf
nodes would not be cached. All it takes is a few
``record instanceof BTreeDirNode`` in single class.

TODO MapDB could also have flexible multi level cache layering.
