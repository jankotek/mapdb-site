Once you have DB you may open a collection or other record. DB has two types of factory methods:

`xxx(String)` takes the name and opens the existing collection (or record), such as `treeMap("customers")`. If a collection with the given name does not exist, it is silently created with default settings and returned. An example:

`xxxCreate(String)` takes the name and creates a new collection with customized settings. Specialized serializers, node size, entry compression and other options are customizable here.

Most `create` methods use builder style configuration. In that case you may finish with two methods: `make()` creates a new collection. If a collection with the given name already exists, `make()` throws an exception. Method `makerOrGet()` is the same, except if a collection already exists, it does not fail, but returns the existing collection.

Transactions
============

`DB` has methods to handle a transaction lifecycle: `commit()`, `rollback()` and `close()`.

One `DB` object represents single transaction. The example above uses single global transaction per store, which is sufficient for some usages.

Concurrent transactions are supported as well, with full serializable isolation, optimistic locking and MVCC snapshots. For concurrent transactions we need one extra factory to create transactions: `TxMaker`. We use `DBMaker` to create it, but instead of `make()` we call `makeTxMaker()`

A single `TxMaker` represents an opened store. `TxMaker` is used to create multiple `DB` objects, each representing a single transaction. In that case `DB.close()` closes one transaction, but storage remains open by `TxMaker`:
