Performance and durability
============================

In default configuration MapDB uses very conservative, safe and slow settings.
Other options make it several times faster, but when used incorrectly,
they might  lead to memory leaks, data corruption or even JVM crash!
Make sure you understand implications and read Java Doc on ``DBMaker``.

On other side maybe author is too paranoid,
his goal is to minimize number of questions on mailing list,
and very safe settings works best for that case.
Many options works just fine except some very exotic circumstances.

Good performance is usually result of compromise between consistency,
speed and durability. MapDB gives several options to make this compromise.
There are 4 different storage implementations, commit and disk sync strategies,
caches, compressions...

This chapter outlines performance and durability related options

Transactions disabled
---------------------

If a process dies in the middle of write, storage files might become
inconsistent. For example the pointer was updated with a new location,
but new data were not written yet. For this MapBD storage is protected by
write-ahead-log (WAL). WAL is
reliable and simple, and is used by many databases, such as Posgresql or
MySQL.

However WAL is slow, data has to be copied and synced multiple times between files.
So there is option to bypass WAL and write data directly to store.
In this case MapDB writes data several times faster.

That is great for in-memory stores or caches where data can be reconstructed.
Other use is to disable WAL for fast initial import, and latter enable it for
production use.

WAL is disabled with  ``DBMaker.transactionDisable()``:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_transaction_disable.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

With WAL disabled you loose crash protection.  In this case you **must** correctly
close the store, or you will loose all your data. MapDB detects
unclean shutdown and will refuse to open such corrupted storage.

There is a shutdown hook to close the database automatically before JVM exits,
however this does not protect your data if JVM crashes or is killed.
Use ``DBMaker.closeOnJvmShutdown()`` option to enable it.

With transactions disabled you loose rollback capability,
``db.rollback()`` will throw an exception. ``db.commit()`` will have
nothing to commit (all data are already stored), so it does the next best
thing: Commit tries to flush all the write caches and synchronizes the storage
files. So if you call ``db.commit()`` and do not make any more writes,
your store should be safe (no data loss) in case of JVM crash.

Memory mapped files (mmap)
--------------------------

MapDB was designed from ground to take advantage of mmap files. However
mmap files are limited to 4GB by addressing limit on 32bit JVM. Mmap have a
lot of nasty effects on 32bit JVM (it might crash if addressing limit is exceeded),
so by default we use a slower and safer disk access mode called Random-Access-File (RAF).

Mmap files are much faster compared to RAF. The exact speed bonus depends on
the operating system and disk case management, but is typically between 10%
and 300%.

Memory mapped files are activated with ``DBMaker.mmapFileEnable()``
setting.

One can also activate mmap files only if a 64bit platform is detected:
``DBMaker.mmapFileEnableIfSupported()``.

Mmap files are highly dependent on the operating system. For example, on
Windows you cannot delete a mmap file while it is locked by JVM. If
Windows JVM dies without closing the mmap file, you have to restart Windows
to release the file lock.

There is also `bug in JVM <http://bugs.java.com/view_bug.do?bug_id=4724038>`_.
Mmaped file handles are not released until ``DirectByteBuffer`` is GCed.
That means that mmap file remains even after ``db.close()`` is called.
On Windows it prevents file to be reopened or deleted.
On Linux it consumes file descriptors, and could lead to errors once all descriptors are used.

There is a workaround for this bug using undocumented API.
But it was linked to JVM crashes in rare cases and is disabled by default.
Use ``DBMaker.fileMmapCleanerHackEnable()`` to enable it.

Here is example with all mmap related options:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_mmap.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

File channel
--------------

By default MapDB uses ``RandomAccessFile`` to access disk storage. Outside fast mmap files there
is third option based on ``FileChannel``. It should be faster than ``RandomAccessFile``,
but has bit more overhead. It also works better under concurrent access (RAF has global lock).

FileChannel was causing problems in combination with ``Thread.interrupt``. If threads
gets interrupted while doing IO, underlying channel is closed for all other threads.

To use FileChannel use ``DBMaker.fileChannelEnable()`` option:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_filechannel.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Checksums
---------------

You may want to protect your data in case file gets corrupted, and some bytes are
randomly replaced. MapDB has basic parity checks which detect such failure very early,
but it optionally supports CRC32 checksums. In this case, each record stores an extra 4 bytes
which contains its CRC32 checksum. If the data are somehow modified or
corrupted , the next read will fail with an exception.
This gives an early warning and prevents db from returning the wrong data.

CRC32 checksum has to be calculated on each put/modify/get operation so
this option has some performance overhead.
It affects the storage format, so once activated you always have to reopen the store
with this setting. Also checksum can not be latter activated, if the store
was created without CRC32.

Checksum is activated by this setting: ``DBMaker.checksumEnable()``:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_crc32.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Cache
----------

By default MapDB has no cache enabled. It leaves operating system to do disk caching,
which works well enough on Linux with mmap files. With enough free heap memory,
there is option to cache frequently accessed object instances (instance cache). This decreases
disk IO and decreases deserialization overhead. On other side it could cause
Out of Memory errors, if cache is not flushed fast enough.

There is :doc:`entire chapter <caches>` on Caches, so here is just quick example to enable most
basic cache:

.. literalinclude:: ../../mapdb/src/test/java/doc/cache_hash_table.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Asynchronous write
--------------------

Write operations such as ``map.put(key,value)`` blocks until data are written to underlying store.
There is option to place data into write queue, and write them latter in background thread.

``.asyncWriteEnable()`` options enables async write queue. You can also change its size with extra
parameter. Write queue can be flushed in background thread if enabled (with ``executorEnable()``).
If there is no background executor write queue gets flushed as part of other operation (in that
case write operatiosn such as ``map.put(key,value)`` or ``db.commit()`` might block for very long time).

Async write options:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_async_write.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Asynchronous write does not compromise durability and consistency.
Operations such as ``db.commit()`` or ``db.close()``
will block until write queue is flushed and synced to disk.


In-memory mode
----------------

MapDB has three in-memory stores:

On-heap which stores objects in ``Map<recid,Object>`` and does not use serialization.
This mode is very fast for small datasets, but is affected by GC, so performance drops from cliff
after a few gigabytes. It is activated with:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_memory_heap.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Store based on ``byte[]``. In this mode data are serialized and stored into 1MB large byte[].
Technically this is still on-heap, but is not affected by GC overhead, since data are not visible to GC.
This mode is recommended by default, since it does not require any additional JVM settings.
Increasing maximal heap memory with ``-Xmx10G`` JVM parameter is enough.

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_memory_byte_array.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Store based on ``DirectByteBuffer``. In this case data are stored completely off-heap.
in 1MB DirectByteBuffers created with ``ByteBuffer.allocateDirect(size)``.
You should increase maximal direct memory with JVM parameter.
This mode allows you to decrease maximal heap size to very small size (``-Xmx128M``).
Small heap size has usually better and more predictable performance.

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_memory_direct.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Allocation options
---------------------

By default MapDB tries minimize space usage and allocates space in 1MB increments.
This additional allocations might be slower than single large allocation.
There are two options to control storage initial size and size increment.
This example will allocate 10GB initially and then increment size in 512MB chunks:

.. literalinclude:: ../../mapdb/src/test/java/doc/performance_allocation.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Allocation Increment has side effect on performance with mmap files.
MapDB maps file in series of DirectByteBuffer. Size of each buffer is equal to Size Increment
(1MB by default), so larger Size Increment means less buffers for the same disk store size.
Operations such as sync, flush and close have to traverse all buffers.
So larger Size Increment could speedup commit and close operations.