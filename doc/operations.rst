Operations
============

This is chapter for DBA and DevOps. It describes howto run MapDB in long period of time,
while keeping data safe and performance good and consistent.

MapDB is just database engine, so basic library does not include tools usually bundled with full
database systems. But there are external libraries which will be also discussed here.
With some tuning MapDB is very capable of running in continuous operation.

Backups
~~~~~~~~

Incremental backups
-------------------

Backup under commit lock
------------------------

Backup with snapshots
-------------------------
 - data corruption in HTreeMap from incomplete updates
 - lock entire HTreeMap

Full export into text file
--------------------------


Monitoring
~~~~~~~~~~~

JMX
----

Compaction
~~~~~~~~~~~

- compaction in memory versus temporary files

Record fragmentation and average free record size
---------------------------------------------------


BTree compaction
--------------------


Append Only File background compaction
-----------------------------------------


