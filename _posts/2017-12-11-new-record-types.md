---
title: New record types
layout: comments
tags: [4.0, Store]
--- 

`StoreDirect` in MapDB 3 is very simple key-value store.
It takes an object (record), serializes it into binary form, and stores it in file.
(it is bit more complex, but for purpose of this article...) 

Update mechanism for record is:
- serialize on-heap object into binary `byte[]`
- check if old record has the same size, if yes update on place (no allocations needed)
- if binary size differs, allocate new space 
- write record 
- release old space

Get operation is also trivial:
- it finds record position in file
- loads it into `byte[]`
- and deserializes it into onheap object

This approach kind of works. But in many usages you run into performance issues. 
Good example are BTrees: 

- to add single key, you need to rewrite entire BTree node and 
other keys, that are part of that node
- in counted BTrees (each dir node contains counter) you need to rewrite several nodes just to increment single `int`

It was suggested that BTrees are not good for disk storage application for this reason.

And once you get into more exotic data structures 
(linked list, hash trees, counted btrees, compressed bitmaps, queues...) 
similar problems become way too common. 
Data structure that works well on-heap (and on paper), becomes performance nightmare 
when implemented in database.  

Solution?
------------------

There are several workarounds for performance problems. 
Just BTree has dozens reimplementation to fix write and read amplifications.
I studied some, but I believe it just introduces  more complexity without
really fixing performance problems. 

