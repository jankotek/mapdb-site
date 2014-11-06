`Quick overview podcast <http://youtu.be/_KGDwwEP5js>`__
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

|Quick overview podcast|

Learn more about MapDB in quick video podcast.

`Learn more <02-getting-started.html>`__
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

|Learn more|

MapDB provides concurrent Maps, Sets and Queues backed by disk storage
or off-heap memory.

`Benchmarks </benchmarks.html>`__
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

|benchmarks|

MapDB is probably the fastest pure java database. Checkout our
benchmarks.

`Features </features.html>`__
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

|features|

CRUD, off-heap, append only store.. we have them all. MapDB is highly
modular and flexible

--------------


Features
--------

-  **Concurrent** - MapDB has record level locking and state-of-art
   concurrent engine. Its performance scales nearly linearly with number
   of cores. Data can be written by multiple parallel threads.

-  **Fast** - MapDB has outstanding performance rivaled only by native
   DBs. It is result of more than a decade of optimizations and
   rewrites.

-  **ACID** - MapDB optionally supports ACID transactions with full MVCC
   isolation. MapDB uses write-ahead-log or append-only store for great
   write durability.

-  **Flexible** - MapDB can be used everywhere from in-memory cache to
   multi-terabyte database. It also has number of options to trade
   durability for write performance. This makes it very easy to
   configure MapDB to exactly fit your needs.

-  **Hackable** - MapDB is component based, most features (instance
   cache, async writes, compression) are just class wrappers. It is very
   easy to introduce new functionality or component into MapDB.

-  **SQL Like** - MapDB was developed as faster alternative to SQL
   engine. It has number of features which makes transition from
   relational database easier: secondary indexes/collections,
   autoincremental sequential ID, joins, triggers, composite keys...

-  **Low disk-space usage** - MapDB has number of features
   (serialization, delta key packing...) to minimize disk used by its
   store. It also has very fast compression and custom serializers. We
   take disk-usage seriously and do not waste single byte.

.. |Quick overview podcast| image:: images/car-overview.jpg
   :target: http://youtu.be/_KGDwwEP5js
.. |Learn more| image:: images/car-intro.png
   :target: 02-getting-started.html
.. |benchmarks| image:: images/car-benchmarks.jpg
   :target: /benchmarks.html
.. |features| image:: images/car-features.png
   :target: /features.html
