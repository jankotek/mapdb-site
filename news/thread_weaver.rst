Unit testing multi-threaded code with Thread Weaver
=====================================================

.. post:: 2016-01-24
    :tags: test concurrency weaver
    :author: Jan


MapDB uses `Thread Weaver <https://github.com/google/thread-weaver>`_ for verifying multi-threaded code.
It was written by Google developers and has pretty impressive features. Thread Weaver executes
code in multiple threads, adds breakpoints into each thread, and pauses thread  execution, until other thread progresses.
In theory that should catch all race conditions, because all possible combinations of delays are executed and verified.

If you are not familiar with Thread Weaver there is a `User Guide <https://code.google.com/p/thread-weaver/wiki/UsersGuide>`_
and `video presentation <https://www.youtube.com/watch?v=FvH4RBn2gJ8>`_.

Here is an example of how Thread Weaver is used with JUnit test. The source code of this example is
`here <https://github.com/jankotek/thread-weaver/blob/master/src/test/java/examples/UniqueListTest.java>`_:

.. code:: java

    public class UniqueListTest{

      private static final String HELLO = "Hello";

      private volatile UniqueList<String> uniqueList;

      @Test(expected = RuntimeException.class)
      public void testPutIfAbsent() {
        System.out.printf("In testPutIfAbsent\n");
        // Create an AnnotatedTestRunner that will run the threaded tests defined in this
        // class. We want to test the behaviour of the private method "putIfAbsentInternal" so
        // we need to specify it by name using runner.setMethodOption()
        AnnotatedTestRunner runner = new AnnotatedTestRunner();
        HashSet<String> methods = new HashSet<String>();
        runner.setMethodOption(MethodOption.ALL_METHODS, methods);
        runner.setDebug(true);
        runner.runTests(this.getClass(), UniqueList.class);
      }

      @ThreadedBefore
      public void before() {
        // Set up a new UniqueList instance for the test
        uniqueList = new UniqueList<String>();
        System.out.printf("Created new list\n");
      }

      @ThreadedMain
      public void main() {
        // Add a new element to the list in the main test thread
        uniqueList.putIfAbsent(HELLO);
      }

      @ThreadedSecondary
      public void secondary() {
        // Add a new element to the list in the secondary test thread
        uniqueList.putIfAbsent(HELLO);
      }

      @ThreadedAfter
      public void after() {
        // If UniqueList is behaving correctly, it should only contain
        // a single copy of HELLO
        assertEquals(1, uniqueList.size());
        assertTrue(uniqueList.contains(HELLO));
      }
    }

This example adds an element into thread-unsafe list. Insertion should be performed only once,
but list is thread-unsafe. Parallel threads will insert the same element twice.

Thread Weaver annotations are straightforward. You need public class with no-argument constructor, which will be instantiated multiple times.
``@ThreadedBefore`` initializes the class before the tests start. Methods annotated by ``@ThreadedMain`` and ``@ThreadedSecondary``
are executed in parallel in two threads. Code called from those two methods will be paused by breakpoints.
``@ThreadedAfter`` collects and verifies the result, and it should throw an exception if there is a mistake.


Our fork & Maven release
--------------------------

Thread Weaver stagnated a bit recently. The most recent commit is over one year old,
and `last release <http://mvnrepository.com/artifact/com.googlecode.thread-weaver/threadweaver>`_
in Maven Central is three years old.

We are quite keen on using Thread Weaver. So we `forked it <https://github.com/jankotek/thread-weaver>`_,
and made a new `Maven release <http://mvnrepository.com/artifact/org.mapdb/thread-weaver/3.0.mapdb>`_.
You can use our version with the following Maven dependency:

.. code:: xml

    <dependency>
	    <groupId>org.mapdb</groupId>
	    <artifactId>thread-weaver</artifactId>
	    <version>3.0.mapdb</version>
	    <scope>test</scope>
    </dependency>

We made some minor tweaks. First we automated the build process and turned it into a regular Maven project.
Original version is Ant based, requires you to download dependencies manually, and edit a few files before build.
Secondly we changed and simplified the project layout, and included more unit tests.
And thirdly we made Thread Weaver Java8 compatible.

Practical experience with Thread Weaver
----------------------------------------

