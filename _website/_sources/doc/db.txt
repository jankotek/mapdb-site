DB and DBMaker
==============

MapDB is like a lego, and there are two classes which assemble pieces together:
To hold things together there are two classes: ``DBMaker`` and ``DB``.

`DBMaker <http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-d-b-maker/index.html>`__ handles database
configuration, creation and opening. MapDB has several modes and
configuration options. Most of those can be set using this class.

`DB <http://www.mapdb.org/dokka/latest/mapdb/org.mapdb/-d-b/index.html>`__
represents opened database (or single transaction session).
It creates and opens collections . It also handles
db lifecycle with methods such as ``commit()``, ``rollback()`` and
``close()``.


To open (or create) a store use one of ``DBMaker.xxxDB()`` static
methods. MapDB has more formats and modes, each ``xxxDB()`` uses
different mode: ``memoryDB()`` opens in-memory database backed by
``byte[]``, ``appendFileDB()`` opens db which uses append-only log
files and so on.

``xxxDB()`` method is followed by configuration options and ``make()``
method which applies all options, opens storage and returns ``DB`` object.
This example opens the file storage with encryption enabled:

.. literalinclude:: ../src/test/java/doc/dbmaker_basic_option.java
    :start-after: //a
    :end-before: //z
    :language: c++
    :dedent: 8
