Frequently Asked Questions
==========================

General questions
-------------------

Is MapDB free as a beer?
^^^^^^^^^^^^^^^^^^^^^^^^^^

Yes! MapDB is distributed under `Apache 2
license <https://github.com/jankotek/MapDB/blob/master/license.txt>`__,
it can be modified, sold and used in commercial product. All
documentation, unit tests and build scripts are published as well. I
believe that MapDB has good chance to become the de facto standard Java
storage engine. For that it needs permissive license and even LGPL could
ruin its chances.

Is MapDB commercial project?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Yes and no. It started as a hobby project.
Now MapDB is sponsored by company Codefutures and I work on MapDB full-time.
However it is still developed as independent opensource project.
There is plan to make money on consulting once MapDB is popular. I am `available <http://kotek.net/contact>`__ for commercial support and consulting

What sort of DB is this? (graph, sql, key-value...)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

MapDB is *not* a database, it is db engine on top of which some database
can be build, similar way as MyISAM is engine for MySQL database. I try
to keep MapDB as generic as possible. For that reason MapDB does not
have its own network protocol or query language.

On other side MapDB is very flexible, has many utilities and offers rich
set if data structures (trees, queues...). So it is easy to use MapDB
API directly similar way as SQL.

Are there any design goals?
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Two design goals: minimal overhead and simplicity without compromising features.
MapDB evolved differently than most dbs, so internally it is very different.

Does it have ACID transactions?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Yes. MapDB has several storage modes, two of them are transactional Write-Ahead-Log and Append-Only.
Than there is optional layer on top which provides MVCC isolation.


Can MapDB be used as in-memory cache?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Absolutely. MapDB has very small overhead and low space usage.
HashMap has build in expiration based on Time-To-Live or maximal collection size.
TODO: link to doc

Does MapDB have network server?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

No. It is embedded database engine. Network interface should be provided by
other project which sits on top.
TODO: There is plan to implement Redis server protocol, so MapDB would be drop in replacement for Redis server

Does it have replication/clustering?
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

No. I work on other DB `AgilData <http://codefutures.com/agildata/>`_ which is clustered database inspired by MapDB


Performance questions
----------------------

HashMap or TreeMap?
^^^^^^^^^^^^^^^^^^^^^

HashMap is better suited for large keys. TreeMap is suited for smaller
keys. TreeMap may be also good choice for larger keys which can be
delta-packed (strings, tuples).


Troubleshooting questions
---------------------------------

MapDB uses chained exception so user does not have to write try catch
blocks. IOException is usually wrapped in IOError which is unchecked. So
please always check root exception in the chain.

java.lang.OutOfMemoryError: Map failed
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

MapDB can not map file into memory. Make sure you are using latest JVM
(7+). This is common problem on Windows (use Linux). Workaround is to
use RAF files (on by default) and disable memory mapped files
(``mmapFileEnable()``)

InternalError, Error, AssertionFailedError, IllegalArgumentException, StackOverflowError and so on
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

There was an problem in MapDB. Maybe store was corrupted thanks to an
internal error or disk failure. Disabling cache
``DBMaker.cacheDisable()`` or async writes
``DBMaker.asyncWriteDisable()`` may workaround the problem. Please
`create new bug report <https://github.com/jankotek/MapDB/issues/new>`__
with code to reproduce this issue.

OutOfMemoryError: GC overhead limit exceeded
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Your app is creating new object instances faster then GC can collect
them. This assertion is triggered if Garbage Collection consumes 98% of
all CPU time. Tune Instance Cache settings and optimize your
serialization. You may also increase heap size by ``-Xmx3G`` switch.
Workaround is to disable this assertion by JVM switch
``-XX:-UseGCOverheadLimit``

Can not delete or rename db files on Windows
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

MapDB uses memory mapped files, Windows locks them exclusively and
prevents deletion. Solution is to close MapDB properly before JVM exits.
Make sure you have latest JVM. Disable memory mapped files of those are
enabled (``mmapFileEnable()``) Also restarting Windows may help.

Computer becomes very slow during DB writes
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

MapDB uses most of available CPU to speedup writes. Try to insert
Thread.sleep(1) into your code, or set lower thread priority.

File locking, OverlappingFileLockException, some IOErrors
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

You are trying to open file already opened by another MapDB. Make sure
that you ``DB.close()`` store correctly. Some operating systems
(Windows) may leave file lock even after JVM is terminated. You may also
try to open database in read-only mode.

Strange behavior in collections
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Maps and Sets in MapDB should be drop-in replacement for ``java.util``
collections. So any significant difference is a bug. Please `create new
bug report <https://github.com/jankotek/MapDB/issues/new>`__ with code
to reproduce issue.

Hard to replicate issues
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

If you ran into hard to replicate problem (race condition, sneak data
corruption), I would strongly suggest to record JVM execution with
`Chronon <http://www.chrononsystems.com/learn-more/products-overview>`__
and submit the record together with a bug report. Thanks to Chronon we
can replicate and fix issues at light speed.
