Secondary Collections
=====================

Rational databases have a very good system of primary and secondary
indexes, tables and views. It has clear benefits for extensibility,
clarity and robustness. On the other hand, it has limitations for scalability
and performance. MapDBs Secondary Collections are *poor man\`s* SQL
tables. It brings the most benefits without sacrificing flexibility.

Secondary Collections are a very simple and flexible way to access and
expand primary collections. Primary collection is an authoritative source
of data, and is modified by the user. Secondary collection contains records
derived from the primary. Secondary is bind to primary and updated by a
listener, when the primary collection is modified. The secondary should not be
modified directly by a user.

Secondary Collections are typically used in three ways:

-  Secondary Keys (indexes) to efficiently access records in primary
   collection.

-  Secondary Values to expand primary collection, while keeping primary
   small and fast.

-  Aggregation to groups. For example in order to list all high-income customers.

The Primary Collection is typically stored in MapDB. The requirement is that
it provides `modification
listener <http://www.mapdb.org/apidocs/org/mapdb/Bind.MapWithModificationListener.html>`__
triggered on entry update, insert or removal. There is no such
requirement for the Secondary Collection. The Secondary may be stored in MapDB,
but it can also be a usual Java Collection such as java.util.TreeSet.

Primary and Secondary collections are bind together. There is
`Bind <http://www.mapdb.org/apidocs/org/mapdb/Bind.html>`__ class with
static methods to establish binding. Binding adds a modification listener
to the primary collection and changes secondary collection accordingly. It
also removes old entries from the secondary, if the primary entry gets deleted or
modified.

Bind relation is not persistent, so binding needs to be restored every
time the store is reopened. If the secondary collections is empty when binded,
then the entire primary is traversed and the secondary is filled accordingly.

Consistency
-----------

The consistency between primary and secondary collections are on a
'best-effort' basis. The secondary map might contain an old value,
while the primary map was already updated. Also if two maps are part of two different transactions,
and one transaction get rolled back but second transaction is commited,
the secondary map will become inconsistent with the primary
(its changes were not rolled back.

The secondary collection is updated in `serializable
fashion <https://en.wikipedia.org/wiki/Serializability>`__. This means
that if two concurrent threads update primary, the secondary collection is
updated with 'winner'. TODO verify this is true, update this paragraph
after `issue <https://github.com/jankotek/MapDB/issues/226>`__ is
closed.

There are some best practices for Secondary Collections to handle this:

-  Secondary Collections must be thread safe. Either use MapDB or
   ``java.util.concurrent.*`` collections. Another
   option is to use ``Collections.synchronized*()`` wrappers.

-  When using concurrent transactions, do not mix the collections from
   multiple transactions. If the primary gets rollback, the secondary will not
   be updated if its not within the same transaction.

-  Keep binding minimal. It should only transform one value into the other,
   without dependency on third collections.

Performance
-----------

To import a large dataset, you should not enable binding until the primary
collection has finished its import. Also, there might be a more efficient
way to pre-fill the secondary collection (for example with a data pump).
