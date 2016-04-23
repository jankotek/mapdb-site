Changelog
============================


Milestone 6 released (2016-04-23)
--------------------------------------------

.. post:: 2016-04-23
   :tags: release
   :author: Jan

Maven version number: ``3.0.0-M5``

- Fixed BTreeMap race condition `#664 <https://github.com/jankotek/mapdb/issues/664>`_.
- Improved POJO serialization (Elsa) dependency.
- MapDB now should handle and use class catalog.
- Added several tests to test concurrency
- Memory mapped file preclear no longer sync file. Faster file growth.

**Storage format change:**
Maximal record ID increased. Older storage format should be still readable.



Milestone 5 released (2016-04-14)
--------------------------------------------

.. post:: 2016-04-14
   :tags: release
   :author: Jan

Maven version number: ``3.0.0-M5``

There are several new options ported from 2.0 branch. Transactions are disabled by default,
use `DBMaker.transactionEnable()` to enable it.

There is `a new blog post </news/migrating_to_30/>`_ for 2.0 users who would like to use new 3.0 branch


3.0.0 Milestone 4 released (2016-03-28)
--------------------------------------------

.. post:: 2016-03-28
   :tags: release
   :author: Jan

Maven version number: ``3.0.0-M4``

M4 fixes Store reopen `issue <https://github.com/jankotek/mapdb/issues/680>`_.


3.0.0 Milestone 3 released (2016-03-10)
--------------------------------------------

.. post:: 2016-03-10
   :tags: release
   :author: Jan

Maven version number: ``3.0.0-M3``

Next release towards stable 3.0. Some features are not yet implemented, most notably memory mapped files and transactions.

3.0.0 Milestone 2 released (2016-02-12)
--------------------------------------------

.. post:: 2016-02-12
   :tags: release
   :author: Jan

Maven version number: ``3.0.0-M3``

This version adds BTreeMap



MapDB 1 and 2
----------------

Older changelog are in `archive <../changelog-archive>`_