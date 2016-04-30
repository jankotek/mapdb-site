MapDB enters beta, storage format and API freeze
======================================================

.. post:: 2016-04-30
   :tags: release
   :author: Jan

After almost 6 months of development, 3.0 branch enters beta stage.
API and storage are now frozen and no breaking change should happend,
unless its required for bugfix. Stable version will follow in about
a month, once remaining bugs are squashed and documentation is in
reasonable shape.

Why new branch
----------------------------
I already described `my reasons for dropping 2.0 branch </news/mapdb3/>`_.
I did that to speedup development and become more productive.
Old version had too limited feature set.
And finally I really miss old days with JDBM3, when I would introduce
new features every day, without adding new bugs.

MapDB 3.0 is now mostly written in Kotlin; very expressive language with
excellent Java compatibility and low overhead. MapDB now also depends on other libraries.

What is new in 3.0
------------------
New MapDB 3.0 release does not have all features from 1.0 and 2.0.
Most notably it is missing asynchronous writes, instance cache
and concurrent transactions (TxMaker). In many benchmarks it
is slower than older versions.
Missing features will be reintroduced in 3.1 release.

Also some default settings
had changed (transactions are disabled by default). There is a short
document if you are `migrating from older branch </news/migrating_to_30/>`_.

Outside internal changes, there are some new features:

Binary acceleration
~~~~~~~~~~~~~~~~~~~~
Older versions required records (such as tree node) to be fully deserialized
in order to perform any operation.
Now some operations (most notably binary search) are done on raw binary data without deserialization.
It shows up to 3x better performance for ``BTreeMap.get()`` operation.
Currently its fully implemented only in ``Serializer.INTEGER``, other datatypes will follow soon

Data pump improvements
~~~~~~~~~~~~~~~~~~~~~~~~
BTreeMap Data Pump now takes data in naturaly sorted order (no reverse sort is needed).
Also it doesn not require source to be an iterator, but takes data from Sink.

BTreeMap
~~~~~~~~~~~~~
BTreeMap descending iterator is now faster. It keeps stack of BTree Directory nodes
and jumps backward without performing new tree search for each Leaf Node.

SortedTableMap
~~~~~~~~~~~~~~~~~
Is SortedMap inspired by Sorted String Tables from Cassandra. It is about 5 faster compared
to BTreeMap. Current version is read-only, writeable version will be added in near future.

Index Tree List
~~~~~~~~~~~~~~~~~~
HTreeMap uses sparse array like data structure backed by very efficient tree.
This code was extracted from HTreeMap into separate class and now provides IndexTreeList, an
``java.util.List`` collection which provides sparse indexed List.


Near future
-------------

There will be a few betas before stable release. I expect new beta release every week after major bugfix.

MapDB 3.0 will graduate into stable version in about 4 weeks.
Biggest obstacle are not bugs, current version has many unit tests and is quite stable, but documentation.
And MapDB now has secondary projects (mapdb-jcache, Elsa serialization) and those
need bit more polishing before MapDB becomes stable.

After stable 3.0 there will be new stable release every month. I have many plans.
