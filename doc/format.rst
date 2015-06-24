Appendix: Storage formats
============================


Parity
---------

MapDB uses parity bits to check storage pointers for corruption.
Offsets are usually aligned to multiples of 8, so lower bits are used for parity checks and other flags.

One important requirement is that zeroes is not valid parity value (0 produces non zero parity bits).

Parity 1
~~~~~~~~~~~~
Used for values where last bit is unused, lowest bit stores parity information. It is calculated as number of bits set,
lowest bit is than set so total number of non-zero bits is odd:

.. code:: java

    val | ((Long.bitCount(val)+1)%2)


Methods: ``DataIO.parity1set()`` and ``DataIO.parity1get()``

TODO document other parity methods, for now see DataIO class

Feature bitmap header
----------------------
Feature bitmap is 64bits stored in header at offset 8 (after file header and version).
It indicates features storage was created with.
Some of those affect storage format (compression, checksums) and must be enabled to make store readable. 
Some slots are not yet used and are reserved for future features. If such unknown bit is set, 
MapDB might refuse to open storage, with exception that never version should be used

Currently used feature bits are:

1) LZW record compresson enabled

2) XTEA record encryption enabled. User must supply password to open database.

3) CRC32 record checksum enabled

4) Store does not track free space. There might be unclaimed free space between records, this makes free space metrics invalid.

5) Sharded engine. It means that name catalog or class catalog might not be present

6) Created from backup. Storage was not created empty, but from existing database, as backup or from data pump

7) External index table. Index table is not stored in this Volume (file), but somewhere outside, most likely in external file

8) Compaction disabled. Some features might prevent compaction, for example StoreAppend in single file. 

9) Paranoid. Store was created by patched MapDB it added extra information to catch bugs and data corrupton. 
This store can not be opened with normal mapdb.
  
10) Disable parity bit checks. Stor was created by patched MapDB. For extra performance some checksums were disabled.
This store can not be opened with normal mapdb.

11) Block encryption enabled

TODO declare range which is backward compatible in read-only mode.

StoreDirect
------------------

StoreDirect uses update in place. It keeps track of free space released by record deletion and reuses it.
It has zero protection from crash, all updates are written directly into store.
Write operations are very fast, but data corruption is almost guaranteed when JVM crashes.
StoreDirect uses checksums as passive protection not to return incorrect data after corruption.
Internal data corruption should be detected reasonably fast.

StoreDirect allocates space in 'pages' of size 1MB. Operations such as ``readLong``, ``readByte[]``
must be aligned so they do not cross page boundaries.

Head
~~~~~~~
Header in StoreDirect format is composed by number of 8 byte longs:

0) **header** and **head checksum**. Checksum is CRC of entire HEAD and is recalculated on
every sync/close. Invalid checksum means that store was not closed correctly,
is very likely corrupted and MapDB should fail to open it. See ``StoreDirect.headChecksum()``

1) bit field indicating **format features**. IE what type of checksums are enabled, compression enabled etc...

2) **store size** pointer to last allocated page inside store. Parity 16.

3) **max recid** maximal allocated recid. Shifted <<<3 for parity 3

4) **index page registry** points to page with list of index pages. Parity 16.

5) **free recids** longs stack

TODO free longstacks

TODO rest of zero page is filled by recids



Index page
~~~~~~~~~~~~~~~~~~~~~~~~
Linked list of pages. It starts at **index page registry**.

Index page contains at start:

- first value is **pointer to next index page**, Parity 16
- second value in page is **checksum of all values** on page (add all values)
        TODO checksum is different

Rest of the index page is filled with index values.


Index Value
~~~~~~~~~~~~~
Index value translates Record ID (recid) into offset in file and record size. Position and size of record might
change as data are updated, that makes index tables necessary. Index Value is 8 byte long with parity 1

- **bite 49-64** - 16 bite record size. Use ``val>>48`` to get it

- **bite 5-48** - 48 bite offset, records are aligned to 16 bytes, so last four bites can be used for something else.
  Use ``val&MOFFSET`` to get it

- **bite 4** - linked or null, indicates if record is linked (see section TODO link to section). Also ``linked && size==0`` indicates null record. Use ``val&MLINKED``.

- **bite 3** - indicates unused (preallocated or deleted) record. This record is destroyed by compaction. Use ``val&MUNUSED``

- **bite 2** - archive flag. Set by every modification, cleared by incremental backup. Use ``val&MARCHIVE``

- **bite 1** - parity bit

Linked records
~~~~~~~~~~~~~~~~~
Maximal record size is 64KB (16bits). To store larger records StoreDirect links multiple records into single one.
Linked records starts with Index Value where Linked Record is indicates by a bit. If this bit is not set, entire record
is reserved for record data. If Linked bit is set, than first 8 bytes store Record Link with offset and size of the next part.

Structure of Record Link is similar to Index Value. Except parity is 3.

- **bite 49-64** - 16 bite record size of next link. Use ``val>>48`` to get it

- **bite 5-48** - 48 bite offset of next record alligned to 16 bytes. Use ``val&MOFFSET`` to get it

