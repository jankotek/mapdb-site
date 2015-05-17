Durability and speed
====================

There are several configuration options to make compromises between
durability and speed. You may choose consistency, disk access patterns,
commit type, flush type and so on.

Transactions disabled
---------------------

If a process dies in the middle of write, storage files might become
inconsistent. For example the pointer was updated with a new location,
but new data were not written yet. For this MapBD storage is protected by
write-ahead-log (WAL) which applies commits in atomic fashion. WAL is
reliable and simple, and is used by many databases, such as Posgresql or
MySQL.

However WAL is slow, data has to be copied and synced multiple times.
You may optionally disable WAL by disabling transactions:
``DBMaker.transactionDisable()``. In this case you **must** correctly
close the store before JVM shutdown, or you loose all your data. You may
also use a shutdown hook to close the database automatically before JVM exits,
however this does not protect your data if JVM crashes or is killed:


.. literalinclude:: ../../mapdb/src/test/java/doc/durability_transaction_disable.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Transaction disable (also called direct mode) will apply all the changes
directly into storage file. Combine this with in-memory store or mmap
files and you get very fast storage. Typical use is in a scenario where
the data does not have to be persisted between JVM restarts or can be easily
recreated: off-heap caches.

Other important usage is for the initial data import. Transactions and
no-transactions share the same storage format (except optional write-ahead-log),
so one can import data very quickly with transactions disabled. Once the import is
finished, the store is reopened with transactions enabled.

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

CRC32 checksums
---------------

You may want to protect your data from disk corruption. MapDB optionally
supports CRC32 checksums. In this case, each record stores an extra 4 bytes
which contains its CRC32 checksum. If the data are somehow modified or
corrupted (file system or storage error), the next read will fail with an
exception. This gives an early warning and prevents db from returning the wrong
data.

CRC32 checksum has to be calculated on each put/modify/get operation so
this option has some performance overhead. Write-ahead-log uses CRC32
checksum by default.

Checksum is activated by this setting: ``DBMaker.checksumEnable()``. It
affects the storage format, so once activated you always have to reopen the store
with this setting. Also checksum can not be latter activated once the store
was created without it.

TODO: CRC32 serializer link

TODO: WAL and CRC32 disable

TODO: WAL flush on commit

TODO async file sync, use futures?
