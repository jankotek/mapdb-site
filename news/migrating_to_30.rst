Migrating to MapDB 3.0
-------------------------

.. post:: 2016-04-14
   :tags: mapdb2, mapdb1
   :author: Jan


MapDB 3 is now usable, so lets talk about differences from older releases and howto migrate into new release.

MapDB now depends on external libraries, most notably Guava and Eclipse Collections. I will not cover that
here, so please just use dependencies provided by Maven.
Latter I will use more flexible version ranges and optional dependencies to make dependencies simpler.
There will also be small self-contained flavour with all dependencies packed into single jar file and
compressed by Proguard.

There is big change in default configuration. Transactions are now disabled by default,
``DBMaker.memoryDB().make()`` creates ``DB`` with transactions disabled.
There is new option ``transactionEnable()`` to enable transactions.
This settings has changed because some new storage engines do not support transactions.

``BTreeKeySerializer`` was merged into ``Serializer`` class. So for ``BTreeMap`` key serializer
use ``Serializer.STRING`` or similar static field. Serializers with delta packing have ``_DELTA`` suffix.

Data Pump for BTreeMap has changed. Now it takes data sorted in naturally order (older version required reverse sorted source).
Also it does no longer require source iterator, but can take data in form of ``Sink`` (consumer with ``put`` and ``finish`` method).
Use ``DB.treeMap("map").createFrom...()`` method to create populated ``BTreeMap``.

Features missing in milestone 5
------------------------------------

Instance cache is not implemented yet. It will be added in latter release.

Asynchronous writes are missing and will be added latter.

TXMaker and concurrent transactions are missing. Will be added latter.

StoreAppend and StoreAppend is missing, will be added latter.

Bind utilities are missing, it will probably be added latter.