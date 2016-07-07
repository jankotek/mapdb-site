Volume
======

MapDB has its own storage abstraction similar to `ByteBuffer`. It is called `Volume` and resides in package [org.mapdb.volume](https://github.com/jankotek/mapdb/tree/master/src/main/java/org/mapdb/volume). It is growable, works over 2GB and has number of tweaks to work better with MapDB.

There are several implementations:

> -   Volume over multiple `byte[]`. Use `DBMaker.memoryDB(file)`
> -   `DirectByteBuffer` for direct memory. Use `DBMaker.memoryDirectDB()`
> -   `RandomAccessFile` is safer way to access files. It is enabled by default, use `DBMaker.fileDB(file)`
> -   `FileChannel` a bit faster then RAF. Use `DBMaker.fileDB(file).fileChannelEnable()`
> -   `MappedByteBuffer` for memory mapped files. Use `DBMaker.fileDB(file).fileMMapEnable()`
> -   Unsafe over memory region (heap or direct memory). This is not available in `DBMaker` yet.
> -   Unsafe over file. This is not available in `DBMaker` yet.

Construct DB from Volume
------------------------

You can supply your own Volume and create DB on top of it. Example bellow opens memory mapped file and creates DB over it. Note `contentAlreadyExists` boolean. It tells `DBMaker` if Volume already contains database and should be just opened, or is empty and it should be overwritten.

<!--- #file#volume_db.java--->
```java
File f = File.createTempFile("some","file");
Volume volume = MappedFileVol.FACTORY.makeVolume(f.getPath(),false);

boolean contentAlreadyExists = false;
DB db = DBMaker.volumeDB(volume, contentAlreadyExists).make();
```
Load Volume into memory
-----------------------

Volume content can be copied from one to another. This example opens inmemory Volume backed by `byte[]` and fills its content from file:

<!--- #file#volume_load_from_file.java--->
```java
//open file volume
String file = "pom.xml";
Volume fileVolume = MappedFileVol.FACTORY.makeVolume(file,false);

//create in memory volume
Volume memoryVolume = new ByteArrayVol();

```
It is also possible to load Volume content from `InputStream` or save Volume content into `OutputStream`

<!--- #file#volume_input_output_streams.java--->
```java
InputStream input = new FileInputStream("pom.xml");

Volume volume = new ByteArrayVol();
volume.copyFrom(input);

OutputStream out = new ByteArrayOutputStream();
volume.copyTo(out);
```
Memory mapped files can also cache file content into memory. It uses [MappedByteBuffer.load()](https://docs.oracle.com/javase/8/docs/api/java/nio/MappedByteBuffer.html#load--) method under hood:

<!--- #file#volume_mmap_load.java--->
```java
//open memory mapped file
String file = "pom.xml";
MappedFileVol fileVolume = (MappedFileVol) MappedFileVol.FACTORY.makeVolume(file,false);

```