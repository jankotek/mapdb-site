MapDB 3.0
===========

.. post:: 2016-01-13
    :tags: MapDB30 MapDB20 MapDB10
    :author: Jan


I will start with the bad news. Current 2.0 branch needs major redesign. It has bugs, concurrency issues and is slow. I decided to start work on new 3.0 branch. First milestone with HTreeMap will be released in a few days, stable version will be out on March.

There is not going to be stable 2.0 release. There will be another bug fix release for 1.0 and 2.0-beta, after that it will be all about MapDB 3.

I am not really in position to delay stable release any longer, but it has to be done to keep MapDB competitive. Basic concept and design in MapDB is 17 years old (JDBM started in 1999). Over years it got many improvements and is now almost perfect. But it is slow, inflexible, outdated and believe it is a dead-end.

My business is to sell consulting services around MapDB, and I can not deliver with current 2.0 branch. I need way to improve performance 100x, but with MapDB2 I could deliver at most 20% performance increase. Also writing things like Parallel Compaction in plain Java6 is just pain. I still believe that MapDB idea is solid. It just needs  a bit of reshuffling, to give it an edge and to ensure its bright future.

Changes in MapDB 3:
-------------------------

Most of MapDB is now written in Kotlin. Java6 was necessary for performance a few years ago, but Kotlin generates very nice bytecode and has most benefits of Scala without its bloat and complexity.

Deserialization overhead can now be bypassed with Binary Operations. Operations such as Binary Search on Tree Nodes can now operate directly on binary data. That gives huge performance boost, for example ``HTreMap.get()`` in M1 is 3x faster compared to MapDB 2.

There are way more tests. MapDB 3.0 tests concurrency with Thread Weaver, JVM crash with ``kill -9``, there are performance regression tests etc.. Current M1 has 70,000 unit tests. MapDB2 has 5x more functionality with only 3,000 unit tests. Final 3.0 will probably have couple of millions tests.

MapDB now has dependencies. It needs Guava, primitive GS collections, compression libs, bytecode generators... Total dep size should be under 15 MB.

MapDB will offer much more collections. There will be ArrayList, Bimaps, Multimaps, Dequeu etc..

It is now Java8 based. Java6 is still possible, but I see no reason. 95% of my customers are on Java8 and Android will support Java 8 soon.

All Git repositories are now merged into single repo. MapDB is now multi-module Maven project. Core MapDB should stay under 1MB, some components such as serialization now have separate module.

Release lifecycle is changing. MapDB will be developed in milestones and released every a few weeks. Each milestone will cover one feature and contain everything related to that feature (code, tests, benchmarks, optimizations). This way we should always have stable and almost-stable version available.

The way MapDB development is driven changed. Rather than implementing some theoretical idea, each milestone will cover one tasks. Current M1 provides HTreeMap with expiration to implement JCache. M2 will cover Sorted Maps for OSM, M3 will provide transactions for JTA implementation etc. This way new release stays relevant and gets tested better.

MapDB is now way more modular and flexible. For example MapDB is now split into several components: Expiration Queue, Index Tree, Leafs… The some code now powers collections such as Sparse List, Dequeue, ``SortedMap<Long,Object>``. It is also possible to do hybrid layout, such as Hash Table in flat mmap file, Expiration Queue onheap and Key Value Pairs in traditional Store.

MapDB will now provide its own primitive collections stored in mmap files etc. Checkout this `blog post <http://www.mapdb.org/blog/better_primitive_collections_proposal.html>`_. It will extend GS Collections (aka Eclipse Collections).

Release schedule:
-----------------------

MapDB 3.0 milestone 1 will be released in a few days. It covers ``StoreDirect`` and ``HTreeMap`` with expiration. It also provides offheap or ondisk JCache implementation. Current version is fairly optimized, but is missing some basic features such as store reopen (no persistence between JVM restarts).

Milestone 2 will add ``BTreeMap`` with Data Pump. BTree performance will be much improved with Binary operations, and Pump will be easier to use. Another addition is ``SortedTableMap``, which does binary search over flat file. ``SortedTableMap`` will support writes via Pump and merge. It is inspired by SSTable from Cassandra.

Milestone 3 will finally provide ability to reopen file store and persist data between JVM restarts. It will also add JVM crash resistance, durable commits, MVCC and transactions. It will also implement Java Transaction API.

Milestone 4 will add generic serialization and other bits from MapDB 1 & 2. At this point storage and API will be stabilized.

Stable 3.0 will be released in March after couple of milestones. It will have Long Term Support. After that I will work on submodules, such as Redis server, primitive mmap file collections, graph API etc...

Comments
------------
MapDB now has new `subreddit <https://www.reddit.com/r/mapdb>`_ for support and discussion. Mailing list will stay,
subreddit seems to work fine for Redis, so MapDB will use it as well.
Discussion for this announcement is `here <https://www.reddit.com/r/mapdb/comments/40sdzw/mapdb_30_announcement/>`_.