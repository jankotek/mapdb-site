DB and DBMaker
==============

MapDB is set of loosely coupled components. One could wire classes such
as ``CacheMRU``, ``StoreWAL`` and ``BTreeMap`` manually, byt there are
two factory classes to do it for you: ``DBMaker`` and ``DB``. They use
maker (builder) pattern, so most configuration options are quickly
available via code assistant in IDE.

`DBMaker <apidocs/org/mapdb/DBMaker.html>`__ handles database
configuration, creation and opening. MapDB has several modes and
configuration options. Most of those can be set using this class.

`DB <apidocs/org/mapdb/DB.html>`__ represents opened database (or single
transaction session). It creates and opens collections . It also handles
transaction with methods such as ``commit()``, ``rollback()`` and
``close()``.

To open (or create) store use one of ``DBMaker.newXXX()`` static
methods. MapDB has more formats and modes, each ``newXXX()`` uses
different: ``newMemoryDB()`` opens in-memory database backed by
``byte[]``, ``newAppendFileDB()`` opens db which uses append-only log
files and so on.

``newXXX()`` method is followed by configuration options and ``make()``
method which applyes all options and returns ``DB`` object. This example
opens file storage with encryption enabled:

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_basic_option.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Once you have DB you may open collection or other record. DB has two
types of factory methods:

``getXXX()`` opens existing collection (or record). If collection with
given name does not exist, it is silently created with default settings
and returned. An example:

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_treeset.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

``createXXX()`` creates new collection (or settings) with customized
settings. Specialized serializers, node size, entry compression and so
on affect performance a lot and they are customizable here.

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_atomicvar.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8

Some ``create`` method may use builder style configuration. In that case
you may finish with two methods: ``make()`` creates new collection, if
collection with given name already exists it throws an exception.
``makerOrGet()`` is same, except if collection already exist it does not
fail, but returns existing collection.

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_treeset_create.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


Transactions
------------


``DB`` has methods to handle transaction lifecycle: ``commit()``,
``rollback()`` and ``close()``.

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_basic_tx.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


One ``DB`` object represents single transactions. Examples above use
single global transaction, which is sufficient for some usages. MapDB
support concurrent transactions as well with full serializable
isolation, optimistic locking and MVCC snapshots. In that case we need
one extra factory which creates transactions: ``TxMaker``. We use
``DBMaker`` to create it, but instead of ``make()`` we call
``makeTxMaker()``

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_txmaker_create.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8


And ``TxMaker`` is than used to create multiple ``DB`` objects, each
representing single transaction:

.. literalinclude:: ../../mapdb/src/test/java/doc/dbmaker_txmaker_basic.java
    :start-after: //a
    :end-before: //z
    :language: java
    :dedent: 8
