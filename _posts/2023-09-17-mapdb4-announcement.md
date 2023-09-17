---
title: MapDB 4 announcement 
layout: comments
tags: [Roadmap, 4.0]
ghcommentid: 4
---

I started work on MapDB 4; this blog post describes some details. The last major version, 3.0, was released a long time ago. I decided to wrap up my work on different prototypes and make a new major release.  
  
My major goal now is to leave out anything that is too complex to implement and maintain . Instead, I aim to neatly arrange realistic simple elements into a cohesive puzzle that functions smoothly. I am somewhat giving up on the "large database idea" while returning to the root that made JDBM3 and MapDB 1.0 great.
  

### Goals for the new release

- MapDB is an alternative memory model. It can store data on disk and work as a database engine, but the main goal is to be a simple drop-in replacement for Java collections. It should make processing data that does not fit into memory easy.

- Designed around Spliterator and Parallel Streams from Java 8. There are consumer CPUs with 128 cores; let's make them usable.

- Very fast data ingestion (imports). MapDB 1.0 had Data Pumps that could create BTreeMap in a very fast way. I would like to extend this idea. It will be possible to load a data file, perform transformations using parallel streams, and save it into new writable collections. All very, very fast, using append-only operations. The goal is to saturate SSD speeds.

- Cheap snapshots. It will finally be possible to take a snapshot of a collection and use it to make backups or for data processing. It will work well with Parallel Streams and fast ingestion.

- Fix write amplification issue in BTreeMap and other collections. Group multiple writes together into single batch.

- Simplicity and maintainability. I am fixing all design mistakes that did not work in older versions.

### What is not there
  
MapDB V4 will be in a single Java package again. It will also use no dependencies (no Eclipse Collections, no Guava...). I am even dropping Kotlin from production code (it will still be used for unit tests). The new version will be a single JAR file that only depends on Java 8 or newer.

Packaging will change to make it possible to use older versions in parallel. The package name will be org.mapdb.v4, and the Maven archetype name will be org.mapdb:mapdb-v4. I am also going back to Maven. It seems simpler for tricky stuff like OSGI annotations.  
   
I am also dropping all the tricky elements from storage. No dynamic recursive space allocation, no multi-sized records, and no alternative storage access methods (sorry, no RandomAccessFiles, ByteBuffers all the way).

In practice, it means three things:

- Memory-mapped files will work everywhere, even on tricky network file systems like CEPH.
- No need for Unsafe and unmap tricks; MapDB 4 will only use the vanilla Java 8 API.
- There will be a bigger space overhead for writable stores.

Serializers will also be removed. Now, collections will not directly "see" records, such as BTreeNode, but will have to perform "actions" at the Store on top of binary data. This will allow to store all modification in an event log, and use it to replay snapshots.   
  
And finally all concurrency design is greatly simplified. MapDB will be thread safe, and use single ReadWriteLock per collection. No more segmented and structural locking. It will be still insert data using multiple threads, but that will be done at caching layer.  
  
That is all for now, more details in next few days.  