---
title: Scala, boxing and concurrent collections
layout: comments
tags: [Scala, Kotlin]
---

I am working on concurrent Scala code. Native Scala collections do not have that many features, so I have 
to use native Java collections together with some extra libraries (Eclipse Collections, Guava).
It is real pain to do use those from Scala, here is one example why you should just use Kotlin.

### File reader count

I need to track number of readers for each file. it is simple semaphore system using: `Map<File, Long>`, 
where key is file, value is number of readers. 

Ideally this map should support atomic operations (`cas`, `swap`, `computeIfAbsent`...). 
No locking should be required to maintain this map. 
So I need some sort of concurrent map.

Scala has `scala.collection.mutable.ConcurrentMap` interface, but without any actual implementations. 
Plus it does not have any useful functions.

Java has `java.util.concurrent.ConcurrentMap` with nice methods such as `compute`, `computeWithPresent` etc... So the choice is simple. 

The map declaration looks like this:

```scala
// scala code
val fileSemaphore = new ConcurrentHashMap[File, Long]()
```

### Lock

Now the locking  method. It should increment value (number of readers) in atomic way. 
If key (File) is not in map (has zero readers), it should insert default value `1`
```scala
// scala code
def fileLock(file:File){
    fileSemaphore.compute(file, {(file,value)=> 
      if(value==null) 1 
      else value+1
    })
  }
```
* The `compute` method will update map in atomic way. 
* If the key-value pair is not present, the `value` argument will be null
* Function returns new value, which is inserted into map

* This code is actually broken and just happens to work (see next chapter)

### Unlock

Now the file unlock method. 

* It should decrement number of readers
* If the number of readers reaches zero, it should remove key from Map (function returns `null`)

```scala
// scala code
def fileUnlock(file:File): Unit ={
  fileSemaphore.compute(file, {(file,value:Long) => 
    if(value==1L) null 
    else value-1
  })
}
```
This code does not compile, scalac can not infer function return type from `null`. It need cast for extra hint:

```scala
if(value==1L) null.asInstanceOf[Long] 
else value-1
```

It compiles and runs, but does not work. 
When number of readers reaches zero, the file is not removed from Map but its value is set to zero. 

### Nullable Long in Scala

Both lock and unlock methods are broken. 
If you convert this code to Java, it works. 
Problem is that Scala does not allow 
nullable longs. 
Any `null` variable or expression with type `Long` is silently converted to `0L`. 

First `lock` method just happens to work, because the non-existing key (null value) is silently converted to 0L and incremented. 
But the first line in this expression is never executed:

```scala
if(value==null) 1  // always false, never executed, value is never null, but 0L
else value+1
```

Unlock method never returns null (to remove file from map). 
First line is executed, but the return value is converted to 0L, and  inserted to map:

```scala
if(value==1L) null.asInstanceOf[Long]  //converted to 0L
else value-1
```

This conversion is pretty nasty. It swaps your values at runtime. I would expect something like that from Javascript, but not from strongly typed language. 

To be fair the scalac emits warning in lock method, but it is not fatal error. And unlock method passes without warning.

### Solution

I found it impossible to write correct solution in Scala. There are two workarounds:

* Remove `Long` definition and use `Any` to keep compiler away: `Map[File, Long]` becomes `Map[File,Any]`. 

* Write lock/unlock methods in Java. 

### Nullable types in Kotlin

I could not resist to show how elegant this code becomes in Kotlin with nullable types. 
So I rewrote code above in Kotlin.

```kotlin
// kotlin code
val readers = ConcurrentHashMap<File,Long>()

fun lock(file:File):Unit{
    readers.compute(file,{file, value ->
        if(value==null) 1L 
        else value+1
    })
}

fun unlock(file:File):Unit{
    readers.compute(file,{file, value ->
        if(value==1L) null 
        else value-1
    })
}
```

Fancy null operators (elvis) would not make code better.
Instead Kotlin provided another unexpected benefit; 
compiler found concurrency issue :-) 

Code above does not compile. If we unlock wrong file (not yet locked), value is null and `else value-1` would throw NPE.

So we need to handle case when wrong file is unlocked. 
This version is correct and compiles:

```kotlin
// kotlin code
fun unlock(file:File):Unit{
    readers.compute(file,{file, value ->
        if(value==null) 
            throw IllegalMonitorStateException("file not locked")
        if(value==1L) null
        else value-1
    })
}
```

PS: Keep on mind that `ConcurrentMap` is interface defined in Java code. Nullability information was added latter with external annotations :-)


Comments
-------------
    Avatar
    Tse-Wen Wang (Tom) • 3 years ago • edited

    Scala supports nullable Long through its support of Java classes. Here's how I would use nullable Long.

    import java.lang.{Long => JLong} // JLong will be alias for java.lang.Long

    null.asInstanceOf[JLong] // returns null

    --

    Mateusz Maciaszek • 4 years ago

    Wouldn't using AtomicRef with Optional type help to resolve all of these problems?


        Avatar
        Jan Kotek Mateusz Maciaszek • 4 years ago

        I guess you mean in combination with immutable Scala Map. It would not, it generates too much GC garbage.
                −
            Avatar
            Mateusz Maciaszek Jan Kotek • 4 years ago

            Not really, mutable version should be ok as well once dealing with concurrency control mechanism (hence not sure about GC pressure).