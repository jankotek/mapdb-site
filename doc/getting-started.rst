Getting started
===============

MapDB has very power-full API, but for 99% of cases you need just two
classes:
`DBMaker <http://www.mapdb.org/apidocs/org/mapdb/DBMaker.html>`__ is a
builder style factory for configuring and opening a database. It has a
handful of static 'newXXX' methods for particular storage mode.
`DB <http://www.mapdb.org/apidocs/org/mapdb/DB.html>`__ represents
storage. It has methods for accessing Maps and other collections. It
also controls the DB life-cycle with commit, rollback and close methods.

The best places to checkout various features of MapDB are
`Examples <https://github.com/jankotek/MapDB/tree/master/src/test/java/examples>`__.
There is also
`screencast <http://www.youtube.com/watch?v=FdZmyEHcWLI>`__ which
describes most aspects of MapDB.

There is :download:`MapDB Cheat Sheet <../down/cheatsheet.pdf>`,
 a quick reminder of the MapDB capabilities on just two pages.

Maven
-----

MapDB is in Maven Central. Just add the code bellow to your pom file to use
it. You may also download the jar file directly from
`repo <http://search.maven.org/#browse%7C845836981>`__.

.. code:: xml

    <dependency>
        <groupId>org.mapdb</groupId>
        <artifactId>mapdb</artifactId>
        <version>1.0.7</version>
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

.. literalinclude:: ../../mapdb/src/test/java/doc/start_hello_world.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

This is a more advanced example, with configuration and write-ahead-log
transaction.

.. literalinclude:: ../../mapdb/src/test/java/doc/start_advanced.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

What you should know
--------------------

MapDB is very simple to use, however it bites when used in the wrong way.
Hereis a list of the most common usage errors and things to avoid:

-  Transactions (write-ahead-log) can be disabled with
   DBMaker.transactionsDisable(), this will speedup the writes. However,
   without WAL the store gets corrupted when not closed correctly.

-  Keys and values must be immutable. MapDB may serialize them on
   background thread, put them into instance cache... Modifying an
   object after it was stored is a bad idea.

-  MapDB relies on memory mapped files. On 32bit JVM you will need
   DBMaker.randomAccessFileEnable() configuration option to access files
   larger than 2GB. RAF introduces overhead compared to memory mapped
   files.

-  MapDB does not run compaction on the background. You need to call
   ``DB.compact()`` from time to time.

-  MapDB uses unchecked exceptions. All ``IOException`` are wrapped into
   unchecked ``IOError``. MapDB has weak error handling and assumes disk
   failure can not be recovered at runtime. However, this does not
   affect the data safety, if you use durable commits.


