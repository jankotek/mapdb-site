MapDB: database engine
-----------------------

MapDB is an open source (Apache licensed), embedded Java database engine and collection framework.
It provides Maps, Sets, Lists, Queues, Bitmaps with range queries, expiration, compression and streaming.
MapDB is probably the fastest Java database, with performance comparable to ``java.util`` collections.
It also provides advanced features such as ACID transactions, snapshots, incremental backups...
`Learn more → <doc/intro.html>`_

MapDB implements traditional interfaces such as ``ConcurrentMap`` and is configured by maker pattern:

.. literalinclude:: src/test/java/doc/hello_world.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

`TODO better syntax highlight`

Use cases
------------

MapDB is great in following scenarios:

- Java collection alternative with highly efficient memory usage and no GC overhead.

- Large in-memory cache with disk overflow

- Analytical engine for data processing, machine learning

- Traditional database with Spring integration, JTA support...

`TODO links`

MapDB has several users, find out `what they say... <success.html>`_

News
----
.. postlist:: 6
   :list-style: circle
   :format: {title} on {date}
   :sort:

Follow news on `RSS <http://www.mapdb.org/news.xml>`__ \|
`Mail-List <https://groups.google.com/forum/?fromgroups#!forum/mapdb-news>`__
\| `Twitter <http://twitter.com/MapDBnews>`__


MapDB is used by number of projects, checkout :doc:`changelog`

Doc
--------

.. toctree::
   doc/index


Content
------------

.. toctree::
   format
   success