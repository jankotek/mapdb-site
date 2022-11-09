---
title: MapDB storage and design change
layout: comments
tags: [storage]
ghcommentid: 3
---

This article outlines recent changes in MapDB 4 development. I decided 
to refactor Serializer interface and merge it into Store. 

### Write amplification and allocation problem

MapDB has big problem with write amplification. 
Random inserts into BTreeMap trigger cascade of memory allocations, 
and disk space fragmentation.

One solution is data structure designed to absorb large amount of writes gracefully.
I  experimented with Fractal Trees, where each tree node has buffer for storing changes.  However, compaction algorithm for Fractal Trees is beyond my skills (I will write another blog post about this adventure). 

Another way to solve write amplification are append-only log files. 
It has many great features: snapshots, versioning, logs are streameable over network. 
But this requires a lot of disk space,  and is useless for memory based systems. 

After some failed experiments,  I got key criteria for new storage engine:

- Fixed page size (such as 1024 bytes)
- Very simple free space management, no recursion... 
- Absorb large amount of writes into single node, at expense of temporary slower reads
- Combine append-only log files, with update-on-place for space efficiency (RAM is limited)
- Simple compaction, it must operate in tiny chunks, no dependency between pages
- Support streaming, versioning
- Can not be tied to single data-structure (such as tree). Most work on all data types such as JSON documents, arrays...   
- Multiple write operations must merge into single write operations. For example +1+1+1 becomes +3   

### Store, Serializer, Collection 

To make it work, I needed one big change in design. 

`Store` interface in MapDB is essentially simple key value map: `Map<Pointer,Value>`.
Store does not know what data type its stores. 
If BTreeMap wants to perform some operation (such as insert new key into dir node), 
it has to take value from Store, modify it, and put new value back to store.  

That generates too much IO, I tried many hacks to make it faster for special cases (such as sorted arrays),  but ultimately that is way too complex.

Another problem with take-modify-put-back approach is streaming. Write operations should produce small deltas.
I tried to compare two binary versions, compress them with common dictionary... Obviously that did not work. More hacks.

Solution  is simple design change! Take data interpretation away from data structures (collections) and move it into Store. So BTreeMap can insert 
new key into btree node, but should not know if node is represented as sorted array. This case hides data types (such as BTree dir node) from collection (BTree). 


This simple change  also greatly simplifies MapDB store design. 
It eliminates unholy trinity  of Serializer-Collection-Store. 
In BTreeMap I had to do many tricks with generics to hide
from BTreeMap that dir nodes may be Long[] (or long[], ByteBuffers, or byte[]...).   

### Page Log Store

Now I can finally use storage format, that solves write amplification problem. 

Most databases use fixed size pages. Unused space on page is left empty, to accommodate 
future writes. So if new key is inserted into BTree page, it does not trigger new 
space allocation (like in MapDB 3), but simply uses free space on page. 

Another cause of write amplification are array insertion 
BTree page uses sorted array, insert in middle has to resize and overwrite entire array (page). Constantly resizing and rewriting array causes write amplification.  
My solution is to leave original array untouched, and write delta log into free space after main array.
 
So that is the trick. New store design uses free space on pages to write delta log. 
This delta log is latter merged with main data on page by background worker. 

This has some key benefits:

- several updates of same page in short interval, are merged together into single write (if several keys are inserted into BTree node, sorted array is only updated once with all changes)

- it reduces write amplification by several magnitudes at expense of temporary slower reads (until background worker kicks in, reader has to replay write log to get most recent version)

- there is no complex free space allocator (fixed size page), yet unused space on pages is not wasted

- "compaction" is very simple, localized and atomic; only single page IO. No traversal or inter-page dependencies

- read and write IO is localized on single page, great for cache locality etc... 

- it is universal, works on most shapes of data, not just sorted BTree nodes. For example JSON documents have similar write amplification problem, and can use delta logs

- opens road to new features (snapshots, isolation...)

- write log can be used for different scenarios (streaming over network, write ahead log...)

### What is next?

I am working on [proof of concept implementation](https://github.com/jankotek/mdb4-prototype). It will provide PageLogStore
and `SortedMap<Long,Long>` implementation.
After performance is verified, I will move to MapDB 4 development again. 