Take a [Fractal BTree](https://en.wikipedia.org/wiki/Fractal_tree_index) for example.
It solves write amplification by caching the keys as part of top BTree nodes.  
There is quite complicated algorithm, where changes slowly trickle down from top nodes, to leafs

In MapDB I spend several months trying to solve performance issues, related to write and 
read amplifications. Partial deserialization, binary acceleration, hash produced from serialized data....
This introduced a lot of complexity into storage. When coupled with caching, transactions, 
snapshots... it is just nightmare.

But now I think problem is somewhere else. 
Problem is not with data structure design, 
but due to limited features of most database engines (or `Store` interface in MapDB). 

In MapDB I control full stack; serializers, caching, commits, binary formats, memory representation...
I can afford some creativity while inserting data. 

Rather than redesigning each data structure separately, 
I can identify common problems,
and redesign my database engine to handle those cases efficiently. 

And if I really have to implement data structure such as Fractal Tree, 
MapDB should have features to make it easy. 

New record types
-----------------

Currently MapDB has two record types:

- tiny records (<5 bytes) are packed into index table instead of offset
- single small record with size up to 64K
- linked record (linked list) with size up to 1GB (theoretically unlimited)

Store exposes those to outside world (to Serializers) as monolithic `byte[]`. 
Small records have zero copy optimizations (serializer reads directly from mmap file). 
But for huge linked record, I have to allocate and copy huge `byte[]` during deserialization.

So for start I decided to redesign how Serializer interacts with the Store and binary data. 
I want to break down monolithic `byte[]` and single step (de)serialization, 
into dance of callbacks and incremental changes. 

Some of those changes are already there. For example MapDB 3 has 'binary acceleration',
where BTreeMap performs binary search on its nodes, without full deserialization. 
But this redesign should address common problems, rather than adhoc optimizations. 

Also I want to make record types more abstract, with option to add new record types in future.
This should eventually handle stuff like compression or hashing, which are now part of serializers.


### Huge records (aka linked records)
Current linked records has following structure:
```
  R1 - first chunk of data, link to R2
  R2 - second chunk of data, link to R3
  R3 - third chunk of data, end of record
```

Deserialization is slow and requires two passes:
- traverse linked record to find its size
- allocate `byte[]`
- traverse linked recod again and copy it into `byte[]`
- pass `byte[]` to deserializer

In new form the links will be stored on first node:

```
  R1 - link to R2, size of R2, link to R3, size of R3...
  R2 - first chunk of data
  R3 - second chunk of data
  R4 - second chunk of data
```

This allows some interesting features:
- size can be determined by only reading first part
- no need to traverse entire set (traversing 2GB in 64KB chunks can take a long time) to find size
- deserialization can be incremental, only smaller chunks can be loaded into memory
- we can do on place binary modification (change Nth byte), without reinserting entire set
- binary record can expand in middle (just resize R3), so adding an element into middle of array is possible
- we can efficiently append to end of record, without reinserting old data


### Partial update

Take a [Counted BTree](https://github.com/jankotek/mapdb/issues/231) (Interval Tree) or Authenticated Skip List. 
- In both cases there is larger node, with a small counter. 
- Node is relatively static, but counter is updated frequently
- Node and counter should be collocated for performance reasons 
  - memory locality
  - deserialization needs to be fast
 
Solution is to store both node and counter in the same record, but on update overwrite only small fraction of binary record; 
- let say that node consumes 2000 bytes
- counter is first for 4 bytes of node
- update in counter always rewrites first 4 bytes 
- this is done using special action in serializer, with function passed to store on update    
- counter update does not require serialization of entire node
- deserialization (get operation) always sees most recent binary version
 

### Write Buffer

Fractal Trees have huge nodes (MBs), each node has write buffer which stores changes in keys.

Or in other terms, imagine large compressed array with frequent updates.
- Update on place is not practical (decompress, update and compress array again).
- Decompression is much faster than compression. 
- We store changes in write buffer
- every N updates we flush the write buffer
  - read old version
  - apply changes from write buffer
  - compress new version and update database
- get operation  
  - read old version 
  - apply changes from write buffer
  - return current version
  - performance is ok, decompression is much faster than compression
 
In binary terms it will be implemented in following way

- record has allocated space, lets say 64KB
  - this allocation rarely changes, one of the goals is to prevent frequent allocations
- first 50KB are reserved for array itself (static part)
- last 14KB are for write buffer
- write buffer is append only
  - there is pointer to end of buffer
  - new write operation (such as delete or add key) 
    - appends information to write buffer
    - and increases pointer
- once buffer is full (or some condition is met), record is rewritten and buffer is flushed  
       
       
### Virtual Records

Some records do not really store any information, but are just used as cache. 
Their content can be reconstructed from other records. 
Good example is counter attached to collection, 
it caches number of elements in collection so `Collection#size()` is fast,
its content is reconstructed by counting all elements in collection.

- Content is temporary and frequently updated
- Updates do not have to be durable, since content can be reconstructed
- Value of Virtual Record can be kept in memory, does not have to be stored in file
- Virtual Record can be excluded from Write Ahead Log (is reconstructed in case of crash)
- Virtual Record can not be stored on heap, but must be part of Store
  - Read only snapshots would conflict with multiple instances
  - Rollback needs to have access to Virtual Record to revert to older value
- Serializer can provide initial value 
  - for collection counter Serializer would need access to content of collection (possible cyclic dependency)  
 
  
### External File

StoreDirect does not support huge binary records well, even with new storage format. 
Frequent updates in large file may trigger allocator and cause fragmentation.
Also moving large records through Write Ahead Log will slow down commits. 
So it makes sense to optionally move Large Record into separate files. 

- binary Store contains only file name
- user can configure directory, where files will be stored 
  - relative or absolute
  - Map (index) can have keys on fast SSD, large values on slow spinning disk
- Transactions 
  - updated records creates new file
  - file sync runs on background
  - commit can sync multiple files in parallel
  - Write Ahead Log only contains new file name
    - is smaller, much faster log replay
    - large values completely bypasses WAl and does not have to be copied multiple times
- Deserialization can just use buffered `FileInputStream`
  - does not load all content in memory
  - much easier to partially modify, expand or shring record in external file 
- Option to specify file prefix and suffix
  - images (or other files) stored in MapDB can be opened directly by external programs 

### Compression

Currently compression is handled by serializer. That complicates caching etc. 
It makes sense to introduce compression as new type of record. 
This opens some interesting scenarious for backround oerations:

- `Store#update` will store values in decompressed form
- Background operation will compress those on backround
  - it might try different types of compressions (different dictionaries) to see which works best
- Background operation might even train new type of dictionary on background
  - and recompress entire store on background (works very well for XML chunks)
  
  
### Checksum
Right now the checksum is handled by serializer. It should be handled by store as new type of record  

Incremental updates
----------------------

Big priority for me is to support partial updates for Write Ahead Log. 
If large record is updated, only small part of it should pass through Write Ahead Log. 
         
Also if single part of record is updated frequently, multiple updates should be merged 
together and only every Nth update hits file. The changes should be cached in memory somehow.          
         
When file is stored in external file, and rollback is possible, there should be some
mechanism to update file incrementally. Perhaps provide `FileInputStream` with
write overlay stored in separate file.          
         
Changes in serializers
---------------------------         

Serializers in MapDB 3.0 already do more than just convert data from/to binary form:
- hash code
- comparators
- compact in-memory representation for arrays (`Long[]` versus `long[]`)
- delta packing
- binary search in binary form

Serializers will be redesigned in MapBD 4.0 to handle even more:

- provide diff between two objects for partial updates
- allocator size hints
- partial updates
- do more operations over binary data

Also it will be more flexible with less grouping
I will write separate blog post about those changes.  
         
What will be in MapDB 4.0
-------------------------------

- I want good support for large records from beginning
- DB and Makers will have extra option how to store records
  - it will be possible to compress values in maps, place them into external files...
- current large records (over 64KB) will get new format
- External File records will be there as well
- Store and Serializer will have some extra hints how to store record (place it in external file)
- StoreDirect format will change to support new record types in future 
  - extra byte, so 255 possible record types
- Basic compression and checksums
- More functions that operate directly over binary data  
         


Comments
-------------------

Frolov Aleksey • 2 years ago

Where is secondary key from mapdb v1/v2?