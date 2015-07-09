MapDB
=================================

MapDB is embedded database engine. It provides java collections backed by disk or memory database store.
MapDB has excellent performance comparable to `java.util.HashMap` and other collections, but is not limited by GC.
It is also very flexible engine with many storage backend, cache algorithms,  and so on.
And finally MapDB is pure-java single 400K JAR and only depends on JRE 6+ or Android 2.1+.

MapDB development is sponsored by `CodeFutures Corporation <http://codefutures.com/>`__.
It is a leading supplier of database performance
tools that reduce the time and effort required to develop database applications and dramatically increase deployed database
scalability and performance. MapDB author works for CodeFutures on `next generation distributed database <http://codefutures.com/agildata/>`__.

News
----

-  2015-07-09 `MapDB 2.0-beta2 <http://www.mapdb.org/changelog.htm#version-2-0-beta2-2015-07-09>`__ and
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
    <version>1.0.7</version>
 </dependency>

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


For general questions there is `mailing list <https://groups.google.com/forum/#!forum/mapdb>`_. You may subscribe without GMail account by `sending email <mailto:mapdb+subscribe@googlegroups.com>`_.

Bug reports are hosted on `Github <https://github.com/jankotek/MapDB/issues>`_.

Finally last option is to `contact me directly <mailto:jan(at)kotek.net>`_. Unless you mark your mail as private,
I reserve right to forward technical questions and answers into mailing-list.

I usually respond to non-paying users within a few days. Paid support and consulting is provided by
`CodeFutures <http://codefutures.com/mapdb>`__ (or contact me directly).
