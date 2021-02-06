---
title: Scala has weakly typed syntax
layout: comments
tags: [Scala, Kotlin]
---

I recently gave a talk named "Scala vs Kotlin vs Java8" at  [Geecon conference](http://2016.geecon.cz).
It compared three languages for the purposes of practical, daily programming. 
Video from this talk will be [available in a few weeks](https://vimeo.com/geecon).
This blog post is based on code examples from this talk.

Scala was build from the beginning as "Scalable language". 
It was designed to support many use cases, not just the relatively narrow 
"java replacement" use. Bellow is a quote from [Martin Odersky](http://www.artima.com/scalazine/articles/scalable-language.html):

*The name Scala stands for “scalable language.” The language is so named because it was designed to grow with the demands of its users. You can apply Scala to a wide range of programming tasks, from writing small scripts to building large systems.*

For that reason Scala has some features that are not needed by most programmers.
The markup language support is a good example, `<xml/>` is actually a valid
syntax in Scala.

Scala also has very relaxed syntax, that differentiates it from
Kotlin and Java8 (which are other JVM strongly typed languages).

Relaxed syntax allows Scala fluent programming and to mimic other languages such as SQL.
It is great for creating small domain-specific-languages (DSL) on top of Scala.
However, the relaxed syntax comes at the cost of clarity. 
Stricter syntax makes code more deterministic, and thus easier to read and understand.
Relaxed syntax also makes compiler more complicated, an compiler error messages harder to understand.

My point is that Scala is a strongly typed language. 
However, the relaxed syntax brings it closer to 'weakly typed' languages. So its syntax is 'weakly typed'. 
The compiler is not as strict as it could be, and sometimes it generates unexpected code. 

Scala does not require semicolons and dots on the member function calls. 
Some expressions are finished by the end-of-line, but some are not. 
That might cause unexpected results. `
SortedMap`, in this example, should be instantiated with  reverse order comparator, 
but the call to the `reverseOrder` comparator is ignored because it is on the second line. 
Another problem is on the last line, where the wrong method is called:

```scala
def put(a:Any, b:Any): Unit = {}

import java.util.Collections.reverseOrder

val map = new java.util.TreeMap[Integer, String]
  (reverseOrder[Integer]())

map.put(1,"one")  // this works
map put(2,"two")  // this works as well
map
  put(3,"two")  //put is local method, not map.put
```

Complex conditions usually have each statement on a new line. 
But that might not be possible in Scala:

```scala
// all in parenthesis, works fine
if(1==1
  || false){
  println("true")
}

// multiline condition does not work
val condition =
  1==1
  || 1==2
```

In Scala any sequence of characters could be a method name. 
Extra space might be needed if `=` is part of method name:

```scala
assert(-1 == -1)
assert((-1).==(-1))
// this expression does not work without extra space, 
// `==-` might be a valid function name
assert(-1 ==-1)
```

Tuples are build into Scala syntax, pair is created with a simple `(1,2)` expression.
However, because the parenthesis in Scala are overloaded for many uses, that may lead
to confusing situations:

```scala
val buf =
  mutable.Buffer[(Int, String)]()
// no parenthesis causes error, because Type is referred instead of
buffer:
// mutable.Buffer[(Int, String)]

buf += ((1, "aa"))

// Pair needs two parenthesis, single parenthesis will fail
buf += (1, "aa")
```

And finally, probably the most relaxed part of the Scala syntax: lambda expressions. 
Scala is built around lambdas; `if`, `else`, `for` and other languages
constructs can be translated as call to a method, which takes a lambda as a parameter. 

There are several ways to create lambda. 
It can be wrapped with `(` or `{` , sometimes parenthesis are not required. 

It is also not clear when lambda is invoked. 
For example this example could create a
value with `String` type, but also a value which holds a lambda type:

```scala
val str:String = {
  //lambda returns string
  "some string"
}
// ^^ Kotlin requires () here
```

Here is a reason why I wrote this blog post. 
In example bellow, the first lambda is not invoked at all. 
The problem is in `() =>` part, it renders the lambda as unexecutable. 
I will leave it as homework for reader to find out why. 
We did not found this problem at quick code review, it was latter discovered by unit tests.

```scala
/** this method takes lambda as parameter, measures its execution time */
def stopwatch(computation: => Unit): Long = {
  val s = System.currentTimeMillis()
  computation
  System.currentTimeMillis() - s
}

stopwatch{ () =>
  println("First called")   // << this code is never invoked
}

stopwatch{
  println("Second called")  // << this is executed
}
```

And in the end, something for your own amusement. 
Scala uses `=>` arrow in lambda  calls. 
But Java8 and Kotlin have decided to use `->` arrow instead, and here is the why:
```scala
List(1).filter{i=>1<=i}
```


Comments
---

```
James Ward • 4 years ago

In your stopwatch() example the parameter isn't actually a lambda. 
It is a by-name parameter.

If you want to take a lambda that is Unit => Unit then you'd need 
to do:

def stopwatch(computation: () => Unit): Long = {
val s = System.currentTimeMillis()
computation()
System.currentTimeMillis() - s
}

More good info from https://tpolecat.github.io/...

•
Reply
•
Share ›

Avatar
Blatant_Bolt • 4 years ago

your computation lambda example is an issue, but I noticed it 
instantly.

However, I'm not sure it is better in other languages once you
go past this Unit example --

def stopwatch(computation: Task[Unit]): Long = {
val s = System.currentTimeMillis()
computation
System.currentTimeMillis() - s
}

where Task has a 'run' method. One may forget to run it --
 any code lifted up into a context and lazily executed can have this issue. All of the built in functions, etc. All 'function-ish' types have this problem and no language you mention here solves it.

There was a discussion on scala-internals not too long ago
 about having a type be able to declare that it should not be left as an 'unused' value in a statement without a compiler warning. => Unit could be one such built-in type, and users could say '@functionType class Task ...' Then, the compiler would not allow the type to dangle in a statement without being executed.

After all, this is valid java:

long stopwatch(Runnable computation) = {
long s = System.currentTimeMillis();
computation;
return System.currentTimeMillis() - s;
}

'=> Unit' is just a syntactic sugar tool for '() => Unit' with 
slightly different syntactical behavior in various positions in the source.

•
Reply
•
Share ›

        −
    Avatar
    Jan Kotek Blatant_Bolt • 4 years ago

    Problem with lambda code in Scala is that there are two ways, 
    and one of them is not working. We wrote unit test for `stopwatch` method and it worked (with second case from blogpost). Than it stopped to work when first case was used.

    Java code where `computation.run() `is never invoked would not 
    pass `stopwatch` unit tests. It would be much easier to 
    discover it problem.
```