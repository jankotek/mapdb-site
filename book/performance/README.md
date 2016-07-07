Performance and durability
==========================

Good performance is result of compromise between consistency, speed and durability. MapDB gives several options to make this compromise. There are different storage implementations, commit and disk sync strategies, caches, compressions...

This chapter outlines performance and durability related options. Some options will make storage writes durable at expense of speed. Some other settings might cause memory leaks, data corruption or even JVM crash! Make sure you understand implications and read [Javadoc on DBMaker](http://www.mapdb.org/javadoc/latest/mapdb/org/mapdb/DBMaker.Maker.html).

Transactions and crash protection
---------------------------------

If store is not closed properly and are pending changes flushed to disk, store might become corrupted. That often happens if JVM process crashes or is violently terminated.

To protect file from corruption, MapDB offers Write Ahead Log (WAL). It is reliable and simple way to make file changes atomic and durable. WAL is used by many databases including Posgresql or MySQL. However WAL is slower, data has to be copied and synced multiple times between files.

WAL is disabled by default. It can be enabled with `DBMaker.transactionEnable()`:

<!--- #file#performance_transaction_enable.java--->
```java
DB db = DBMaker
        .fileDB(file)
        .transactionEnable()
        .make();
```
With WAL disabled (by default) you do not have a crash protection. In this case you **must** correctly close the store, or you will loose all your data. MapDB detects unclean shutdown and will refuse to open such corrupted storage. There is a way to open corrupted store in readonly mode and perform data rescue.

There is a shutdown hook to close the database automatically before JVM exits, however this does not protect your data if JVM crashes or is killed. Use `DBMaker.closeOnJvmShutdown()` option to enable it.

With transactions disabled you do not have rollback capability, `db.rollback()` will throw an exception. `db.commit()` will have nothing to commit (all data are already stored), so it does the next best thing: Commit tries to flush all the write caches and synchronizes the storage files. So if you call `db.commit()` and do not make any more writes, your store should be safe (no data loss) in case of JVM crash.

*TODO JVM write cache flush, versus system flush.*

Memory mapped files (mmap)
--------------------------

MapDB was designed from ground to take advantage of mmap files. However on 32bit JVM mmap files are limited to 4GB by its addressing limit. When JVM runs out of addressing space there are nasty effects such as JVM crash. By default we use a slower and safer disk access mode called Random-Access-File (RAF).

Mmap files are much faster compared to RAF. The exact speed bonus depends on the operating system and disk case management, but is typically between 10% and 300%.

Memory mapped files are activated with `DBMaker.mmapFileEnable()` setting.

One can also activate mmap files only if a 64bit platform is detected: `DBMaker.mmapFileEnableIfSupported()`.

Mmap files are highly dependent on the operating system. For example, on Windows you cannot delete a mmap file while it is locked by JVM. If Windows JVM dies without closing the mmap file, you have to restart Windows to release the file lock.

There is also [bug in JVM](http://bugs.java.com/view_bug.do?bug_id=4724038). Mmaped file handles are not released until `DirectByteBuffer` is GCed. That means that mmap file remains open even after `db.close()` is called. On Windows it prevents file to be reopened or deleted. On Linux it consumes file descriptors, and could lead to errors once all descriptors are used.

There is a workaround for this bug using undocumented API. But it was linked to JVM crashes in rare cases and is disabled by default. Use `DBMaker.cleanerHackEnable()` to enable it.

Here is example with all mmap related options:

<!--- #file#performance_mmap.java--->
```java
DB db = DBMaker
    .fileDB(file)
    .fileMmapEnable()            // Always enable mmap
    .fileMmapEnableIfSupported() // Only enable mmap on supported platforms
    .fileMmapPreclearDisable()   // Make mmap file faster

        // Unmap (release resources) file when its closed.
        // That can cause JVM crash if file is accessed after it was unmapped
        // (there is possible race condition).
    .cleanerHackEnable()
    .make();

//optionally preload file content into disk cache
db.getStore().fileLoad();
```
File channel
------------

By default MapDB uses `RandomAccessFile` to access disk storage. Outside fast mmap files there is third option based on `FileChannel`. It should be faster than `RandomAccessFile`, but has bit more overhead. It also works better under concurrent access (RAF has global lock).

FileChannel was causing problems in combination with `Thread.interrupt`. If threads gets interrupted while doing IO, underlying channel is closed for all other threads.

To use FileChannel use `DBMaker.fileChannelEnable()` option:

<!--- #file#performance_filechannel.java--->
```java
DB db = DBMaker
    .fileDB(file)
    .fileChannelEnable()
    .make();
```
In-memory mode
--------------

MapDB has three in-memory stores:

On-heap which stores objects in `Map<recid,Object>` and does not use serialization. This mode is very fast for small datasets, but is affected by GC, so performance drops from cliff after a few gigabytes. It is activated with:

<!--- #file#performance_memory_heap.java--->
```java
DB db = DBMaker
    .heapDB()
    .make();
```
Store based on `byte[]`. In this mode data are serialized and stored into 1MB large byte\[\]. Technically this is still on-heap, but is not affected by GC overhead, since data are not visible to GC. This mode is recommended by default, since it does not require any additional JVM settings. Increasing maximal heap memory with `-Xmx10G` JVM parameter is enough.

<!--- #file#performance_memory_byte_array.java--->
```java
DB db = DBMaker
    .memoryDB()
    .make();
```
Store based on `DirectByteBuffer`. In this case data are stored completely off-heap. in 1MB DirectByteBuffers created with `ByteBuffer.allocateDirect(size)`. You should increase maximal direct memory with JVM parameter. This mode allows you to decrease maximal heap size to very small size (`-Xmx128M`). Small heap size has usually better and more predictable performance.

<!--- #file#performance_memory_direct.java--->
```java
// run with: java -XX:MaxDirectMemorySize=10G
DB db = DBMaker
    .memoryDirectDB()
    .make();
```
Allocation options
------------------

By default MapDB tries minimize space usage and allocates space in 1MB increments. This additional allocations might be slower than single large allocation. There are two options to control storage initial size and size increment. This example will allocate 10GB initially and then increment size in 512MB chunks:

<!--- #file#performance_allocation.java--->
```java
DB db = DBMaker
    .fileDB(file)
    .fileMmapEnable()
    .allocateStartSize( 10 * 1024*1024*1024)  // 10GB
    .allocateIncrement(512 * 1024*1024)       // 512MB
    .make();
```
Allocation Increment has side effect on performance with mmap files. MapDB maps file in series of DirectByteBuffer. Size of each buffer is equal to Size Increment (1MB by default), so larger Size Increment means less buffers for the same disk store size. Operations such as sync, flush and close have to traverse all buffers. So larger Size Increment could speedup commit and close operations.
