Concurrency
=============

All classes in MapDB are thread-safe and to some extend vertically scalable. 
Reads should be linearly scalable without limitations. 
Writes should scale to 4 CPU cores linearly with peak around 6 cores. 
Greater write scalability is possible with sharding or experimental memory allocator. 

However MapDB design is relatively simple. There are global locks and stop-word states.
It is also flexible so it can be tunned well into most situations and  
performance / safety tradeoffs. 

Segment locking
~~~~~~~~~~~~~~~
Here is basic explanation how MapDB can scale under concurrent access.
{@javadoc Engine} implements key-value storage. Instead of single global lock 
it is splited into multiple segments, each withs its own ``ReadWriteLock``

This way records can be updated concurrently to some extend. 
Concurrent read operations are not blocked unless update is running. 

Inside segment locks is global lock called 'structural lock' which 
protects memory allocator. It is locked when record layout changes,
for example space is allocated, record was resized or free space released.

{@code segment-locking}

Memory allocator under global lock limits vertical scalability, so there 
are various workarounds. 
For exemple each segment can have its own storage file, so no shared state exists.


Collection locking
~~~~~~~~~~~~~~~~~~~

On top of ``Engine`` locking are implemented collections. Each collection 
add its own locking to ensure that its data (tree nodes, pointers etc) are consistent. 

``BTreeMap`` (aka TreeMap) is lock free BTree implementation. It only locks single node 
on update and has excelent vertical scalability. 

``HTreemap`` splits its data into 16 segments based on key hash. Number of segments
is hardcoded into its design and can not be changed. However its has simpler 
design compared to ``BTreeMap`` and more predictable performance. 
It is also easy to use separate storage for each segment top boost its performance.

Sequential safety
~~~~~~~~~~~~~~~~~~

Map.put() translates into multiple operations at ``Engine`` level.
Tree must be updated at more levels, counters incremented etc. 
Under some configurations this might not be atomic operationm and could 
cause data inconsistency. 

For example two commits might happen while tree is being updated,
second commit is rolled back and tree becomes inconsistent. 
Or inconsitent snapshot is taken while tree is updated, 
that would cause inconsistent backup, while primary data would be fine.

There are two ways to solve this. First are sequential safe collections 
(usually lock-free) which can handle inconsistency at expense of fragmentation and lower performance. 
``BTreeMap`` is sequentially safe, its nodes are updated in way which handles inconsistency.

Second way is *Sequential Lock*. It is ``ReadWriteLock`` provided by ``DB.getSequentialLock()``.
Sequentially unsafe operations (such as ``HTreeMap.put()``) should be performed under read lock.
Consistent state for snapshot, commit or close operation should be done under write lock.   
TODO sequential lock
