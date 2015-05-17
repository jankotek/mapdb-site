Foreword
=========

MapDB is an embedded database engine for java. It provides collections backed by on-disk or in-memory storage.
It is flexible, simple and free under Apache 2 license. MapDB is extremely fast, as in-memory mode
rivals on-heap ``java.util`` collections and on-disk mode outperforms databases written in C. And it has several
configuration options from regular ACID transactions to very fast direct storage.

MapDB started in 2001 under the name JDBM. The current MapDB2 is its 5th generation.
It evolved as a db engine focused around Java. It is pure-java, implements Maps, has only single 500KB jar,
and runs everywhere, including Android and JDK6.
Also, its serialization lifecycle and separate memory allocator
makes it easy to port existing data models (such as collections) to database engine,
outside of Garbage Collector influence.


This manual is a work in progress and it will be completed together with the MapDB 2.0.0 release.
I hope you will find it useful. I would be very happy to accept
`pull requests <https://github.com/jankotek/mapdb-site/tree/master/doc>`__

