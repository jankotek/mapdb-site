
All binary file format in MapDB start with 32byte header. 
That identifies type of file, its format version and features used in  


Structure

- 2 bytes - format ID, (`StoreDirect`, `StoreWAL`....)
- 2 bytes - format version, 
    - number increases with backward incompatible changes (new features)
    - MapDB will refuse to open newer formats to prevent data corruption
- 4 bytes - Wide Flags; features supported by most formats 
    - file wide checksum ?, type of checksum used... (CRC32,)
    - file size supported?
    - pointer checksums?
    - type of compression?
    - some basic serialization info?
    - is the space zeroed out?
    
- 8 bytes - file size, if set in wide feature flag
- 8 bytes - 64 bit or 32bit checksum (both will use 8 bytes)
- 8 bytes - Format Flags - features only supported by single format
    - type of pointer checksums
    - how zero space 
    
    
    