MapDB uses Thread Weaver to verify concurrent Maps, for example here
`is code for HTreeMap <https://github.com/jankotek/mapdb/blob/mapdb3/mapdb/src/test/java/org/mapdb/HTreeMapWeaverTest.kt>`_.

We had a mixed experience with Thread Weaver.
It was very valuable for initial design and proof-of-concept verification.
But once methods become larger and contained locks, Thread Weaver failed.
We found the following problems while using it:


False positives
~~~~~~~~~~~~~~~~~~~~

Thread Weaver reported some false positives. Methods which were thread-unsafe passed the test.
It is probably related to default timeouts. The test reported false positive after finishing in 1 second.
We increased timeout, and the test ran for several seconds, and failed as expected.

But the increased timeout caused another problem once the test was fixed and methods become thread-safe.

Timeout issues
~~~~~~~~~~~~~~~~
The default timeout in Thread Weaver is 1000 ms. That is not enough to execute more complex methods, so it is necessary
to increase timeout. However increased timeout caused some breakpoints to fail. It is probably a bug, since
that method was never even executed:

.. code::

    Caused by: com.google.testing.threadtester.TestTimeoutException: Did not reach Breakpoint(1) @ at beginning of copyAddKeyDir
	    at com.google.testing.threadtester.AbstractBreakpoint.await(AbstractBreakpoint.java:186)
	    at com.google.testing.threadtester.ObjectInstrumentationImpl.interleave(ObjectInstrumentationImpl.java:285)


To avoid this problem we changed the breakpoint instrumentation. Instead of instrumenting all the methods
with ``MethodOption.ALL_METHODS``, we used ``LISTED_METHODS`` with a white list of methods.


No recursive instrumentation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

We found that Thread Weaver only adds a breakpoints into methods directly called from ``@ThreadedMain`` and ``@ThreadedSecondary``.
This proved to be a great problem, since our ``HTreeMap`` first calls public ``HTreeMap.put(key,value)`` and from there
it calls private ``HTreeMap.putInternal(key,value)``. But an internal method which needs verification was not instrumented!

It is probably possible to make recursive instrumentation work. But we were unable to do it in a reliable way.

We had to bend our code and call ``putInternal`` directly from Unit tests. Some internal methods have to be public
or package protected, there is  extra logic etc...


It froze
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Some of the methods we verified had about 50 lines of code with loops.
In theory that is a few million combinations and should finish within a few hours.
However the test class was executed a few times, and than  Unit test just froze (JVM has 0% CPU usage).

We added debug statements and the ``@ThreadedAfter`` verification method was executed only a few times (it should have been thousands or millions).
It is most likely related to Locks, because it only manifests once Locks are added into place.
Probably due to deadlock between breakpoints.

Alternatives
--------------

There are not many alternatives for Thread Weaver. The only automated solution is stress testing.
The code is executed several times in parallel, in hope that a race condition will manifest itself.
That is quite difficult with low probability race conditions and corner cases.

Another practical problem is that issues manifest randomly in a non-reproducible way.
It can be quite difficult to diagnose a problem after it is manifested in stress tests.

A good framework for concurrent stress testing is `JCStress <http://openjdk.java.net/projects/code-tools/jcstress/>`_.
We are going to use this framework with MapDB soon.

With MapDB we do one trick to increase the chance that race condition will manifest itself.
We put several delay markers ``//$DELAY$`` into code, at places which might be prone to race condition.
The code preprocessor that replaces markers with actual ``Thread.sleep(1)`` delays, then compiles the code and finally runs
concurrency stress test. Markers are not replaced all at once, but in several combinations, to increase race condition chances.

This approach needs a lot of time to execute. Each delay marker adds into combinations to execute, and number of permutations grows exponentially.
We estimated it would take about 2 CPU years to stress test 20,000 lines of code in MapDB 2.0.
This is doable with some cheap computing time on Amazon Spot Instances or a similar cloud service.


Conclusion
-------------

Thread Weaver has its quirks, but there is no alternative.
We found it to be a great aid for the initial prototyping of concurrent code at early design stages.
Once the prototype is tested as thread-safe, it can be refactored into more complex implementation.
We will use Thread Weaver again when designing concurrent Data Pump and Queues.

However Thread Weaver is not usable as automated verification tool.
It needs lot of baby sitting, does not work for complex code and does not produce reliable results.





