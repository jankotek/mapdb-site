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
-------------------

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

File rename is used in StoreDirect compaction. Store is recreated in new file, and old file is replaced with new content.
The 'old file' is file which is being replaced, it will be deleted before File Rename. The 'new file'
replaces old file and has its name changed.

MapDB needs file move to be atomic, and supported in range variety of platforms. There are following problems

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

StoreDirect
------------------

StoreDirect updates records on place. Space used by records get reused.
It keeps track of free space released deletion and reuses it.
It has zero protection from crash, all updates are written directly into store.

StoreDirect allocates space in 'pages' of size 1MB. Operations such as ``readLong``, ``readByte[]``
must be aligned so they do not cross page boundaries.


Index page
~~~~~~~~~~~~~~~~~~~~~~~~
Linked list of pages. It starts at ``FIRST_INDEX_PAGE_POINTER_OFFSET`` parity16.
Link to next page is at start of each page.

Last index page may not be completely filled. Maximal fill is indicated by Max Recid stored at ``INDEX_TAIL_OFFSET`` parity3+shift.

Index Page at start contains:

- first value is **pointer to next index page** parity16
- TODO second value in page is **checksum of all values** on page

Rest of the index page is filled with index values.


Sorted Table Map
---------------------

``SortedTableMap`` uses its own file format. File is split into page,
where page size is power of two and maximal page size 1MB.


