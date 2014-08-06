Benchmarks
========

<script src="js/jquery-1.8.3.min.js" />
<script src="js/excanvas.js" />
<script src="js/js-class.js" />
<script src="js/bluff.js" />
<script src="js/benchmarks.js" />

In memory
-------------

Graphs bellow compare off-heap in-memory `BTreeMap` from MapDB and `ConcurrentSkipListMap` from JDK. 
MapDB speed is comparable to Java collections, despite using serialization and its own 
memory management. 

Test was performed on HP Proliant 585 G2 with 24 physical cores and 32GB memory. 
Y-Axis is thousands operations per second, higher is better. 
Data set size is 100 millions. Key size is 8 bytes, value size is 16 bytes (`Map<Long,UUID>`).


Read-only lookups. MapDB is comparable to Java collections:

<span id="memoryRead" />

Random updates. MapDB scales upto 6 cores, than concurrency overhead increases and limits its performance:


<span id="memoryUpdate" />


Combined random updates (33%) and lookups (66%):

<span id="memoryCombined" />


Raw benchmark results are available [here](benchmarks-raw.html). 
Benchmark source code is [here](https://github.com/jankotek/mapdb-benchmarks) 
and [here](https://github.com/jankotek/mapdb-benchmarks/blob/master/src/org/mapdb/benchmarks/InMemoryUUIDTest.java)

There is no trick here. MapDB is greatly optimized. With large keys/vals the deserialization overhead would 
increase and MapDB would become slower compared to heap collections (we are working on partial deserialization to fix it).

On other side larger data set would increase GV overhead and make on-heap Java collections slower. 
In this test JVM had enough memory and GC was bellow 1%. 

In general MapDB is about 30% compared to on-heap. But it fits more data per GB and does not degrade its performance 
over large data sets. 


On Disk
-------------


Will be added soon...