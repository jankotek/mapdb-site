---
title: Memory mapped file and JVM crash
layout: single
tags: [MMap]

---

Memory mapped (mmap) file reatly improves performance over traditional `RandomAccessFile` or `FileChannel`. However it has quirks and could cause JVM crash. MapDB contains some workarounds to make it more stable.

First bit of theory. Memory mapped files are using native addressing on operating system. That removes address translation, and makes them fast. On 32bit operating system addressing limit (and maximal mmap file size) is 4GB So its not good idea to use mmap files on 32bit operating systems

On 64bit Linux the mmap file uses delayed writes. Modified data are placed into write cache, to be written to disk some time in future. Latter if write fails, Linux kernel sends SIGBUS signal to JVM process, JVM does not have signal handler installed and crashes in result. More details are [in this blog post](https://www.javacodegeeks.com/2014/03/detecting-write-failures-when-using-memory-mapped-files-in-java.html?utm_content=bufferb5022&utm_medium=social&utm_source=twitter.com&utm_campaign=buffer).

MapDB can not fix the JVM crashes, but it tries to minimize conditions when write fails. The mmap file is not enabled by default. Slower but safer `RandomAccessFile` is default choice. Mmap file can be enabled with `fileMmapEnable()` option or with `fileMmapEnableIfSupported()` option which enables mmap file only when operating system supports it (64bit Unix).

Finally here are some workarounds which make mmap bit slower, but safer:

Sparse files and free disk space
--------------------------------

Most modern filesystems use [sparse files](https://en.wikipedia.org/wiki/Sparse_file). In short: if you allocate 1GB file, it is filled with zeroes and consumes zero space. Sparse file allocates disk space lazily allocated once data are written. Sparse file presents problem for mmap files in MapDB. JVM crashes if lazy allocation fails, and it always happens if there is no free disk space.

I made some debugging to see how this manifests. The source code for all examples is [on github](https://github.com/jankotek/mapdb-site/tree/master/src/test/java/blog/mmap_and_jvm_crash).

First lets create file `disk-image` filled with zeroes, format it as a filesystem, and mount it as a loopback device:

```
dd if=/dev/zero of=disk-image count=40960
/sbin/mkfs -t ext4 -q disk-image
mkdir fs
mount -o loop=/dev/loop0 disk-image fs
```

And here is a demonstration of JVM crash. It expands mmap file until filesystem runs out of free space. We need `IOException` to be thrown at some point, but JVM crashes instead:

<!--- #file#blog/mmap_and_jvm_crash/MMap_Crash.java--->
```java
byte[] buffer = new byte[1024*1024];

FileChannel channel = FileChannel.open(file.toPath(),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.READ);

//allocate in infinite cycle
for(long offset=0; ; offset+=buffer.length){
    ByteBuffer mappedBuffer = channel.map(
            FileChannel.MapMode.READ_WRITE,
            offset,
            offset+buffer.length);

    //this causes JVM crash if there is no free disk space and delayed write fails
    mappedBuffer.put(buffer, 0, buffer.length);
}
```
`FileChannel` allocation is a bit slower, but throws an exception instead:

<!--- #file#blog/mmap_and_jvm_crash/Channel_Crash.java--->
```java
FileChannel channel = FileChannel.open(file.toPath(),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.READ);

ByteBuffer mappedBuffer = channel.map(
        FileChannel.MapMode.READ_WRITE,
        0,
        buffer.length);

//write into buffer in infinitive cycle
while(true){
    mappedBuffer.rewind();
    //this causes JVM crash if delayed write fails
    mappedBuffer.put(buffer, 0, buffer.length);
}
```
To use mmap file we need to disable sparse file and do allocate all disk space. However that is not supported by JVM. The only option is to force allocation by overwriting new file space with zeroes

I tried to overwrite data with `FileChannel`. However the JVM still crashes, I think `FileChannel` has some write caching on its own and all data are not written to disk Adding a few extra bytes to file end (after mmap area) helps and JVM no longer crashes:

<!--- #file#blog/mmap_and_jvm_crash/MMap_Crash_Preclear.java--->
```java
byte[] buffer = new byte[1024*1024];

FileChannel channel = FileChannel.open(file.toPath(),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.READ);

// allocate in infinite cycle
for(long offset=0; ; offset+=buffer.length){

    //preclear space before it is mapped, so there is no sparse file
    ByteBuffer preclearBuffer = ByteBuffer.wrap(buffer);
    while(preclearBuffer.remaining()>0) {
        channel.write(preclearBuffer, offset);
    }
    //and allocate extra a bit of extra space,
    preclearBuffer = ByteBuffer.wrap(buffer, 0, 16);
    while(preclearBuffer.remaining()>0) {
        channel.write(preclearBuffer, offset+buffer.length);
    }

    ByteBuffer mappedBuffer = channel.map(
            FileChannel.MapMode.READ_WRITE,
            offset,
            offset+buffer.length);

    //this causes JVM crash if there is no free disk space and delayed write fails
    mappedBuffer.put(buffer, 0, buffer.length);
}
```
`RandomAccessFile` has no write cache and is better choice. Code bellow does not cause crash and does not need extra bytes at EOF:

<!--- #file#blog/mmap_and_jvm_crash/MMap_Crash_Preclear_RAF.java--->
```java
byte[] buffer = new byte[1024*1024];

RandomAccessFile raf = new RandomAccessFile(file, "rws");
FileChannel channel = raf.getChannel();

// allocate in infinite cycle
for(long offset=0; ; offset+=buffer.length){

    //preclear space before it is mapped, so there is no sparse file
    raf.seek(offset);
    raf.write(buffer);

    ByteBuffer mappedBuffer = channel.map(
            FileChannel.MapMode.READ_WRITE,
            offset,
            offset+buffer.length);

    //this causes JVM crash if there is no free disk space and delayed write fails
    mappedBuffer.put(buffer, 0, buffer.length);
}
```

Clearing newly allocated space with zeroes solved JVM crash when free disk space runs out. Downside is that writes are slower when file expands, but it does not affect write speed once store reaches its maximal size. Example above is integrated into MapDB and named **preclear** feature.

Preclear is enabled by default (if mmap file is enabled) and can be disabled with `fileMmapPreclearDisable()` option.

JVM crash if file becomes unavailable
-------------------------------------

I tried to simulate fatal hardware failure on system with frequent writes. I did:

1.  force unmount with `sudo umount fs -l`
2.  physically unplug USB flash drive
3.  physically unplug SATA drive from running system

JVM does not crash in this scenario. I will do more investigation, but so far I am happy with the way MapDB handles hardware failures.

JVM crash after unmap
---------------------

Access to buffer which is no longer mapped (was closed or unmapped) causes JVM to crash. That buffer has invalid address and access will cause segmentation fault. For that there is no official way in Java to close memory mapped file or `DirectByteBuffer`. There is a workaround which uses unpublished API to unmap buffer.

Find more details in this [Sun bug report](http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4724038).

Lets demonstrate the JVM crash after unmap on memory mapped file:

<!--- #file#blog/mmap_and_jvm_crash/MMap_crash_write_into_closed_MappedByteBuffer.java--->
```java
byte[] buffer = new byte[1024*1024];

FileChannel channel = FileChannel.open(file.toPath(),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE,
        StandardOpenOption.READ);

MappedByteBuffer mappedBuffer = channel.map(
        FileChannel.MapMode.READ_WRITE,
        0,
        buffer.length);

//write into buffer
mappedBuffer.rewind();
mappedBuffer.put(buffer, 0, buffer.length);

//close MappedByteBuffer
unmap(mappedBuffer);

//now write into closed buffer, JVM will crash
mappedBuffer.rewind();
mappedBuffer.put(buffer, 0, buffer.length);
```

`DirectByteBuffer` used in `DBMaker.memoryDirect()` store also crashes:

<!--- #file#blog/mmap_and_jvm_crash/MMap_crash_write_into_closed_DirectByteBuffer.java--->
```java
byte[] buffer = new byte[1024*1024];

ByteBuffer directBuffer = ByteBuffer.allocateDirect(buffer.length);

//write into buffer
directBuffer.rewind();
directBuffer.put(buffer, 0, buffer.length);

//close ByteBuffer
unmap(directBuffer);

//now write into closed buffer, JVM will crash
directBuffer.rewind();
directBuffer.put(buffer, 0, buffer.length);
```

MapDB supports unmap hack, it is disabled by default and can be enabled with `cleanerHackEnable()`. However enabling Cleaner Hack opens race condition, where concurrent access to store, while its being closed will cause JVM crash.

Future versions of MapDB will add another option, where this race condition is removed at price of extra concurrent locking and reduced performance.
