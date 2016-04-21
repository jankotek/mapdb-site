Quick introduction
=====================

MapDB is flexible with many configuration options. But for most cases it is configured with just a few lines of code.

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
You can also find current version on this image:

.. raw:: html

    <img src="https://maven-badges.herokuapp.com/maven-central/org.mapdb/mapdb/badge.svg"/>


Daily builds are in snapshot repository. Version number for last snapshot is `here <https://oss.sonatype.org/content/repositories/snapshots/org/mapdb/mapdb/>`_.

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
Hereafter is a simple example. It opens in-memory HashMap, it uses off-heap store and is not limited by Garbage Collection:

.. literalinclude:: ../src/test/java/doc/hello_world.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8


Another option is to use file storage Content of HashMap is preserved between JVM restarts.
In this case ``DB`` object has to be closed, to close and release underlying file:

.. literalinclude:: ../src/test/java/doc/hello_world_file.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8

*TODO Hello World examples do not cover commits.*

By default MapDB uses generic serialization which can serialize any data type.
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

Quick tips
---------------

- Memory mapped files are much faster and should be enabled on 64bit systems for better performance.

- MapDB has Pump for fast bulk import of collections. It is much faster then to ``Map.put()``

- Transactions have performance overhead, but without them store gets corrupted if not closed correctly.

- Data stored in MapDB (keys and values) should be immutable. MapDB serializes objects on background.

- MapDB needs compaction sometimes. Run ``DB.compact()`` or see background compaction options.
