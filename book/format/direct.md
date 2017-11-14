
### Dict

#### block
Storage space is sequence of fixed size blocks. IO operations (read byte[], write long...) can not cross block boundaries. 

#### zero block 
 block at zero offset, very first block in file

#### file 
file is storage space with uniform addressing (single increasing offset). MapDB was originally file only, but now *file* could be also in memory, on-partition... TODO use *volume* name?

#### record
Serialized object stored in database. Usually btree node, linked node... 

#### recid
Record ID. Unique identificator for stored record, similar to memory pointer... Put operation will return unique  recid. Recids are reused after delete.


-------



### Header 

Zero block has special format. 

### Index table

Translates logical offset (recids) to physical offset in file. Recid is offset Index Table. 


#### Slot Format

### Long stack

Stack of longs, used to store information about free space and free slots in Index Table

#### Recursion 





Files have uniform addressing space (single offset), their can be expanded or shrinked.  Memory store might be implemented as series of `byte[]`, to allow expansion.