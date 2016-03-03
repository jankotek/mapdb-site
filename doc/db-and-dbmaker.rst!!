DB and DBMaker
==============

MapDB is a set of coupled components.
To hold things together there are two classes: ``DBMaker`` and ``DB``.
They use maker (builder) pattern, so most configuration options are quickly
available via code assistant in IDE.

`DBMaker <http://www.mapdb.org/apidocs/org/mapdb/DBMaker.html>`__ handles database
configuration, creation and opening. MapDB has several modes and
configuration options. Most of those can be set using this class.

`DB <http://www.mapdb.org/apidocs/org/mapdb/DB.html>`__
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

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_basic_option.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Once you have DB you may open a collection or other record. DB has two
types of factory methods:

``xxx(String)`` takes the name and opens the existing collection (or record),
such as ``treeMap("customers")``. 
If a collection with the given name does not exist,
it is silently created with default settings and returned. 
An example:

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_treeset.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

``xxxCreate(String)`` takes the name and creates a new collection with customized
settings. Specialized serializers, node size, entry compression and other options
are customizable here.

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_atomicvar.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Most ``create`` methods use builder style configuration. In that case
you may finish with two methods: ``make()`` creates a new collection. If a
collection with the given name already exists, ``make()`` throws an exception.
Method ``makerOrGet()`` is the same, except if a collection already exists,
it does not fail, but returns the existing collection.

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_treeset_create.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Transactions
------------

``DB`` has methods to handle a transaction lifecycle: ``commit()``,
``rollback()`` and ``close()``.

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_basic_tx.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

One ``DB`` object represents single transaction. The example above uses
single global transaction per store, which is sufficient for some usages.

Concurrent transactions are supported as well, with full serializable
isolation, optimistic locking and MVCC snapshots. For concurrent transactions we need
one extra factory to create transactions: ``TxMaker``.
We use ``DBMaker`` to create it, but instead of ``make()`` we call
``makeTxMaker()``

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_txmaker_create.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

A single ``TxMaker`` represents an opened store. ``TxMaker`` is used to create multiple
``DB`` objects, each representing a single transaction. In that case ``DB.close()``
closes one transaction, but  storage remains open by ``TxMaker``:

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_txmaker_basic.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

