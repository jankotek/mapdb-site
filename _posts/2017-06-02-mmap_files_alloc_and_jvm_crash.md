---
title: Memory mapped files, allocation and JVM crash
layout: comments
tags: [mmap]
---

I have some experience when it comes to memory map (mmap) files and database storage/
Long time ago I added mmap storage to [H2 SQL database](http://www.h2database.com).
MapDB was first pure java db to really utilize memory mapped files.

There is [older article about mmap files](http://www.mapdb.org/blog/mmap_file_and_jvm_crash/) I wrote last year.
This article summarises experience I had with mmap files so far.
It also outlines new approach I use in [IODB](https://iohk.io/blog/scorex/iodb-storage-engine/), 
a storage engine developed for blockchain applications. 
This work is developed for and sponsored by [Input Output HK](https://iohk.io/about/).

# Common problems with mmap files

Let's start with common issues memory mapped (mmap) files have. 
They were part of Java for 15 year since 1.4 release. 
But mmap implementation is  far from perfect. 
JVM has number of unfixed bugs related to memory mapped files. 
One of them is [15 years old now](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154), 
there is small group of fans who celebrate its birthday every year ;-)

Those issues are: 

## Limited number of mmap handles
There is limited number of mmap handles per process (64K on Linux),
If you use too many handles, JVM process will not be able to mmap new files. 
Usually your code will fail with exception:
```
Caused by: java.lang.OutOfMemoryError: Map failed
    at sun.nio.ch.FileChannelImpl.map0(Native Method)
    at sun.nio.ch.FileChannelImpl.map(FileChannelImpl.java:937)
```
But this failure may happen anywhere in JVM (when it loads new lib for example), 
and could even cause JVM crash.

You can count number of mmap handles process is using with this command: 
`sudo cat /proc/$PID/maps | wc -l` 

You can also increase number of mmap handles in `/proc/sys/vm/max_map_count`. 

## No unmap

Each mapped `ByteBuffer` uses one mmap handle. But there is no official way to release this handle.

There is no official way to close memory mapped buffer.
JVM releases file handles only after `ByteBuffer` gets garbage collected.
If GC is not invoked for some time (large free heap), you will run out of file handles.

Workaround is to use  *cleaner hack*. 
It uses reflection to access undocumented API that releases mmap file handle, before ByteBuffer gets garbage collected.

However the `ByteBuffer` can not be used after it was unmapped, any access to it lead to  invalid memory operation and causes JVM to crash. 
So cleaner hack should be used together with some locking mechanism, that makes `ByteBuffer` inaccessible after it was closed.


## Windows file delete

If file region was mmaped on Windows, it is not possible to delete that fil.
File remains undeletable, even after it was correctly closed, unmapped and JVM process exits. 
To delete this file, one has to restart operating system (anyone remembers deleteAfterRestart hook in Delphi? :-))
This [was reported](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4715154), but JVM team can not solve it, 
since it is *Windows feature*.

This problem is so annoying, that we recommend not to use mmap files on Windows.

## JVM crash when disk gets full

It is possible to crash JVM using only official API:

- create empty file
- mmap large section of this file for write
- write into mmaped `ByteByffer`
- operating system will keep modified memory pages for delayed write
- if delayed write fails, OS will send SIGBUS signal to JVM process
- JVM process does not handle SIGBUS signal, so JVM process gets killed by Linux kernel

One would think IO errors are rare. 
But it is very common situation when free disk space runs out. 
Most operating systems (such as Linux) use sparse files, 
that means no disk space is really allocated until first write.
If operating system fails to allocate space (no free space on disk), delayed write fails and the JVM gets killed. 

There is old bug report [opened for MapDB](https://github.com/jankotek/mapdb/issues/442). 
It was  solved by preallocating space, when file expands it is filled with `FileOutputStream` to ensure all file space 
is allocated. 
However this slows down MapDB inserts significantly,  and better solution is needed.
 
## mmap regions have fixed size, and do not expand

[FileChannel#map](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html#map-java.nio.channels.FileChannel.MapMode-long-long-) returns fixed size`ByteBuffer`. 
There is no way to change its size after file expands. 

MapDB solved this problem by mapping files in small buffers (1MB). However that uses too many file handles, and 
would cause crash when large stores are opened (>64GB).

Also it very slow on large files. It takes a long time to mmap a few thousands new `ByteBuffer` (1MB regions and 20GB file) when file is open. 
And it takes a long time to sync such file to disk (iterate and call `force()` over all `ByteBuffer`).


## 2GB limit per single region

`ByteBuffer` use 32bit signed integer for addressing, that limits size of single mmap region to 2 GB.  One has to mmap multiple regions to handle larger files. 


# New approach

Over time I learned better way to handle memory mapped files. Here are a few pieces of the puzzle:


## Read-only mmap, write with FileChannel

[LMDB](http://www.lmdb.tech/doc/) is key-value store which provides btree on top of mmap files. 
It is written in C, uses low level API and is pretty efficient. 
I recently studied its source code to see how its design could be reused in IODB. 

The most interesting is that LMDB **does not use mmap files for writing**. 
Files are mmaped in read-only mode and
uses `FileChannel` like method to write changes to disk. 
Once changes are written and synced, they are visible to read-only mmaped `ByteBuffer`.

This approach has some interesting features:

* There is no JVM crash if disk becomes full and delayed write fails

* `FileChannel` preserves order of writes; data are flushed to disk in the same order they were written to `FileChannel`. 
Write order is important to prevent data corruption. If LMDB would use mmap files for write, it would require  write-ahead-log exactly for crash protection. 

* Performance difference between writable mmap files and `FileChannel` is minimal if larger blocks are used. 

* This works extremely well for append-only files

So this could be a new way to write append-only files such as Write Ahead Log. 

## Truncated mmap files

Here is solution to another problem: how to increase size of mmaped `ByteBuffer`, if the underlying file expands, but buffers are fixed size?
MapDB maps file in 1MB increments, but that is very slow and requires too many resources (file handles).

Solution is very simple, and works pretty well on Linux. We mmap beyond the end of file:

* create empty file
* open writable `FileChannel` for this file
* use [FileChannel#map()](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html#map-java.nio.channels.FileChannel.MapMode-long-long-) to map 2GB read-only `ByteBuffer` over this file
* file size grows to 2GB, but it is sparse file, no disk space is actually used
* call [FileChannel#truncate()](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html#truncate-long-) to shrink file size back to zero

Now we have empty file, with large read-only `ByteBuffer` mmaped over it. 
We can safely expand file by writing with `FileeChannel`.
As file grows, the changes will be visible in `ByteBuffer`. 
Parts of file which are beyond enf-of-file are visible in `ByteBuffer` as zeroes. 

Once file grows over 2GB, we mmap another 2GB region and use `FileChannel#truncate()` again. 

In this example we used read-only mmap region. But with some care the writable mmaped `ByteBuffer` could be used. 
The most important is that `FileChannel` will write changes that cause file to expand.

This approach does not work on all operating systems. In Windows, the `FileChannel.truncate()` does not shrink file, 
`empty file` still has 1GB. This method can still be usable on Windows, we just use smaller regions (128MB vs 2GB) and 
 file can be truncated when store is closed (after unmap).

## Fast disk space allocation

Until recently I though that region of sparse file have only two states:

* Large file exists, its disk space is not allocated, data were not written yet
* Large file exists, its disk space is allocated and data were written

But apparently on Linux there is a third state, where the disk space is allocated, but no data were written yet.
There is `fallocate` call which [handles that](http://man7.org/linux/man-pages/man2/fallocate.2.html)

In JVM we can not access this call directly. There are other options: 

* We could use JNA call, for example in [this library](https://github.com/Nithanim/mmf4j).
* Another option is to execute `/usr/lib/fallocate` command. 
* And finally the `FileChannel.transferFrom()` from sparse file seems to have [the same effect](https://groups.google.com/forum/#!topic/mechanical-sympathy/UMrKt75yOmg
).

Why this is excellent? If we have sparse file in this allocated state, the delayed write can not fail when disk is full. 
So when the file expands, we do not have to overwrite new region with zeroes, but can use `fallocate` instead.
That is much faster. 