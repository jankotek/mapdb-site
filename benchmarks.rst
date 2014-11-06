Benchmarks
==========

.. raw:: html

   <script src="js/jquery-1.8.3.min.js" />
   <script src="js/excanvas.js" />
   <script src="js/js-class.js" />
   <script src="js/bluff.js" />
   <script src="js/benchmarks.js" />

In memory
---------

Graphs bellow compare off-heap in-memory ``BTreeMap`` from MapDB and
``ConcurrentSkipListMap`` from JDK. MapDB speed is comparable to Java
collections, despite using serialization and its own memory management.

Test was performed on HP Proliant 585 G2 with 24 physical cores and 32GB
memory. Y-Axis is thousands operations per second, higher is better.
Data set size is 100 millions. Key size is 8 bytes, value size is 16
bytes (``Map<Long,UUID>``).

Read-only lookups. MapDB is comparable to Java collections:

Random updates. MapDB scales upto 6 cores, than concurrency overhead
increases and limits its performance:

Combined random updates (33%) and lookups (66%):

Raw benchmark results are available `here <benchmarks-raw.html>`__.
Benchmark source code is
`here <https://github.com/jankotek/mapdb-benchmarks>`__ and
`here <https://github.com/jankotek/mapdb-benchmarks/blob/master/src/org/mapdb/benchmarks/InMemoryUUIDTest.java>`__

There is no trick here. MapDB is greatly optimized. With large keys/vals
the deserialization overhead would increase and MapDB would become
slower compared to heap collections (we are working on partial
deserialization to fix it).

On other side larger data set would increase GV overhead and make
on-heap Java collections slower. In this test JVM had enough memory and
GC was bellow 1%.

In general MapDB is about 30% slower compared to on-heap. But it fits
more data into same memory and its performance does not degrade with
data size.

On Disk
-------

Will be added soon...
