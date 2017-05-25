---
title: Kotlin vs Java - Lambdas and Locks
layout: comments
tags: [Kotlin]
---

A few months ago ago I wrote post named [Scala has weakly typed syntax](http://www.mapdb.org/blog/scala_has_weakly_typed_syntax/). 
I should expand it a bit: Scala is excellent language, but Kotlin is far better. 
When comparing Kotlin to other languages, you only see nicer syntax. 
But Kotlin really shines, once you start using  for daily tasks, I programmed in Kotlin for 3 years. 
So I am starting small blog series, to describe less known advantages of Kotlin programming language. 
All articles from this series are available under [Kotlin tag](http://www.mapdb.org/blog/tag/#Kotlin).

## Locking within try-finally

Locks (and other resources) are usually handled within `try..finally` block. In java:

```java
Lock lock = new ReentrantLock();       
lock.lock();
try{
    //thread safe action
}finally {
    lock.unlock();
}
```
This works reasonably well. 
It is easy to refactor old (thread unsafe) code to use this new construct.    
But it is easy to introduce errors, 
for example if a different lock variable is unlocked, or `ReadWriteLock` unlocks readLock instead of writeLock... 

One way is to wrap thread safe code into lambda, and specify lock only once.

It would be nice to have `withLock` [directly on Lock interface](https://stackoverflow.com/questions/24034240/why-didnt-java-8-add-withlock-default-methods-to-the-java-util-concurrent-lo), but maybe in Java 9. 
In Java8 we have to specify our own method which takes lock and lambda:

```java
public static void withLock(Lock lock, Runnable block) {
    lock.lock();
    try {
        block.run();
    } finally {
        lock.unlock();
    }
}

Lock lock = new ReentrantLock();

withLock(lock, () -> {
    //thread safe action
});
```        

It works, lock is specified only once, but this creates some new problems:

* New `Runnable` (lambda) is allocated on every invocation. I do not think that is a problem for modern JVM with good GC.

* Lambda invocation inside method adds three new stack frames. That is big overhead, and it prevents JIT.

* It breaks local context; because lambda is executed somewhere else, one can not return from method or continue local cycle.
  
* Extra stack frames complicates debugging and obscures error messages: 

![Stack frames from lambda](/images/blog/kotlin1-java-lambda-stack.png)


### Try finally in local frame

Things get complicated once you need more than code wrapping. Lets take simple getter:
```java
public String getName(){
    lock.lock();
    try{
        return name;    
    }finally{
        lock.unlock();    
    }
}
```
We can not use simple `Runnable` lambda, this does not compile:

```java
public String getName(){
    withLock(lock, () -> {
        return name; // << compilation error
    });
}
```

We can use `Callable` to return value from lambda:

```java
public static <V> V withLock(Lock lock, Callable<V> block) throws Exception {
    lock.lock();
    try {
        return block.call();
    } finally {
        lock.unlock();
    }
}

public String getName() throws Exception {
    return withLock(lock, () -> name);
}
```
And if you nest multiple locks, things get hairy pretty quickly:

```java

public String getName() throws Exception {
    return withLock(lock, () -> {
        return withLock(lock2, () -> {
            return name;
        });
    });
}
```

### Behold the power of Kotlin inline!

Now lets see what solution Kotlin offers to for this problem (but Java developer would not even see it as a problem ;) ).

Kotlin has [inline methods and inline lambdas](https://kotlinlang.org/docs/reference/inline-functions.html).
I recommend you to read the chapter, to get some theory. But in short:

* Kotlin can inline `withLock` method into our code, so no `Runtime` is allocated and no extra stack frames are added

* Inline is type safe, compiler will fail with an exception, if method can not be inlined for some reasons (dependency...). So no nasty performance surprises.

* Compiler actually works, inlined code is executed within local stack frame. We can do non-local `return`s.  For future there is even planned support for `continue` and `break` for loops in parent function.,

Here is first Kotlin example, simple `withLock` method. Kotlin already provides extension method on `Lock` interface:

```kotlin
val lock = ReentrantLock()
lock.withLock { 
    //thread safe code
}
```

But to keep things comparable, lets implement similar method from Java:

```kotlin
inline fun <T> withLock2 (lock: Lock, body: () -> T): T {
    lock.lock()
    try{
        return body()
    } finally {
        lock.unlock()
    }
}

val lock = ReentrantLock()
withLock2(lock){
    //thread safe code
}
```

Here is proof from debugger that this  method and lambda are both inlined, and do not add any new stack frame:

![Stack frames from lambda](/images/blog/kotlin1-java-lambda-stack2.png)

Another nice feature is non-local return. You can put `return` statement inside inlined lambda. 
Because it shares the same stack frame with parent method, you can exit parent method from within lambda:

```kotlin
fun getName():String {
    lock.withLock {
        return name2 //return from getName()
    }
}
```

You can also nest multiple inline methods, all gets inlined into single frame. 
This example uses two nested locks:

```kotlin
fun getName():String {
    lock.withLock {
        lock2.withLock {
            return name2 // returns from getName()
        }
    }
}
```


In next post I will mention that return from lambda also works on collection methods:

```kotlin
// taken from kotlinlang.org
fun hasZeros(ints: List<Int>): Boolean {
    ints.forEach { //this is inlined lambda
        if (it == 0) 
            return true // returns from hasZeros
    }
    return false
}
```
