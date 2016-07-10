TODO what to do with this page?

Credits
=======

Most of MapDB was written by Jan Kotek. He started this project and pretty much runs the show.

There were many people who contributed ideas, bug fixes and improvements. I am sorry if I forgot someone, please send me a line if you should be here.

JDBM 1
------

MapDB is loosely based on older project called JDBM. First version was started around 2001 and was released in 2005 at [SourceForge](http://jdbm.sourceforge.net/). There were several rewrites, there is no code left from original JDBM 1.0 in MapDB. But it influenced our design and general idea. Credit goes to Cees de Groot and Alex Boisvert.

JDBM 2 and 3
------------

JDBM stagnated until it was reanimated by Jan Kotek in 2009. JDBM 2 added Map interface and basic serialization (`SerializerBase` in MapDB). JDBM 3 brought NIO updates, POJO serialization and many performance improvements.

There were many people who improved JDBM 2 & 3 (and indirectly MapDB)

-   Kevin Day sent many ideas and patches, most importantly delta compression in BTree and packed longs.
-   Bryan Thompson worked on concurrency branch of JDBM 1.0, and helped to shape JDBM
-   Thomas Mueller (H2 DB) for code review and advices.

Serialization
-------------

Serialization code is complex and initially had lot of bugs. There were MANY people who submitted bug-fixes and I am sorry for not listing them all. Most importantly:

-   Nathan Sweet wrote Kryo serialization framework which inspired our POJO serializer to some extend(MapDB-Elsa).
-   Roman Levenstein refactored original simple POJO serializer and greatly improved its performance.

Collections
-----------

-   Theoretical design of BTreeMap is based on 1986 paper: [Concurrent operations on B∗-trees with overtaking](http://www.sciencedirect.com/science/article/pii/0022000086900218) by Yehoshua Sagiv.
-   More practical aspects of BTreeMap implementation are based on [notes](http://www.doc.ic.ac.uk/~td202/) and [demo application](http://www.doc.ic.ac.uk/~td202/btree/) from Thomas Dinsdale-Young.
-   Java Collections are very complex to implement. We took unit tests from Google Collections (Guava) which was great help. Credit goes to Jared Levy, George van den Driessche and other Google Collections developers.
-   Long(Concurrent)HashMap and some other classes were taken from Apache Harmony and refactored for our needs.
-   Some unit tests and javadoc comes from JSR166 group
-   BTreeMap uses some code from `ConcurrentSkipListMap` taken from Apache Harmony to implement all aspects of `ConcurrentNavigableMap`. Credit goes to Doug Lea and others.
-   Luc Peuvrier wrote some unit tests for `ConcurrerentNavigableMap` interface.

Other
-----

-   Thanks to my wife Pinelopi for tolerating awful amount of time I spend on this project. Also for proof reading and general advices.
-   XTea encryption was taken from H2 Database (Thomas Mueller)
-   LZF compression was ported to Java by Thomas Mueller. Original C implementation was written by Marc Alexander Lehmann and Oren J. Maurice
-   Stanimir Simeonoff from [Riflexo](http://www.riflexo.com/) reviewed and fixed some concurrency issues.

Donations
---------

MapDB development was generously sponsored in 2014 and 2015 by [AgilData](http://www.agildata.com).

Before AgilData we got donations from:

-   Chronon Systems donated [time traveling debugger](http://chrononsystems.com/). It is great help for fixing complex and hard to reproduce concurrent issues.
-   EJ-Technologies donated [JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html). It is excellent tool and MapDB would not be possible without it.
-   JetBrains donated [Intellij Idea Ultimate](http://www.jetbrains.com/idea/)
-   Michael Hunger donated 100 euro.
-   Jan Venema from [Vertx.io](http://vertx.io) donated 100 euro.

And several other donors.

*TODO this needs updating*
