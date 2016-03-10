MapDB: database engine
-----------------------

MapDB provides Maps, Sets, Lists, Queues and other collections backed by off-heap or on-disk storage.
It is a hybrid between java collection framework and embedded database engine. It is free and open-source under Apache license.
And it has expiration, compression, ACID transactions, snapshots, incremental backups...
`Learn more → <doc/intro/>`_


Code
~~~~~~~~~~~~~

.. literalinclude:: src/test/java/doc/hello_world.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8

MapDB implements Java interfaces such as ``ConcurrentMap`` and is configured by maker pattern. Find out more in
`quick start → <doc/getting-started/>`_


.. TODO better syntax highlight


..
    Use cases
    ------------

    MapDB is great in following scenarios:

    - Java collection alternative with highly efficient memory usage and no GC overhead.

    - Large in-memory cache with disk overflow

    - Analytical engine for data processing, machine learning

    - Traditional database with Spring integration, JTA support...

.. TODO write use cases and their links

..
.. MapDB has several users, find out `what they say → <success.html>`_

News
~~~~~~~~
.. postlist:: 6
   :list-style: circle
   :format: {title} on {date}

Follow news on `RSS <http://www.mapdb.org/blog/atom.xml>`__ ,
`Mailing list <https://groups.google.com/forum/?fromgroups#!forum/mapdb-news>`__
or `Twitter <http://twitter.com/MapDBnews>`__




.. toctree::
    :hidden:

    format
    success
    changelog
    benchmarks
    faq
    format
    success
    credits
    doc/index.rst