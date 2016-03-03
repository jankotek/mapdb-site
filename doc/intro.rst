Introduction
--------------

MapDB is an open source (Apache licensed), embedded Java database engine and collection framework.
It provides Maps, Sets, Lists, Queues, Bitmaps with range queries, expiration, compression and streaming.
MapDB is probably the fastest Java database, with performance comparable to ``java.util`` collections.
It also provides advanced features such as ACID transactions, snapshots, incremental backups...




MapDB is an embedded database engine for java. It provides collections backed by on-disk or in-memory storage.
It is flexible, simple and free under Apache 2 license. MapDB is extremely fast, it
in-memory  mode rivals ``java.util`` collections and on-disk mode outperforms databases written in C. And it has several
configuration options from regular ACID transactions to very fast direct storage.

MapDB started in 2001 under the name JDBM. The current MapDB2 is its 5th generation.
It evolved as a db engine focused around Java. It is pure-java, implements all tricky interfaces
such as ``ConcurrentNavigableMap``, has only single 500KB jar,
and runs everywhere, including Android and JDK6.
Also, its serialization lifecycle and separate memory allocator
makes it easy to port existing data models (such as collections) to database engine,
outside of Garbage Collector influence.

This manual is a work in progress and it will be completed together with the MapDB 2.0.0 release.
I hope you will find it useful. I would be very happy to accept
`pull requests <https://github.com/jankotek/mapdb-site/tree/master/doc>`__










Getting started
===============

MapDB has very power-full API, but for 99% of cases you need just two
classes:
`DBMaker <http://www.mapdb.org/apidocs/org/mapdb/DBMaker.html>`__ is a
builder style factory for configuring and opening a database. It has a
handful of static ``newXXX`` methods for particular storage mode.
`DB <http://www.mapdb.org/apidocs/org/mapdb/DB.html>`__ represents
storage. It has methods for accessing Maps and other collections. It
also controls the DB life-cycle with commit, rollback and close methods.

The best places to checkout various features of MapDB are
`Examples <https://github.com/jankotek/MapDB/tree/master/src/test/java/examples>`__.
There is also
`screencast <http://www.youtube.com/watch?v=FdZmyEHcWLI>`__ which
describes most aspects of MapDB.

`TODO cheat sheet`

.. There is :download:`MapDB Cheat Sheet <../down/cheatsheet.pdf>`, a quick reminder of the MapDB capabilities on just two pages.

Maven
-----

MapDB is in Maven Central. Just add the code bellow to your pom file to use
it and replace VERSION. Latest version number and binary jars can be found in
`Maven central <http://mvnrepository.com/artifact/org.mapdb/mapdb>`_.

There are two generations of MapDB, old 1.0 and newer 2.0. At this point I would recommend to use
2.0, despite its still beta, because it seems to be more robust.

.. code:: xml

    <dependency>
        <groupId>org.mapdb</groupId>
        <artifactId>mapdb</artifactId>
        <version>VERSION</version>
    </dependency>

We are working on the new generation of MapDB. It is faster and more reliable. The latest semi-stable build is at
`snapshot repository <https://oss.sonatype.org/content/repositories/snapshots/org/mapdb/mapdb/>`__:

.. code:: xml

    <repositories>
        <repository>
            <id>sonatype-snapshots</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.mapdb</groupId>
            <artifactId>mapdb</artifactId>
            <version>2.0.0-SNAPSHOT</version>
        </dependency>
    </dependencies>

Hello World
-----------

Hereafter is a simple example. It opens TreeMap backed by a file in temp
directory. The file is discarded after JVM exits:

.. literalinclude:: ../src/test/java/doc/start_hello_world.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

This is a more advanced example, with configuration and write-ahead-log
transaction.

.. literalinclude:: ../src/test/java/doc/start_advanced.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


What you should know
--------------------

MapDB is very simple to use, however it bites when used in the wrong way.
Here is a list of the most common usage errors and things to avoid:

- Transactions (write-ahead-log) can be disabled with
  ``DBMaker.transactionDisable()``, this will MapDB much faster.
  However, without WAL the store gets corrupted when not closed correctly.

- Keys and values must be immutable. MapDB may serialize them on
  background thread, put them into instance cache... Modifying an
  object after it was stored is a bad idea.

- MapDB is much faster with memory mapped files. But those cause problems
  on 32bit JVMs and are disabled by default. Use ``DBMaker.fileMmapEnableIfSupported()``
  to enable them on 32bit systems.

- There is instance cache which uses more memory, but makes MapDB faster.
  Use ``DBMaker.cacheHashTableEnable()``

- MapDB does not run compaction on the background. You need to call
  ``DB.compact()`` from time to time.

- MapDB file storage can be only opened by one user at time.
  File lock should prevent file being used multiple times.
  But if file lock fails to prevent it, the file will become corrupted when opened (and written into)
  by multiple users.


