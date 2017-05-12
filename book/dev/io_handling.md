IO Handling
====================

Some notes about handling IO operations, file access etc in MapDB.

File Rename & replace
------------------------

This operation replaces content of one file with another file. Original file (if existed )is usually destroyed.
File Rename is very fast on local filesystems. 

MapDB renames and replaces files in a few places; for example store compaction recreates store content in new file and than replaces original file. 


### Challenges

* old file might be open, all file handles must be closed before rename starts
* file may remain locked, even after it was closed (mmap on Windows)
* on replace old file is destroyed, but it should be preserved if rename fails
* file rename & replace must recover from crash
    * when new file is opened or created, we need to check if there are rename fragments
    * use atomic rename;  `java.nio.Files#move()` with `StandardCopyOption#ATOMIC_MOVE ATOMIC_MOVE` 



### File Replace algorithm

This is sequence for replacing files, to ensure it recovers from crash

* New File should have suffix `_renameNew`

* New File should be created in single atomic operation (rename from other file)
    * its content must be  valid from very beginning
    * on compaction use different suffix to create New File (`_compact`)
    * once temp file is created and synced, rename it to `_renameNew` and start proper file rename  

    
* check if Old File with suffix `_renameOld` file does not exist
    * fail if it does exist
        * TODO if db crashes before Old File is deleted, DB might not recover from crash here, perhaps delete file instead of throwing an exception?     
* try to close all file handles to Old File
    * if not possible (unmap fails) keep Old File file open and copy content, delete New File & exit
       
* check if Old File is writable, fail if not
* rename Old File, add suffix `_renameOld`
    * check Old File was renamed (original does not exit)
    * if rename fails copy content, delete New File and exit
* rename New File, remove any suffix
    * check New File was renamed (original file does not exist), throw an exception if it exists
* delete Old File (with `_renameOld` suffix)    
    * if delete fails, throw an exception
    
### File Open algorithm

This sequence should be used when file is opened, to ensure that DB recovers if system crashed in middle of file rename

* Check existence of following files (exists and non zero size):
    * F (no suffix)
    * NF (`_renameNew`)
    * OF (`_renameOld`)
    
Here is decision matrix, validation uses checksum or whatever, if validation is not possible just always assume valid file:


* F yes, NF no, OF no
     * usual case, just proceed, perhaps do not even validate
     
* F yes, NF yes, OF yes
    * strange situation, should not exist with atomic renames
        * crash possibly while file content was copied, some files might be corrupted (not fully copied)
    * validate NF
        * if valid, delete OF and proceed with File Replace, exit
        * if invalid, delete NF and continue in next step
    * validate F, 
        * if invalid rename OF to F and exit
        * if valid delete OF and continue
    * validate OF
        * if valid, rename it to F and use it, exit
        * if invalid, fail with fatal data corruption
        
* F yes, NF yes, OF no
    * store crashed before F could be renamed to OF
    * validate NF
        * if invalid delete NF and try to use F, return
        * if valid, proceed with NF to F rename, return

* F no, NF yes, OF yes
    * store crashed after F was renamed to OF, but before NF was renamed to F
    * validate NF
        * if valid proceed with replace
        * if invalid, delete it, rename OF to F and use it
        
* F no, NF yes, OF no
    * should not happen, 
    * validate OF, use it if possible, else fail
    
* F no, NF no, OF yes
    * should not happen
    * validate OF and hope for best
    
* F yes, NF no, OF yes
    * should not happen
    * validate F, use it if possible
    * else validate OF, use it if possible, else fatal error                   

* F no, NF no, OF no
    * no files
    * create new store if auto create configured, else fail to open