---
title: MapDB in February 2021
layout: comments
tags: [4.0, status]
ghcommentid: 2
---

I resumed work on MapDB a few weeks ago.  
Public repo and alpha version should surface at the end of February.
Here are some notes about MapDB development for this month 

Project organization
----------

Many user like single jar with no dependencies. 
MapDB could easily have 20 MB jar with 20+ dependencies (like Eclipse Collections). 
So I decided to split MapDB into several smaller modules.
Each module will be hosted in separate github repo under [new organization](https://github.com/mapdb).

For now, I am working on basic `mapdb-core` module, it contains API  
and basic functionality equivalent to MapDB1 (minus TX): 

- BTreeMap and HashMap
- IndexTreeList (simulates flat array, but uses btree like structure)  
- queues   
- page based store with snapshots and transactions
- redesigned serializers


I am also slowly rebranding MapDB.
I picked cute little rocket back in 2012, but that is a bit obsolete now. 
Today it is time fir a brand new less dynamic logo [Map<D,B>](https://logocaster.com/logo-design/Map%26lt%3BD%2CB%26gt%3B?seed=601469432&compoundIndex=),
that captures good old boring Java values.


Licence will remain Apache 2  with no dual licensing, no proprietary parts and no other hooks.
It is the most effective business model for one man consulting business.
However, I will put less effort into documenting internals of my work (storage format specs, integration tests...), coauthor never materialized. 
In next few months I will probably start an LLC company. 


Store and serializers
-----------

Current `StoreDirect` is designed to save space, and it brings several layers and complexity.
I would like to support libraries like Protocol Buffers, Netty or GRPC.
It should be possible to send serialized data directly into network buffer with `ByteBuffer.put()`.

MapDB uses serializers with `DataInput` and `DataOutput` so using `ByteBuffer` is complicated.
After some thinking I decided to eliminate entire serialization layer 
and (de)serialize into `ByteBuffer`s directly.
 
There is **new page based store** inspired by LMDB. It uses fixed
size pages and tighly integrates with serializers. Data structure is aware of page size,
so  BTree Nodes will split in a way to fit onto page. 
Serializers will be also able to use free space on page, for example for write buffer, 
or sparse node arrays (faster random btree inserts).

`PageStore` is simpler to deal with. Compaction can use temporary bitmaps for spatial information, and will not require  
full store rewrite (like in MapDB1 and 3). Snapshots, transactions, copy-on-write etc will also be  
much easier to implement.

New `PageStore` requires `ByteBuffer`s. Older version supported other 
backends such as `RandomAccessFile` or `byte[]`, but that is gone. 32bit systems will have to use different store, from now on focus is on memory mapped files. 
Memory mapped files in Java are tricky (JVM crash with sparse files, file handles...). 
So this version will use very safe (and slower) defaults. 



Data structures
---------------

I am working on following data structures:

`IndexTreeList` uses sparse BTree like structure to represent a huge array (or list). In here it is used as hash table for HashMap
  
`HTreeMap` we all know and love. First version will be fixed size (specified at creating) and optionally use 64bit hashing. Concurrent segments are gone, did not really scale too well. 

`BTreeMap` - Older version used complicated non-recursive-concurrent map. It was based on paper I found impressive,
but it had complicated design.  This version comes with a simplified version (recursion, single ReadWriteLock, delete nodes).
Old concurrent version will move into separate module. 
Both version will share the same storage format (serializers) and will be interchangeable.

`CircularQueue`, `LinkedFIFOQueue`, `LinkedLIFOQueue` and `LinkedDequeue`. Simple queue implementations with single read-write locks. 
No support for maps yet for expiration yet, that will come in separate module (`mapdb-cache`). 

Concurrency
----------

I tried to reach some sort of concurrent scalability with my own design (segment locking, locking layers), that did not really work and is gone.

Instead, I merged locking into store, and data structure will have an option to lock individual 
records by their recid. That is sufficient to ensure consistency in concurrent environment. 

By default, all data structures will come in two flavors. With no locking or global read-write lock (single writer, multiple readers)

To support concurrent scalability on 16+ cores, all data structures will support Java 8 fork-join framework, parallel streams, producers and consumers.

Older version had data pump to import large BTrees, this will get extended to support other collections as well (HashMap, queues...).
It will be possible to analyze large map (10B entries) with 64 CPU cores and dump result into another collection at 2GBps

In future, I will focus on Java8 Streams and similar filtering APIs, to analyze data that do not fit into memory (external merge sort etc..)

Roadmap
--------
With breaks, I worked on storage engines for 12 years (H2, JDBM 23, MapDB 1234). 
I have many notes, unfinished branches, unit tests, emails, bug reports and ideas.  
So big tasks for February is to compile all this into some sort of list. 
At end, of February I will make an alpha release, to have something to work with.

Once code and ideas are out, I can get some feedback and establish roadmap. 
If everything goes well, we could have a beta release at an end of March.


Funding
---------

I support this project from my consulting work. It is enough to keep lights on, but things could move faster. 
If you feel like [support me on Github](https://github.com/sponsors/jankotek), or throw me some [crypto](mailto:jan@kotek.net).  

That is all for now.






