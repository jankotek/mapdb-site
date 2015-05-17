Caches
======

MapDB has several options to cache deserialized objects. A proper cache
configuration is crucial for good performance of your application. Many
performance problems can be fixed just by changing the cache settings.

Most dbs and the old generation of MapDB (JDBM) use fixed size to cache read
from the disk. MapDB eliminated the page layer, so it does not have regular
cache. Instead it used memory mapped files and relies on the operating
system to do disk caching.

When we talk about cache in mapdb we mean *instance cache* .Instead of
pages, MapDB caches deserialized objects, such as tree nodes, ``Long``,
``Person`` and so on. Instance cache helps to minimize deserialization
overhead. When you fetch an object twice, it will be deserialized only
once. The second time it will be fetched from cache.

Because the object instance may be stored in cache, yor data-model has to be
immutable. On every modification you must create a copy of the object and
persist the new version. You also can not modify an object fetched from the db. An
example of this is the following:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_right_and_wrong.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

MapDB offers 5 cache implementations. Some are unbounded and could cause
``OutOfMemoryError`` when used incorrectly.

Hash Table cache
----------------

This cache is a fixed size hash table (an array), where elements are
placed by recid hash. Old entries are evicted by hash collisions. It has
almost zero performance overhead and it provides good results, so this
cache is recommended.

It does not have an automatic entry eviction, so some records may remain in
cache (on heap) for a very long time. This cache should be used in
combination with small records. As there is no auto-removal and it could
cause OOME.

Default cache size is 2048 records and there is a DBMaker parameter to
regulate its size. This cache is on by default, so it does not need to
be enabled in DBMaker. An example:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_hash_table.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Least-Recently-Used cache
-------------------------

LRU cache keeps track when records are used, and if cache grows
beyond maximal size, it removes the least recently used records. Compared to
HashTable, cache has better hit statistics, but more overhead. There
is overhead associated with maintaining LRU queue. It is recommended to
use this cache only if the cost of the deserialization is high and the cache
miss would be a serious problem.

MRU queue is maintained on 'best effort' and there are some shortcuts for
better performance. So the actual cache size oscillates a few records
around maximal size.

This cache is activated by the ``cacheLRUEnable()`` DBMaker parameter. You
can also change the maximal size. The default size is 2048 records. An example:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_lru.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Hard reference cache
--------------------

HardRef cache is an unbounded cache which does not evict any of its
entries. This cache is practically a ``Map`` of recids and records:
``HashMap<recid, Record>``

If the store is larger than the heap, it will get filled and eventually cause
``OOME`` exception. It is great to get great performance from small
stores. After the cache is warmed, it offers read-only performance
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
clear all the records manually: ``db.getEngine().clearCache();``


Soft and Weak reference cache
-----------------------------

Another option is to use weak or soft reference cache.
Garbage collector removes records from cache after they are GCed. This cache is practically a
``Map`` of recids and references to records:
``HashMap<recid, SoftRef<Record>>``.

Soft and Weak references differ by the eagerness with which they are
garbage collected. Weak reference should be GCed immediately after all the
references are released. Soft reference should be only removed when the free
heap is low. However, the practical implications depend on JVM settings. For
example you can still get ``OutOfMemoryError`` even with soft
references, if memory is not reclaimed fast enough.

This caches are activated by ``cacheWeakRefEnable()`` and
``cacheSoftRefEnable()`` DBMaker parameters:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_weak_soft.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Disabled cache or reduce size
-----------------------------

Instance cache is disabled by default, it was causing memory problems on small devices.
On the other hand a disabled cache hurts the performance. So there are could be
better alternatives:

First you could clear the cache after every operation. The cache can be cleared using: ``db.getEngine().clearCache()``

Another alternative is to reduce the cache size. By default the cache size is
2048 records (when enabled), probably too much for most Android phones:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_size.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Clear cache
-----------

If you use MapDB in batch operations, you can reduce the cache memory overhead,
by clearing cache at the end of each batch. If the cache is empty, it gets faster
populated by new content from a new batch:

.. code:: java

        //do some heavy stuff with mapdb:
        map.getAll...

        //we are done clear cache
        db.getEngine().clearCache();

        //now do some other stuff, which does not use MapDB:
        save_my_files...

Cache hit and miss metrics
-----------------------------

There is option to enable metrics in DBMaker. MapDB will log metrics periodically at info level if enabled.

TODO cache hit/miss statistics.

Cache priority
--------------

TODO Right now there is no record priority for cache.

However it is very easy to add this into MapDB. For example you could
give more preferential treatment to btree directory nodes, while leaf
nodes would not be cached. All it takes is a few
``record instanceof BTreeDirNode`` in single class.

TODO MapDB could also have flexible multi level cache layering.
