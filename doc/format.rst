Storage format
============================

This chapter is storage specification for MapDB files.

File operations
-------------------

File operations (such as file create, rename or sync) must be atomic and must survive system crash.
In case of crash there is recovery operation after restart. If file operation did not finished it reverts
everything into last stable state. That means file operations are atomic (they either succeed or fail without side effects).

To ensure crash resistance and atomicity MapDB relies on marker files.
Those are empty files created and deleted using atomic filesystem API.
Marker files have the same name as main file, but with ``.$X`` suffix.


File Create
~~~~~~~~~~~~~~~~

Empty file creation is atomic operation, but populating file with content is not.
MapDB needs file population to be atomic,  and uses  uses ``.$c`` marker file for that.


File creation sequence:

1) create marker file with ``File.createNewFile()``
2) create empty main file and lock it
3) fill main file with content, write checksums
4) sync main file
5) remove marker file

In case of recovery, or when file is being opened, follow this sequence:

1) open main file and lock it, fail if main file does not exist
2) TODO we should check for pending File Rename operations here
3) check if marker file exists, fail if it exists

In case of failure throw an data corruption exception.

Temporary file write open
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Temporary file in MapDB is write-able file without crash protection (usually by write-ahead-log).
Compared to *File Create* this file is opened continuously and only closed on system shutdown.
If file was not closed, it most likely becomes corrupted and MapDB will refuse to reopen in.


*File Create* sequence is also used for temporary file without crash protection.
In that case marker file stays while the main file is opened for write.
If there is an crash, recovery sequence will find marker file, assume that main file was not closed correctly and will refuse to open it.
In this case main file should be discarded and recreated from original data source.
Or user can remove marker file and try his luck.

File Rename
~~~~~~~~~~~~

File Rename is used in StoreDirect compaction. Store is recreated in new file, and old file is replaced with new content.
The 'old file' is file which is being replaced, it will be deleted before File Rename. The 'new file'
replaces old file and has its name changed.

MapDB needs file move to be atomic, and supported in range variety of platforms. There are following problems:

- ``java.nio.file.Files#move`` is atomic, but it might fail in some cases

- Opened memory mapped file on Windows can not be renamed. MappedByteBuffer handle is not released until GC or cleaner hack.
  Sometimes handle is not released even after JVM exit, and OS restart is required.

- There should be fallback option, when we can not close file Volume, but copy content between Volumes.


File rename has following sequence:

- synchronize and close new file, release its c marker

- create 'c' marker on old file

- create 'r' marker on new file

- delete old file

- use ``java.nio.file.Files#move`` in atomic or non-atomic way. But rename operation must be finished and synced to disk.

- delete r marker for new file

- delete c marker on old file

- open old file (with new content)

TODO this does not work on windows with memory mapped files. We need plan B with Volume copy, without closing them.

Recovery sequence is simple. If following files exist:

- c marker for old file

- r marker for new file

- new file (under its name before rename)

Than discard the old file if present and continue rename sequence from step 'delete old file'

Rolling file
~~~~~~~~~~~~~~~~~~~~

Rolling file is a single file, but continuously replaced with new content. To make content replacement atomic,
the content of file is written into new file, synced and then old file is deleted.
File name has '.N' suffix, where N is sequential number increased with each commit. Rolling file
is used in ``StoreTrivial``.

There is following sequence for updating rolling file with new content. Ther is 'old file' with original content
and  number N and 'new file' with number N+1.

- Create c marker for new file, fail if it already exists

- Populate new file with content, sync and close

- Remove C marker for new file

- Delete the old file

And there is following sequence for recovery

- List all files in parent directory, find file with highest number without C marker, lock and open it.

- Delete any other files and their markers (only files associated with the rolling file, there might be more files with different name)

File sync
~~~~~~~~~~~~~~~~~~~~~~~

On commit or close, write cache needs to be flushed to disk, in MapDB this is called sync.
We also need to detect corrupted files if system crashes in middle of write.

There are following ways to sync file:

- **'c' file marker** (see File Rename).

- **File checksum:** Before the file sync is called, checksum of entire file is calculated and written into file header.
  Corruption is detected by matching file checksum from header with file content.
  This is slow because entire file has to be read

- **Commit seal:** Uses double file sync, but does not require checksum calculation. First file is synced with zero checksum in file header. Than commit seal
  is written into file header, and file is synced again. Valid commit seal means that file was synced.
  TODO: commit seal is calculated based on file size

File header
--------------

Every non empty file created by MapDB has 16 byte header. It contains header, file version, bitfield for optional features
and optional checksum for entire file.

Bites:

- 0-7 constant value 0x4A

- 8-15 type of file generated. I

- 16-31 format version number. File will not be opened if format is too high

- 32-63 bitfield which identifies optional features used in this format. File will not be opened if unknown bit is set.

- 64-127 checksum of entire file.


File type
~~~~~~~~~~~~
can have following values:

- 0 unused
- 1 StoreDirect (also shared with StoreWAL)
- 2 WriteAheadLog for StoreWAL
- 10 SortedTableMap without multiple tables (readonly)
- 11 SortedTableMap with multiple tables
- 12 WriteAheadLog for SortedTableMap

