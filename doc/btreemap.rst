BTreeMap
==========

``BTreeMap`` provides ``TreeMap`` and ``TreeSet`` for MapDB. It is based on lock-free concurrent B-Linked-Tree.
It offers great performance for small keys and has good vertical scalability.

TODO explain compressions

TODO describe B-Linked-Tree

Performance
----------------

BTrees in general are affected by node size and number of directory levels. Larger node means shallow tree,
and faster lookups, but slower updates. In MapDB you should adjust node size with average key size



Fragmention
-------------
Trade-off for lock-free design is fragmentation after deletion. B-Linked-Tree does not delete btree nodes after
entry removal, once they become empty. If you fill BTreeMap and than remove all entries, about 40% of space will not
be released. Value updates (keys are kept) is not affected by this fragmentation.

This fragmentation is at different than storage fragmentation, so ``DB.compact()`` will not help.
Solution is to move all content into new ``BTreeMap``. Its very fast with Data Pump streaming,
new Map will have zero fragmentation and better node locality (in theory disk cache friendly).

TODO provide utils to move BTreeMap content
TODO provide statistics to calculate BTreeMap fragmentation

In future we will provide BTreeMap wrapper which will do this compaction automatically. It will use three collections:
First ``BTreeMap`` will be read-only and contain data. Second small map will contain updates. Periodically third map
will be produced as merge of first two, and swapped with primary.
``SSTable``'s in Cassandra and other databases works similar way.

TODO provide wrapper to compact/merge BTreeMap content automatically.


Compared to HTreeMap
---------------------

TODO create separate page about collections in MapDB



