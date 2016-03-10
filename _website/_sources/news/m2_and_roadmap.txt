Milestone 2 is out and 3.0 roadmap
===================================

.. post:: 2016-02-12
    :tags: release 
    :author: Jan


MapDB 3.0 milestone 2 was released today.
M2 adds BTreeMap, Data Pump and new faster SortedTableMap. 

BTreeMap was reimplemented. New version is shorter, more compact 
and probably more robust. It also handles iteration faster and 
has faster range queries.

SortedTableMap is read-only and uses binary search over sorted table in file.
It is faster compared to BTreeMap and is designed to work well with Data Pump.

Links
----------
Maven dep for 3.0M2:

.. code:: xml

    <dependency>
	    <groupId>org.mapdb</groupId>
	    <artifactId>mapdb</artifactId>
	    <version>3.0.0-M2</version>
    </dependency>

Code examples are `here <https://github.com/jankotek/mapdb/tree/mapdb3/mapdb-site/src/test/java/doc>`_.
Please keep on mind that DB can only be created, and can not be reopened yet.
``SortedTableMap`` does not use DB and can be reopened.

Documentation for ``SortedTableMap`` is `on github <https://raw.githubusercontent.com/jankotek/mapdb/mapdb3/mapdb-site/doc/sortedtablemap.rst>`_.

BTreeMap
------------
Major news in BTreeMap is faster reverse iteration. Old version cached leaf nodes
and used ``findSmaller`` function to jump to next leaf node.
New reverse iteration code stores the directory nodes on stack, and greatly 
reduces number of IO operations needed for reverse iteration. 

Data Pump was redesigned. Now it takes data in ascending order (no longer reverse sorted). 
Also source data do not have to be provided in an iterator. MapDB now provides ``Consumer`` callback interface
with ``take`` and ``finish`` methods. New Data Pump is much easier to use, 
and accepts data from user loops, file parsers etc...

Major change in BTreeMap is *binary acceleration*. Old version suffered from 
deserialization overhead and read amplification. To read single entry from leaf node,
it had to deserialize parent directory nodes to perform binary search.
This created many instances and generated lot of GC garbage.

*Binary acceleration* (still looking for better name) bypasses the deserialization,
and performs binary search directly over binary data.
So far it is 3x faster over deserialization, but that will probably improve even more.

Less visible change is elimination of ``BTreeKeySerializer`` class. Its functionality was merged into ``Serializer`` class.
Now there will be names such as ``Serializer.STRING_DELTA`` (common prefix compression).

SortedTableMap
-----------------
There is new and shiny``SortedTableMap``. It takes lot of ideas and code from ``BTreeMap``
(delta compression, binary acceleration, value packing...) and is also inspired by Sorted String Tables from Cassandra.

Instead of directory it uses faster binary search over table. It is faster, but read-only.
It uses fixed-size pages, but supports variable length entries, compression and is fairly space efficient. 

It was inspired by Cassandra SSTables and is designed around Data Pump. 
Eventually it will support modifications by using secondary writeable map, and periodical merging of both maps.
I would like provide Map implementation which will make this process completely transparent to user.

So far ``SortedTableMap`` seems to be 5x faster compared to old ``BTreeMap``.
Again, that is not final number yet, I have not started optimizing code yet.

Benchmarks 
----------------

Here are `quick benchmark <https://github.com/jankotek/mapdb/tree/mapdb3/mapdb-benchmark/src/test/java/org/mapdb/benchmark>`_
results. It shows Map.get() on Map<Integer,Integer> with 10 million entries.

MapDB 3.0 is faster compared to older MapDB 2.0.
3.0 is not yet optimized and with default setting. I expect its numbers will improve at least 2x.

.. code:: xml

     ConcurrentHashMap_get - 282
     ConcurrentSkipListMap_get - 2211

     BTreeMap_get - 17054
     HTreeMap_get - 22214
     SortedTableMapBenchmark_get - 7069

     MapDB2_BTreeMap_get - 25886
     MapDB2_HTreeMap_get - 43968

Proper benchmarks will be provided in a few weeks after M3.

3.0 Roadmap
---------------

Current M2 has severe limitations, most notably it does not allow to reopen DB files.
But so far it delivered and verified most architectural changes needed for 3.0 release.

Right now 3.0 is two weeks behind schedule. Almost stable release-candidate is expected at end of March.
This date is fixed, and I will not compromise on quality, so some features might be postponed for latter release.

Next week there will be new ``2.0-beta13`` and ``1.0.9`` releases. Those will probably be the last
releases for 1.x and 2.x branches. I will migrate issue list to 3.0 and close old bugs.
After that support for older branches will be minimal (pull requests, critical bugs etc..).

Milestone 3 is expected in about ten days. Main goal is to port features, unit tests, documentation etc from older
branches. MapDB 3 will become much more usable in this milestone.
Also 3.0 branch will become master branch on Github and Mapdb.org website.

Milestone 4 will finalize and freeze API and storage format specification. It will also introduce crash resistance
for Write Ahead Log. It will also add some extra collections (IndexedTreeList, Deque...).
M4 is expected at beginning of March.

Milestone 5 will fix bugs and improve performance. Main focus will be on
unit tests and benchmarks. It will be released in March.

M5 should be followed by 3.0.0 release candidate.

Features not in 3.0
----------------------

Here is list of features, which might not be in 3.0 release:

- **Concurrent Transactions** (aka TxMaker). Doing it right is quite complicated.  I decided to move it
  into separate release. It will support MVCC and usual goodies,
  I think I will use Java Transaction API and will be in separate subproject.

- **Instance Caches** are practically made obsolete by Binary Acceleration. With extra dependencies
  we also have access to more sophisticated caching algorithms (LIRS). Instance Caches will be added in latter release.

- **Serializer POJO** is general serializer for DB object. If you specify no serializer while creating map,
  this one will be used. This is pretty solid serialization framework. I decided to move it into independent project
  outside MapDB. It will be usable as standalone and will be optional dependency for MapDB.
  Not sure if it will be done on time, so MapDB 3.0 might be without default serializer. That means serializer definition will be always required.




