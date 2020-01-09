---
title: "Changelog"
excerpt: "Changelog"
layout: "single"
permalink: "/changelog/"
---

3.0.8 released
--------------
Maven version number: `3.0.8`, release date 2020-01-08

- [Fix #965](https://github.com/jankotek/mapdb/issues/965) Memory leak in `StoreWAL`

- [Fix #964](https://github.com/jankotek/mapdb/issues/964) EC added new method into interface, MapDB would not compile

- [Fix #939](https://github.com/jankotek/mapdb/issues/939) Compile with JDK 10 and JDK 11, better support for cleaner hack

- [Fix #898](https://github.com/jankotek/mapdb/issues/898) Add a classloader parameter to SerializerJava

- Add OpenJDK 10 and OpenJDK 11 into Travis  


3.0.6 released
--------------

Maven version number: `3.0.6`, release date 2018-05-16
    
- Update Kotlin to 1.2.x

- Update Eclipse Collections to latest version

- Use wildcards on Maven dependencies versions (Guava, Eclipse Collections) to allow more flexible dependencies

- [Fix #841](https://github.com/jankotek/mapdb/issues/841), `HashSet.add()` returns wrong value

3.0.5 released
--------------

Maven version number: `3.0.5`, release date 2017-07-08
    
- [Fix #836](https://github.com/jankotek/mapdb/issues/836), HTreeMap.keySet performs linear scan

- [Fix #778](https://github.com/jankotek/mapdb/issues/778), BTreeMap counter wrong after `BTreeMap.clear()` called several times

- Replace SingleEntry locks with Reentrant Locks. 5x performance improvement in most cases

3.0.4 released
--------------

Maven version number: `3.0.4`, release date 2017-04-24

- [Fix #819](https://github.com/jankotek/mapdb/issues/819), HTreeMap Concurrency Layout was persited wrong way


- [Fix #816](https://github.com/jankotek/mapdb/issues/816), BTreeMap and HTreeMap.KeySet.add has incorrect return value 

- Update Kotlin compiler and library to 1.0.7, fix relevant compilation errors

- [Fix #815](https://github.com/jankotek/mapdb/issues/815), SortedTableMap Page Size had wrong check

- [Fix #781](https://github.com/jankotek/mapdb/issues/781), fix deadlock in `Utils.lock*All()`

- [Fix #780](https://github.com/jankotek/mapdb/issues/780), 
    `DBMaker.make()` blocks without throwing `FileLockedException` when fileLockWait timeout specified. Use `FileChannel#tryLock()` when locking a store.

- [Fix #800](https://github.com/jankotek/mapdb/issues/800), NPE in `SortedableMap` subsets

- [Fix #776](https://github.com/jankotek/mapdb/issues/776), `StackOverflowError` on unmapping mmap files


3.0.3 released
--------------

Maven version number: `3.0.3`, release date 2017-01-12

- [Fix #794](https://github.com/jankotek/mapdb/issues/794), `TreeMap.createFromSink` with external values causes `DBException$PointerChecksumBroken: Broken bit parity`
- Upgrade Kotlin compiler and library to 1.0.6

3.0.2 released
--------------

Maven version number: `3.0.2`, release date 2016-09-23

Fixed two critical data corruption bugs. No changes in API or storage format. 

Changes:

- [Fix #765](https://github.com/jankotek/mapdb/issues/765), Data corruption from long stack
- [Fix #760](https://github.com/jankotek/mapdb/issues/760), `StoreDirect#compact()` and `StoreDirect#put()` had race condition which caused data corruption


3.0.1 released
--------------

Maven version number: `3.0.1`, release date 2016-07-17

Fixed two critical data corruption bugs. Update highly recommended. No changes in API or storage format. 

Changes:

- Fix #743, BTreeMap returns wrong values after clear and reinsert.
- Fix #746, StoreWAL: delete() could corrupt data store. 


3.0.0 released
--------------

Maven version number: `3.0.0`, release date 2016-06-29

Small bugfix, no changes in API or storage format

Changes:

-   Fixed \#733: `SerializerArray<T>.deserialize()` return `Object[]` instead of `T[]`. Added new constructor parameter to control array type after deserialization.
-   Small changes in javadoc

3.0 Release Candidate 2 released
--------------------------------

Maven version number: `3.0.0-RC2`, release date 2016-06-19

Two bugfixes. No changes in format or API

Changes:

-   Fixed \#726: DBMaker.volumeDB broken
-   HTreeMap expiration could throw `DBException.GetVoid`. Fixed by using lazy iterators in `IndexTreeLongLongMap`

3.0 Release Candidate 1 released
--------------------------------

Maven version number: `3.0.0-RC1`, release date 2016-06-06

Number of small improvements. At this point MapDB has no obvious bugs. If no bugs are found this version will become stable release.

No changes in format or API.

Changes:

-   added `Store.fileLoad()` method. Content of memory mapped file can be loaded into memory by `db.getStore().fileLoad()`
-   added `DBMaker.allocationIncrement()` to minimize number of memory mapped chunks. This is workaround for *too many file handles*, see [\#723](https://github.com/jankotek/mapdb/issues/723)
-   Added documentation
-   Rework close methods to be thread safe, do not throw exception on second close.
-   Make `Store.commit()` and `Store.rollback()` thread safe.
-   BTreeMap: external values were not deleted after removal
-   Store: add `getAllFiles()` method.
-   Update Elsa serialization library

3.0 beta5 released
------------------

Maven version number: `3.0.0-beta5`, release date 2016-05-24

This release is fixing data corruption caused by `StoreWAL` with transactions enabled. Update is strongly recommended.

No changes in format or API.

3.0 beta4 released
------------------

Maven version number: `3.0.0-beta4`, release date 2016-05-19

**FORMAT CHANGE** POJO serializer format has changed. If you use default serializer together with your own objects, your data might not be readable. Well known java classes in `java.lang` such as `String`, `Long`, `BigDecimal` etc are not affected.

Changes:

-   reworked Elsa serialization (default serializer) to fix Class Catalog issues.
-   Elsa serializer now uses `IdentityHashMap` for recursive references. Much faster serialization for large object graphs
-   Rework JVM shutdown hook and `DB.close()`, should fix \#706.
-   JVM Shutdown hook now uses single thread for all DBs (memory efficient for many DBs with shutdown hook registered).
-   JVM Shutdown hook now uses hard reference instead of `WeakReference`. There is new option in DBMaker to use weak ref.
-   Fix `BTreeMap.put` caused `ArrayIndexOutOfBoundsException`. Fix \#707
-   `BTreeMap.prefixSubmap` now works with more primitive arrays (`int[]`, `long[]` etc..). Credit Dmitriy Shabanov
-   fix various small issues to make acceptance tests pass
-   Kotlin updated to 1.0.2
-   HTreeMap has new options to clear map content with expiration triggers. See \#708.

3.0 beta3 released
------------------

Maven version number: `3.0.0-beta3`, release date 2016-05-08

Fixed performance in HTreeMap, user serializers and file locking.

No changes in storage format or API.

Changes:

-   DB: throw IllegalAccessError on access after DB was closed
-   DB: make serializers optional in name catalog
-   `DB.fileLockDisable()` would not work. Fixed
-   Added option `DB.fileLockWait()` to block until file lock is released by second JVM \#693. There is optional timeout.
-   HTreeMap and IndexTreeList default sizes were to small, list would throw assertion error after 64K records, HTreeMap was slow due to huge number of collisions. Default size changed from 64K to 1E9

3.0 beta2 released
------------------

Maven version number: `3.0.0-beta2`, release date 2016-05-01

This release is fixing **data corruption issue**. Compaction on `StoreDirect` will corrupt storage if there is record smaller then 6 bytes (affects `HTreeMap` and `BTreeMap`).

Second bugfix is on `BTreeMap`, it would not call modification listeners.

No changes in storage format or API.

3.0 beta1 released
------------------

Maven version number: `3.0.0-beta1`, release date 2016-04-28

First semi-stable release. Storage format and API (`DB`, `DBMaker`) should be stable from now on.

**Storage format change:** Format has changed, files generated by M6 and older should not be opened with this version.

Milestone 6 released (2016-04-23)
---------------------------------

Maven version number: `3.0.0-M6`

-   Fixed BTreeMap race condition [\#664](https://github.com/jankotek/mapdb/issues/664).
-   Improved POJO serialization (Elsa) dependency.
-   MapDB now should handle and use class catalog.
-   Added several tests to test concurrency
-   Memory mapped file preclear no longer sync file. Faster file growth.

**Storage format change:** Maximal record ID increased. Older storage format should be still readable.

Milestone 5 released (2016-04-14)
---------------------------------

Maven version number: `3.0.0-M5`

There are several new options ported from 2.0 branch. Transactions are disabled by default, use DBMaker.transactionEnable() to enable it.

There is [a new blog post](/news/migrating_to_30/) for 2.0 users who would like to use new 3.0 branch

3.0.0 Milestone 4 released (2016-03-28)
---------------------------------------

Maven version number: `3.0.0-M4`

M4 fixes Store reopen [issue](https://github.com/jankotek/mapdb/issues/680).

3.0.0 Milestone 3 released (2016-03-10)
---------------------------------------

Maven version number: `3.0.0-M3`

Next release towards stable 3.0. Some features are not yet implemented, most notably memory mapped files and transactions.

3.0.0 Milestone 2 released (2016-02-12)
---------------------------------------

Maven version number: `3.0.0-M3`

This version adds BTreeMap

MapDB 1 and 2
-------------

Older changelog is in [archive](../changelog-archive)
