---
title: MapDB 3 Release Candidate and near future
layout: single
tags: [Release, Roadmap]

---

MapDB 3.0 Release Candidate 1 [is out](/changelog). In about two weeks it will become stable release. Right now the priority is to fix bugs and improve documentation. Now lets talk about near future of MapDB.


MapDB 3.1
---------

There is [a list of issues on Github](https://github.com/jankotek/mapdb/issues?q=is%3Aissue+is%3Aopen+label%3A3.1) targeted for 3.1 release.

Current 3.0 release is missing some important features from 2.0 and 1.0 branches. Most of them are already designed and half implemented, but needs to be merged into stable version. 3.1 release will add all features from older version. Most notably:

-   Instance cache
-   Asynchronous writes
-   Compaction with transactions enabled
-   Store based on append-only log files with compaction
-   Concurrent transactions with MVCC and isolation

MapDB can be faster, 3.1 will add many optimizations including `sun.misc.Unsafe` memory access.

Finally I want to crack parallelism. That was main reason for switching to Kotlin and for using external libraries. Most background operations in 3.1 will be executed in parallel by multiple threads. That includes:

-   Async write file dump
-   Transaction conflict resolution
-   Compaction on `StoreDirect` and `StoreWAL`, it will also allow reads and writes while compaction is in progress
-   Compaction on append-only log files inspired by RocksDB

MapDB 3.2
---------

This release will expand number of supported collections. I want deeper integration with Eclipse Collections. MapDB will implement some of its interfaces such as `MultiMap`.

This release will add full support for Java8 Streams including Fork/Join framework and Parallel Collections. Even in current version it is faster to iterate over Map entries with `forEach` rather then with an Iterator in for-loop. This version will take full advantage of functional programming and optimizations it offers.

MapDB 3.2 will come with support for queues. Rather then providing several separate implementations, there will be `QueueMaker` to configure various options.

`BTreeMap` and `SortedTableMap` will get better streaming support. It will be possible to store modifications in separate map and latter merge them into main Map in an efficient way.

Finally 3.2 will add compressed bitmaps inspired by [Roaring bitmaps](http://roaringbitmap.org/).

External projects
-----------------

Most important side project is Elsa serialization &lt;https://github.com/jankotek/elsa&gt;, it is former `SerializerBase` and `SerializerPOJO` which were refactored from MapDB into separate project. There is no stable release yet, so priority for now is to make it stable. Also we need to improve documentation, and write some benchmarks to compare it with competitors (Kryo, Java Serialization...).

Another external project is [MapDB JCache implementation](https://github.com/jankotek/mapdb-jcache). Right now it works but is not documented and does not have stable release. So priority is to fix that.

I really like [Eclipse Collections](https://www.eclipse.org/collections/) (former Goldman Sachs Collections). I will contribute many changes in there (Unsafe access, mmaped files...). Also I like their Parallel Collections, MapDB might implemented in future.

Next in line is off-heap Hazelcast. This project was in limbo for a long time but has good potential.

Long term future
----------------

You might be familiar with Data Pump and External Merge Sort in MapDB 1.0. It sorts dataset which does not fit into memory, by streaming and slicing it into smaller sets.

I believe Spark Datasets provides better and more universal semantic to do the same. Spark splits data into smaller chunks on multiple machines, but the same approach can be used on single machine with disk swap files.

So in future MapDB will implement API similar to Spark Datasets. It will allow MapDB to manipulate data sets larger than memory on single machine. Not just for sorting, but also for graphs, machine learning, matrix multiplications and so on. Stay tuned, there will be separate blog post about this.
