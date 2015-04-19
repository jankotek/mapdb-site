Concurrency
=============

All classes in MapDB are thread-safe and to some extend vertically scalable. 
Reads should be linearly scalable without limitations. 
Writes should scale to 4 CPU cores linearly with peak around 6 cores. 
Greater write scalability is possible with sharding or experimental memory allocator. 

However MapDB design is relatively simple. There are global locks and stop-word states.
It is also flexible so it can be tuned well into most situations and
performance / safety compromises.

Segment locking
~~~~~~~~~~~~~~~~

Here is basic explanation how MapDB can scale under concurrent access.
{@javadoc Engine} implements key-value storage. Instead of single global lock 
it is split into multiple segments, each withs its own ``ReadWriteLock``

This way records can be updated concurrently to some extend. 
Concurrent read operations are not blocked unless update is running. 

Inside segment locks is global lock called 'structural lock' which 
protects memory allocator. It is locked when record layout changes,
for example space is allocated, record was resided or free space released.

{@code concurrency-segment-locking}

Memory allocator under global lock limits vertical scalability, so there 
are various workarounds. 
For example each segment can have its own storage file, so no shared state exists.


Collection locking
~~~~~~~~~~~~~~~~~~~

On top of ``Engine`` locking are implemented collections. Each collection 
add its own locking to ensure that its data (tree nodes, pointers etc) are consistent. 

``BTreeMap`` (aka TreeMap) is lock free BTree implementation. It only locks single node 
on update and has excellent vertical scalability.

``HTreeMap`` splits its data into 16 segments based on key hash. Number of segments
is hardcoded into its design and can not be changed. However its has simpler 
design compared to ``BTreeMap`` and more predictable performance. 
It is also easy to use separate storage for each segment top boost its performance.

Consistency safety
~~~~~~~~~~~~~~~~~~

Map.put() translates into multiple operations at ``Engine`` level.
Tree must be updated at more levels, counters incremented etc. 
Under some configurations this might not be atomic operation and could
cause data inconsistency. 

For example two commits might happen while tree is being updated,
second commit is rolled back and tree becomes inconsistent. 
Or inconsistent snapshot is taken while tree is updated,
that would cause inconsistent backup, while primary data would be fine.

There are two ways to solve this. First are sequential safe collections 
(usually lock-free) which can handle inconsistency at expense of fragmentation and lower performance. 
``BTreeMap`` is sequentially safe, its nodes are updated in way which handles inconsistency.

Second way is *Consistency Lock*. It is ``ReadWriteLock`` provided by ``DB.getConsistencyLock()``.
Sequentially unsafe operations (such as ``HTreeMap.put()``) should be performed under read lock.
Operations which require consistent state (taking snapshot, commit or close) should be performed under write lock.

``DB`` provides sequential lock:

{@code concurrency-consistency-lock}


Executors
~~~~~~~~~~~~~~

Some tasks such as write or queue maintenance could be done in background thread. In default
configuration MapDB does not start any threads (its safer), but piggy backs those tasks to regular operations, at cost
of slowing them down.

Other option is to give MapDB an executor which could perform those tasks on background. In this case
some tasks will be executed asynchronously (writes) or in parallel (compaction). Simplest is to enable executor globally:

{@code concurrency-executor-global}

Concurrent execution usually improves performance, but sometimes it might make it worse. For that MapDB has several
options to enable Executor only for specific tasks. For example parallel in-memory compaction has no benefit,
but on-disk it has large improvement, since IO can be done in parallel:

{@code concurrency-executor-compaction}

Other option is to enable executor just for asynchronous writes:

{@code concurrency-executor-async-write}

And finally for cache expiration in reference based cache (hard, soft or weak cache):

{@code concurrency-executor-cache}

There is also question how many threads and what type of Executor one should use. MapDB uses
`ScheduledExecutorService <https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html>`__
to execute its tasks. Previous MapDB versions were starting threads directly, but that meant problem in restricted and managed environments
such as J2EE containers. It is possible to supply your own Executor directly, usually you can use one of factory methods in
``Executors``:

{@code concurrency-executor-custom}

TODO how many threads and executor selection

Executor keeps threads running on background. That means memory leak and could prevent JVM from shutting down.
Also background tasks would fail after DB has been closed. For that reason  ``DB.close()`` shutdowns
all Executors associated with DB instance, before it is closed. This way all background jobs are finished before
DB is closed.

So currently it is not possible to reuse Executor after DB was closed. It is also not possible to share Executor between
DB instances if one of them is closed. Solution for that is to provide Executor wrapper, which only shutdowns its own tasks,
but that is not implemented yet. TODO executor sharing

