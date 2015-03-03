MapDB
===========

MapDB is embedded database engine for java. It provides java collections backed by on-disk or in-memory storage. 
It has great performance and many configuration options. Its not limited by garbage collector and in-memory 
collections outperform their on-heap counterparts such as ``java.util.HashMap``. MapDB has tiny overhead
and excelent vertical scalability. It runs from Android phone to multi CPU server with several terabyte stores. 

MapDB is old project. It started in 2001 under name JDBM. Originally it was storage engine for astronomical application. 
Over years it evolved, got new features and better performance. It started as a hobby project, and eventually turned 
into full time paid job, however it retains its qualities:  carefully crafted compact and quality code. 
Current MapDB 2.0 is 5th generation of this project. 

MapDB followed different evolution than other databases. Rather than db engine it could be better described
as alternative memory model for java. There are several components: storage engines, memory allocator, serialization, trees,
caches... Those can be used together, or independenly, swapped, wrapped etc. MapDB has very modular and flexible design
so it is very easy to implement your own things such as RAID or in-memory cache with on-disk overflow. 

MapDB also has concurrent design in sense that it allows multiple threads to write (and read) data in parallel. Default configuration
scales linearly to 4 CPU cores (parallel updates). However with different configuration (and trade-offs) it scales well even on 20+ cores. 

And finally MapDB has very little overhead (and consequently great performance). It was originally designed to run on 486 CPUs and it retained
much of its qualities. Internal logic is represented using primitive variables, there is almost zero trash for GC. 
And unlike other dbs it does not have block page layer, updates and reads require almost zero copy. And finally
its serialization lifecycle takes good advantage of JVM qualities, so its performance can be compared to low level engines written in C 
(MapDB is pure java). 