- **bite 4** - true if next record is linked, false if next record is last and not linked (is tail of linked record).
    Use ``val&MLINKED``

- **bite 1-3** - parity bits

Tail of linked record (last part) does not have 8-byte Record Link at beginning.


Long Stack
~~~~~~~~~~~~
Long Stack is linked queue of longs stored as part of storage. It supports two operations: put and take, longs are
returned in FIFO order. StoreDirect uses this structure to keep track of free space. Space allocation involves
taking long from stack.
There are more stacks, each size has its own stack, there is also stack to keep track of free recids.
For space usage there are in total ``64K / 16 = 4096`` Long Stacks
(maximal non-linked record size is 64K and records are aligned to 16 bytes).

Long stack is organized similar way as linked record. It is stored in chunks, each chunks contains multiple long
values and link to next chunk. Chunks size varies. Long values are stored in bidirectional-packed form, to make
unpacking possible in both directions.  Single value occupies from 2 bytes to 9 bytes.
TODO explain bidi-packing, for now see DataIO class.

Each Long Stack is identified by master pointer, which points to its last chunk. Master Pointer for each Long Stack
is stored in head of storage file at its reserved offset (zero page). Head chunk is not linked directly, one has to fully
traverse Long Stack to get to head.

Structure of Long Stack Chunk is as follow:

- **byte 1-2** total size of this chunk.
- **byte 3-8** pointer to previous chunk in this long stack. Parity 4, parity is shared with total size at byte 1-2.
- rest of chunk is filled with bidi-packed longs with parity 1

Master Link structure:

 - **byte 1-2** tail pointer, points where long values are ending at current chunk. Its value changes on every take/put.
 - **byte 3-8** chunk offset, parity 4.

Adding value to Long Stack goes as follow:

1) check if there is space in current chunk, if not allocate new one and update master pointer
2) write packed value at end of current chunk
3) update tail pointer in Master Link

Taking value:

1) check if stack is not empty, return zero if true
2) read value from tail and zero out its bits
3) update tail pointer in Master Link
4) if tail pointer is 0 (empty), delete current chunk and update master pointer to previous page


Write Ahead Log
-------------------------

WAL protects storage from data corruption if transactions are enabled.
Technically it is sequence of instructions written to append-only file. Each
instruction says something like: 'write this data at this offset'. TODO explain WAL.

WAL is stored in sequence of files.

WAL lifecycle
~~~~~~~~~~~~~~~~~
- open (or create) WAL
- replay if unwritten data exists (described in separate section)
- start new file
- write instructions as they come
- on commit start new file
- sync old file. Once sync is done, exit commit (it is blocking operation, until data are safe)
- once log is full, replay all files
- discard logs and start over

WAL file format
~~~~~~~~~~~~~~~~~~~
- **byte 1-4** header and file number
- **byte 5-8** CRC32 checksum of entire log file.  TODO perhaps Adler32?
- **byte 9-16** Log Seal, written as last data just before sync.
- rest of file are instructions
- **end of file** - End Of File instruction

WAL Instructions
~~~~~~~~~~~~~~~~~~
Each instruction starts with single byte header. First 3 bits indicate type of instruction. Last 5 bits contain
checksum to verify instruction.

Type of instructions:

0) **end of file**. Last instruction of file. Checksum is ``bit parity from offset & 31``

1) **write long**. Is followed by 8 bytes value and 6 byte offset. Checksum is ``(bit parity from 15 bytes + 1)&31``

2) **write byte[]**. Is followed by 2 bytes size, 6 byte offset and data itself.
    Checksum is ``(bit parity from 9 bytes + 1 + sum(byte[]))&31``

3) **skip N bytes**. Is followed by 3 bytes value, number of bytes to skip .
    Used so data do not overlap page size. Checksum is ``(bit parity from 3 bytes + 1)&31``

4) **skip single byte**. Skip single byte in WAL. Checksum is ``bit parity from offset & 31``

5) **record**. Is followed by 6 bytes recid, than 4 bytes record size and an record data.
    Is used in Record format. Size==-2 is tombstone, size==-1 is null record
    TODO checksum for record inst

6) TODO write two bytes.

Append Only Store
--------------------
StoreAppend implements Append-Only log files storage. It is sequence of instructions such as 'update record', 'delete record'
and so on. Optionally store can be split between multiple files, to support online compaction.

Instructions
~~~~~~~~~~~~~

Recid and size has parity. If CRC32 is enabled parity is 16 bites, otherwise 1 bite parity.

Instruction byte has first 5 bites for instruction and 3 bites for checksum.
It is calculated from offset, file number and instruction number.
IF CRC32 is enabled next byte replicates byte, shifted by some value (+101).

1) record update. Followed by packed recid with parity, packed size with parity and binary data

2) delete record. Places tombstone in index table. Followed by packed recid with parity.

3) record insert. Followed by packed recid with parity, packed size with parity and  binary data

4) preallocate record. Followed by packed recid with parity

5) skip N bytes. Followed by packed size with parity.

6) skip single byte

7) EOF current file. Move to next file

8) Current transaction is valid. Start new transaction

9) Current transaction is invalid. Rollback all changes since end of previous transaction. Start new transaction
