Transactions
============

Transactions in MapDB are considered a heavyweight option. Often you can
get a similar result using atomic updates in
`ConcurrentMap <http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentMap.html>`__
and `Atomic
variables <http://www.mapdb.org/apidocs/org/mapdb/Atomic.html>`__

On the other hand transactions are not just for concurrent updates, but they can also protect the store from corruption.
MapDB has two ways to handle rollback: Write-Ahead-Log and Append-Only-Log files.

MapDB comes with several transactions options. From ultra-fast direct
mode to concurrent transactions with MVCC snapshots and serializable
conflict resolution.

There are three transactional modes:

Transactions disables (aka direct mode)
---------------------------------------

In this mode transactions and all safety options are disabled to achieve
best write performance. Changes are written directly to underlying
files.

This mode has zero crash protection. If you do not close your database
correctly, your data are gone. You may also call ``db.commit()`` to
flush and sync all changes into underlying files.

This mode is generally recommended for cases where data can be
reconstructed easily. For example initial imports, caches etc.

An example how to enable this mode:

.. code:: java

    DB db = DBMaker.newMemoryDB()
        .transactionsDisable()
        .make();

Single global transaction
-------------------------

This mode is the default option. In this case DB has a single global
transaction. It is very simple to use, since the user does not have to handle
concurrent write conflicts and rollback. In this case the store is
protected by Write-Ahead-Log.

.. code:: java

    DB db = DBMaker.newMemoryDB()
        .make()

Concurrent transactions
-----------------------

In this case the user has multiple transactions. Each has its own snapshots
with serializable isolation and optimistic locking. It offers the strongest
consistency possible. Any changes are held in memory. On commit MapDB checks
for conflicts and commits or throws ``TxRollbackException`` and rollback
changes.

.. code:: java

        //Open Transaction Factory. DBMaker shares most options with single-transaction mode.
            TxMaker txMaker = DBMaker
                    .newMemoryDB()
                    .makeTxMaker();

            // Now open first transaction and get map from first transaction
            DB tx1 = txMaker.makeTx();

            //create map from first transactions and fill it with data
            Map map1 = tx1.getTreeMap("testMap");
            for(int i=0;i<1e4;i++){
                map1.put(i,"aaa"+i);
            }

            //commit first transaction
            tx1.commit();

            // !! IMPORTANT !!
            // !! DB transaction can be used only once,
            // !! it throws an 'already closed' exception after it was commited/rolledback
            // !! IMPORTANT !!
            //map1.put(1111,"dqdqwd"); // this will fail

            //open second transaction
            DB tx2 = txMaker.makeTx();
            Map map2 = tx2.getTreeMap("testMap");

            //open third transaction
            DB tx3 = txMaker.makeTx();
            Map map3 = tx3.getTreeMap("testMap");
    //TODO more from tx example

