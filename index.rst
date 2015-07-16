MapDB
=================================

MapDB is embedded database engine. It provides Maps and other collections backed by disk or memory storage.
It offers excellent performance comparable to java collections, but is not limited by GC overhead.
It is also a full database engine with storage backends, transactions, cache algorithms, expiration and many other options.
MapDB is very easy to use. It is a pure-java 500K JAR and only depends on JRE 6+ or Android 2.1+.
It is also very flexible, it exposes many low level optimizations, without sacrificing usability,

MapDB is free under **Apache 2 license** with no strings attached. There is active community on
`Github <https://github.com/jankotek/mapdb>`__. MapDB is sponsored by `consulting services <mailto:jan@kotek.net>`__.


News
----

-  2015-07-09 `MapDB 2.0-beta2 <http://www.mapdb.org/changelog.html#version-2-0-beta2-2015-07-09>`__ and
   `MapDB 1.0.8 <http://www.mapdb.org/changelog.html#version-1-0-8-2015-07-09>`__ released.
   Fixed JVM crash with mmap files enabled and other bugs.

-  2015-06-29 `MapDB 2.0-beta1 released <http://www.mapdb.org/changelog.html>`__.
   Format and API freeze for MapDB 2.0.

-  2015-06-16 `MapDB 2.0 is near <http://kotek.net/blog/MapDB_20_is_near>`__.
   Format and API freeze for MapDB 2.0 is just a few weeks away.

-  2015-06-16 `MapDB 2.0-alpha3 released <http://www.mapdb.org/changelog.html#Version_20-alpha3_2014-06-16>`__.
   Last release before stabilization

-  2015-02-19 `MapDB 1.0.7 released <http://www.mapdb.org/changelog.html#Version_107_2015-02-19>`__.
   Fixed WAL corruption and others.


-  2014-08-07 `MapDB 1.0.6 released <http://www.mapdb.org/changelog.html#Version_106_2014-08-07>`__.
   Fixed transaction log replay after unclean shutdown.


Follow news on `RSS <http://www.mapdb.org/news.xml>`__ \|
`Mail-List <https://groups.google.com/forum/?fromgroups#!forum/mapdb-news>`__
\| `Twitter <http://twitter.com/MapDBnews>`__


What for?
-------------
MapDB is great in following situations: TODO links to doc

- In-memory cache with expiration based on time-to-live or maximal size, Redis or EZCache replacement
- Huge indexes
- Persistent data model
- Alternative memory model if you are limited by GC

MapDB is used by number of companies, checkout :doc:`success`


Get started
------------

Add `maven dependency <http://mvnrepository.com/artifact/org.mapdb/mapdb>`_:

.. code:: xml

 <dependency>
    <groupId>org.mapdb</groupId>
    <artifactId>mapdb</artifactId>
    <version>1.0.8</version>
 </dependency>

There is also newer and faster 2.0-beta (replace X with current version):

.. code:: xml

 <dependency>
    <groupId>org.mapdb</groupId>
    <artifactId>mapdb</artifactId>
    <version>2.0-betaX</version>
 </dependency

And simple in-memory example:

.. code:: java

    import org.mapdb.*;

    DB db = DBMaker.newMemoryDB().make();

    ConcurrentNavigableMap treeMap = db.getTreeMap("map");
    treeMap.put("something","here");

    db.commit();
    db.close();

Continue at :doc:`doc/getting-started` chapter in documentation.


Documentation
-----------------

- :doc:`doc/index`
- :download:`Manual in PDF <down/mapdb-manual.pdf>`,
- :download:`Cheat Sheet <down/cheatsheet.pdf>`,
- `Javadoc <http://www.mapdb.org/apidocs/>`_
- `Examples <https://github.com/jankotek/MapDB/tree/master/src/test/java/examples>`_


.. toctree::
    :maxdepth: 1

    benchmarks
    changelog
    credits
    features
    success
    contents



Support
--------------

First please try usual search, read documentation and Frequently Asked Questions:

.. toctree::
    :maxdepth: 2

    doc/index
    faq

For general questions there is `mailing list <https://groups.google.com/forum/#!forum/mapdb>`_.
You may subscribe without GMail account by `sending email <mailto:mapdb+subscribe@googlegroups.com>`_.

Bug reports are hosted on `Github <https://github.com/jankotek/mapdb/issues>`_.

Finally there are `consulting services <mailto:jan@kotek.net>`_ for MapDB.

I usually respond to non-paying users within a few days via email.
Paid customers get faster response times with optional video-conferences.
I am located in Europe, but usually work late evenings.