Feature bitfield
~~~~~~~~~~~~~~~~~~~~~~
has following values. It is 8-byte long, number here is from least significant bit.

- 0 encryption enabled. Its upto user to provide encryption type and password

- 1-2 checksum used. 0=no checksum, 1=XXHash, 2=CRC32, 3=user hash.

- TODO more bitfields

Checksum
~~~~~~~~~~~
is either XXHash or CRC32. It is calculated as ``(checksum from 16th byte to end)+vol.getLong(0)``.
If checksum is ``0`` the ``1`` value is used instead. ``0`` indicates checksum is disabled.


StoreDirect
------------------

StoreDirect uses update in place. It keeps track of free space released by record deletion and reuses it.
It has zero protection from crash, all updates are written directly into store.
Write operations are very fast, but data corruption is almost guaranteed when JVM crashes.
StoreDirect uses parity bits as passive protection from returning incorrect data after corruption.
Internal data corruption should be detected reasonably fast.

StoreDirect allocates space in 'pages' of size 1MB. Operations such as ``readLong``, ``readByte[]``
must be aligned so they do not cross page boundaries.

Head
~~~~~~~
Header in StoreDirect format is composed by number of 8-byte longs. Each offset here is multiplied by 8

0) header and format version from file header *TODO chapter link*

1) file checksum from file header *TODO chapter link*

2) **header checksum** is updated every time header is modified, that can detect corruption quite fast

3) **data tail** points to end location where data were written to. Beyond this is empty (except index pages). Parity 4 with no shift (data offset is multiple of 16)

4) **max recid** maximal allocated recid. Parity 4 with shift.

5) **file tail** file size. Must be multiple of PAGE_SIZE (1MB). Parity 16

6) not yet used

7) not yet used

This is followed by Long Stack Master Pointers. Those are used to track free space, unused recids and other information.

- ``8`` - **Free recid** Long Stack, unused Recids are put here

- ``9`` - **Free records 16** - Long Stack with offsets of free records with size 16

- ``10`` -  **Free records 32** - Long Stack with offsets of free records with size 32 etc...

- ...snip 4095 minus 3 entries...

- ``8+4095`` - **Free records 65520** - Long Stack with offsets of free records with size 65520 bytes (maximal unlinked record size).
  4095 = 65520/16 is number of Free records Long Stacks.

- ``8+4095+1``  until ``8+4095+4`` - **Unused long stacks** - Those could be used latter for some other purpose.


Index page
~~~~~~~~~~~~~~~~~~~~~~~~

Rest of the zero page (up to offset 1024*1024) is used as Index Page (sometimes it's referred as Zero Index Page).
Offset to next Index Page (First Index Page) is at ``8+4095+4+1``, Zero Index Page checksum is at ``8+4095+4+2``.
First recid value is at ``8+4095+4+3``.

Index page starts at ``N*PAGE_SIZE``, except Zero Index Page which starts at ``8 * (8+4095 + 4 + 1)``.
Index page contains at start:

- zero value (offset ``page+0``) is **pointer to next index page**, Parity 16
- first value (offset ``page+8``) in page is **checksum of all values** on page (add all values)
        *TODO seed? and not implemented yet*

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

- **bite 5-48** - 48 bite offset of next record aligned to 16 bytes. Use ``val&MOFFSET`` to get it

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
1) **write long**. Is followed by 8 bytes value and 6 byte offset. Checksum is ``(bit count from 15 bytes + 1)&31``
2) **write byte[]**. Is followed by 2 bytes size, 6 byte offset and data itself.
   Checksum is ``(bit count from size + bit count from offset + 1 )&31``
3) **skip N bytes**. Is followed by 3 bytes value, number of bytes to skip .
   Used so data do not overlap page size. Checksum is ``(bit count from 3 bytes + 1)&31``
4) **skip single byte**. Skip single byte in WAL. Checksum is ``bit count from offset & 31``
5) **record**. Is followed by packed recid, than packed record size and an record data.
   Real size is +1, 0 indicates null record
   TODO checksum for record inst
6) **tombstone**. Is followed ba packed recid. . Checksum is ``bit count from offset & 31``
7) **preallocate**. Is followed ba packed recid. . Checksum is ``bit count from offset & 31``
8) **commit**. TODO checksum
9) **rollback**. TODO checksum


Sorted Table Map
---------------------

``SortedTableMap`` uses its own file format. File is split into page,
where page size is power of two and maximal page size 1MB.

Each page has header. Header size is bigger for zero page, because it also contains file header. TODO header size.

After header there is a series of 4-byte integers.

First integer is number of nodes on this page (N). It is followed by N*2 integers. First N integers are offsets
of key arrays for each node. Next N integers are offsets for value arrays for each node. Offsets are relative to page offset.
The last integer points to end of data, rest of the page after that offset is filled with zeroes.

Offsets of key array (number i) are stored at: ``PAGE_OFFSET + HEAD_SIZE + I*4``.

Offsets of value array (number i) are stored at: ``PAGE_OFFSET + HEAD_SIZE + (NODE_COUNT + I) * 4``.
