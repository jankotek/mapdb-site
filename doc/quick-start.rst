Quick Introduction
=====================

MapDB is flexible, with many configuration options. But in most cases, it is configured with just a few lines of code.

*TODO more resources: cheat sheet, examples, KATA...*

Get it
-------

MapDB binaries are hosted in Maven Central repository. Here is dependency fragment for MapDB.

.. code:: xml

    <dependency>
        <groupId>org.mapdb</groupId>
        <artifactId>mapdb</artifactId>
        <version>VERSION</version>
    </dependency>

``VERSION`` is the last version number from `Maven Central <http://mvnrepository.com/artifact/org.mapdb/mapdb>`_.
You can also find the current version on this image:

.. raw:: html

    <img src="https://maven-badges.herokuapp.com/maven-central/org.mapdb/mapdb/badge.svg"/>


Daily builds are in snapshot repository. The version number for the latest snapshot is `here <https://oss.sonatype.org/content/repositories/snapshots/org/mapdb/mapdb/>`_.

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
            <version>VERSION</version>
        </dependency>
    </dependencies>

You can also download MapDB directly from Github. MapDB depends on Eclipse Collections and Guava.
*TODO host tars on github*

Hello World
--------------------
Hereafter is a simple example. It opens in-memory HashMap, it uses off-heap store and it is not limited by Garbage Collection:

.. literalinclude:: ../src/test/java/doc/hello_world.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8

HashMap (and other collections) can be also stored in file. In this case the content can be preserved between JVM restarts.
It is necessary to call ``DB.close()`` to protect file from data corruption. Other option is to enable transactions with write ahead log.

.. literalinclude:: ../src/test/java/doc/hello_world_file.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8

*TODO Hello World examples do not cover the commits.*

By default, MapDB uses generic serialization, which can serialize any data type.
It is faster and more memory efficient to use specialized serializers.
Also we can enable faster memory-mapped files on 64bit operating systems:

.. literalinclude:: ../src/test/java/doc/hello_world_serializers.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8

Example projects
--------------------
*TODO example projects*

Quick Tips
---------------

- Memory mapped files are much faster and should be enabled on 64bit systems for better performance.

- MapDB has Pump for fast bulk import of collections. It is much faster than to ``Map.put()``

- Transactions have a performance overhead, but without them the store gets corrupted if not closed properly.

- Data stored in MapDB (keys and values) should be immutable. MapDB serializes objects on background.

- MapDB needs compaction sometimes. Run ``DB.compact()`` or see background compaction options.
