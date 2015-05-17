Concurrency
=============

All classes in MapDB are thread-safe and to some extend vertically scalable. 
Reads should be linearly scalable without limitations. 
Writes should scale linearly up to 4 CPU cores,  with a peak around 6 cores.
Greater write scalability is possible with a sharding or experimental memory allocator.

However MapDB design is relatively simple. There are global locks and stop-word states.
It is also flexible so it can be tuned well into most situations and
performance / safety compromises.

Segment locking
~~~~~~~~~~~~~~~~

Here is a basic explanation of how MapDB can scale under concurrent access.
{@javadoc Engine} implements key-value storage. Instead of a single global lock,
it is split into multiple segments, each withs its own ``ReadWriteLock``

This way, the records can be updated concurrently to some extend.
Concurrent read operations are not blocked unless an update is running.

Under the segment locks is a global lock called 'structural lock' which
protects the memory allocator. It is locked when record layout changes,
for example when space is allocated, a record was resized or free space is released.

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_segment_locking.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

The memory allocator uses single global lock, and that limits vertical scalability.
There are some workarounds.
For example, each segment can have its own storage file, so no shared state exists.


Collection locking
~~~~~~~~~~~~~~~~~~~

On top of ``Engine`` collections have their own locking layer. Each collection
adds its own locking to ensure that its data (tree nodes, pointers etc) are consistent.

``BTreeMap`` (aka TreeMap) is a lock free BTree implementation. It only locks a single node
on update and has excellent vertical scalability.

``HTreeMap`` splits its data into 16 segments based on key hash. The number of segments
is hardcoded into its design and can not be changed. However it has a simpler
design compared to ``BTreeMap`` and more predictable performance. 
It is also easy to use separate storage for each segment in order to top boost its performance.

Please note that locking at collection layer might not use memory barrier. Since collections are
stateless (all state is in database), locks at this level only protects from concurrent modification.

Consistency safety
~~~~~~~~~~~~~~~~~~

``Map.put()`` translates into multiple operations at ``Engine`` level.
For example a tree must be updated at more levels, counters incremented etc.
Under some configurations this might not be an atomic operation and could
cause data inconsistency. 

For example two commits might happen while a tree is being updated.
The second commit is rolled back and the tree becomes inconsistent.
Or if an inconsistent snapshot is taken, while the tree is being updated,
that would cause an inconsistent backup, while the primary data could be fine.

There are two ways to solve this. First are the sequential safe collections
(usually lock-free), which can handle inconsistency at the expense of fragmentation and lower performance.
``BTreeMap`` is sequentially safe, its nodes are updated in a way which handles inconsistency.

The second way is *Consistency Lock*. It is ``ReadWriteLock`` provided by ``DB.getConsistencyLock()``.
Sequentially, unsafe operations (such as ``HTreeMap.put()``) should be performed under read lock.
Operations which require a consistent state (taking snapshot, commit or close) should be performed under write lock.

``DB`` provides consistency lock:

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_consistency_lock.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Please note that Consistency Lock might not use memory barrier. Since collections are
stateless (all state is in database), locks at this level only protects from concurrent modification.
For details consult MapDB source code.

Executors
~~~~~~~~~~~~~~

Some tasks, such as write or queue maintenance, could be done in a background thread. In default
configuration MapDB does not start any threads (its safer), but piggy backs those tasks as part of regular operations.
This might slow down MapDB operations.

Another option is to give MapDB an executor which performs tasks on the background. In this case
some tasks will be executed asynchronously (writes) or in parallel (compaction). The simplest way is to enable the executor globally:

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_executor_global.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Concurrent execution usually improves the performance, but sometimes it might make it worse. For that, MapDB has several
options to enable Executor only for specific tasks. For example a parallel compaction has no benefit for in-memory store,
but on-disk it brings large improvement, since IO (and disk seeks) can be done in parallel:

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_executor_compaction.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Another option is to enable executor just for asynchronous writes:

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_executor_async_write.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


And finally, for cache expiration in reference based cache (hard, soft or weak cache):

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_executor_cache.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


There is also the question about how many threads and what type of Executor one should use. MapDB uses
`ScheduledExecutorService <https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html>`__
to execute its tasks. Previous MapDB versions were starting threads directly, but that meant problems in restricted and managed environments
such as J2EE containers. It is possible to supply your own Executor directly, as usually you can use one of the factory methods in
``Executors``:

.. literalinclude:: ../../mapdb/src/test/java/doc/concurrency_executor_custom.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


TODO how many threads and executor selection

The Executor keeps threads running on the background. That could mean memory leak and also prevents JVM from shutting down.
And if executor keeps running after db is closed,
background tasks would fail with an exception and possibly loose unwritten data.
For that reason  ``DB.close()`` shutdowns all Executors associated with DB instance,
before it is closed. This way all background task are finished (and synced) before DB is closed.

Currently it is not possible to reuse Executor after DB was closed. It is also not possible to share Executor between
DB instances if one of them is closed. The solution for that is to provide Executor wrapper, which only shutdowns its own tasks,
but that is not implemented yet.

TODO executor sharing wrapper

