What for?
==================

MapDB works fairly well in several situation. However it has several features and it might be hard to tailor it to
your needs. This chapter enumerates common usages and discusses best setting for each situation.
It is TL;DR for this manual.

TODO link to cheat sheet

Huge on-disk collections
---------------------------
Use data-pump to create huge BTrees. Data pump imports data in streaming fashion and its performance does not degrade
over time.

- use memory mapped files

TODO HashMap versus TreeMap

TODO discus how transactions prevent data corruption

TODO BTreeMap node fragmentation


In-memory collections
----------------------
MapDB can be great alternative to java.util collections (aka heap collections). It packs data more tightly so
it fits more data into same memory. And MapDB performance is great, it often outperforms heap collections even
when GC activity is low. Heap collections are usually limited by GC overhead to 1e7 entries, MapDB performance
is predictable and is only by available memory.

Recommendations:

- use correct serializers for entries, keys and values.

- disable transactions unless you need them. Uncommited data are kept on heap. Write-ahead-log means data are written twice.

- there is ``DBMaker.newMemoryDB()`` and ``DBMaker.newMemoryDirectDB()``. It uses on-heap ``byte[]`` versus
``DirectByteBuffer``. Both are not limited by GC, but there is difference in deployment and setting memory limits.
TODO memory limits

- MapDB does not shrink storage after data are deleted. You should run ``DB.compact()`` sometimes.

- There is data-pump for moving data around very fast. TODO make chapter on this

TODO discus on-heap mode


Key-value cache with expiration
----------------------------------
HashMap has build in expiration based on time-to-live and maximal cache size.

Alternative memory model
--------------------------

If your data are limited by GC, to move completely into MapDB

- concurrency and locks are unchanged

- TODO reference locality and spliting

- TODO atomic variables and atomic vars.



TODO subchapters about transactions and MVCC


TODO chapter about append-only store
