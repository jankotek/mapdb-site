.. MapDB documentation master file, created by
   sphinx-quickstart on Tue Nov  4 19:14:09 2014.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.


MapDB
=================================

MapDB is embedded database engine. It provides java collections backed by disk or memory database store.
MapDB has excellent performance comparable to `java.util.HashMap` and other collections, but is not limited by GC.
It is also very flexible engine with many storage backend, cache algorithms,  and so on.
And finally MapDB is pure-java single 400K JAR and only depends on JRE 6+ or Android 2.1+.

News
----

-  2015-02-19 `MapDB 1.0.7
   released <http://www.mapdb.org/changelog.html#Version_107_2015-02-19>`__.
   Fixed WAL corruption and others.


-  2014-08-07 `MapDB 1.0.6
   released <http://www.mapdb.org/changelog.html#Version_106_2014-08-07>`__.
   Fixed transaction log replay after unclean shutdown.


Follow news on `RSS <news.xml>`__ \|
`Mail-List <https://groups.google.com/forum/?fromgroups#!forum/mapdb-news>`__
\| `Twitter <http://twitter.com/MapDBnews>`__


What for?
-------------
MapDB is great in following situations: TODO links to doc

- In-memory cache with expiration based on time-to-live or maximal size, Redis or EZCache replacement
- Huge indexes
- Persistent data model
- Alternative memory model if you are limited by GC


Get started
------------

Add `maven dependency <http://mvnrepository.com/artifact/org.mapdb/mapdb>`_:

.. code:: xml

 <dependency>
    <groupId>org.mapdb</groupId>
    <artifactId>mapdb</artifactId>
    <version>1.0.6</version>
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
- `Javadoc <http://www.mapdb.org/apidocs/>`_
- `Examples <https://github.com/jankotek/MapDB/tree/master/src/test/java/examples>`_




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

I usually respond to non-paying users within a few days. Contact me if you need more direct support or consultation.
