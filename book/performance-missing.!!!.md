Section from performance:

Checksums
=========

You may want to protect your data in case file gets corrupted, and some bytes are randomly replaced. MapDB has basic parity checks which detect such failure very early, but it optionally supports CRC32 checksums. In this case, each record stores an extra 4 bytes which contains its CRC32 checksum. If the data are somehow modified or corrupted , the next read will fail with an exception. This gives an early warning and prevents db from returning the wrong data.

CRC32 checksum has to be calculated on each put/modify/get operation so this option has some performance overhead. It affects the storage format, so once activated you always have to reopen the store with this setting. Also checksum can not be latter activated, if the store was created without CRC32.

Checksum is activated by this setting: `DBMaker.checksumEnable()`:

Cache
=====

By default MapDB has no cache enabled. It leaves operating system to do disk caching, which works well enough on Linux with mmap files. With enough free heap memory, there is option to cache frequently accessed object instances (instance cache). This decreases disk IO and decreases deserialization overhead. On other side it could cause Out of Memory errors, if cache is not flushed fast enough.

There is entire chapter &lt;caches&gt; on Caches, so here is just quick example to enable most basic cache:

Asynchronous write
==================

Write operations such as `map.put(key,value)` blocks until data are written to underlying store. There is option to place data into write queue, and write them latter in background thread.

`.asyncWriteEnable()` options enables async write queue. You can also change its size with extra parameter. Write queue can be flushed in background thread if enabled (with `executorEnable()`). If there is no background executor write queue gets flushed as part of other operation (in that case write operatiosn such as `map.put(key,value)` or `db.commit()` might block for very long time).

Async write options:

Asynchronous write does not compromise durability and consistency. Operations such as `db.commit()` or `db.close()` will block until write queue is flushed and synced to disk.
