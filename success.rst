Who is using MapDB
=============================

Following companies are using MapDB:

- Twitter

- Linkedin

- Credit Suisse

- Deutsche Bank

- HP Labs

Here is what users wrote about MapDB:

GigaSpaces
~~~~~~~~~~~~~~~~~~~~~~

XAP (memory data grid) is using MapDB, an embedded database engine which provides concurrent Maps, Sets and
Queues backed by disk storage or off-heap memory. When writing an object to the space,
its indexed data maintained on-heap and it’s raw data is stored on off-heap.

`XAP manual <http://docs.gigaspaces.com/xap102adm/memoryxtend-ohr.html>`_

ModusP
~~~~~~~~~~~
We have successfully finished our consultation session with Jan Kotek
for MapDB integration. With Jan's help we've successfully ran our
mapping task using mapDB after weeks of failing to do this using any
other method (and we've tried many).

It was my first use of MapDB, and for the first time I learned about
the big limitations of the java collections and since using MongoDB was
also not efficient due to the huge amount of IO required. MapDB was the
perfect solution for us.

It was a great pleasure to work with Jan, he is an amazing
professional

Tom B., `ModusP <http://www.modusp.com/>`_

FullContact
~~~~~~~~~~~~~~~~~~~~~~

MapDB has been a fantastic resource, allowing us to transparently move
traditionally a SQL-backed application into main memory for performance.
It's easy to use, and the examples are quite good. It definitely follows
the principle of least surprise. It actually surprised me that things "just worked",
and required very little effort to get going.

I replaced a 6-table system, relations, secondary indexes, and all into MapDB.
It was quite simple and blazing fast.

Michael Rose, Senior Platform Engineer, `FullContact <http://www.fullcontact.com>`_

OpenStreet map import
-----------------------

MapDB is great product. Today I finished importing file planet.osm.pbf from OpenStreetMap project that is ~27GB huge. All data after import with spatial index and simple search index by names took only ~60GB. The best thing is that it only took 3.5 hours!!!!

Configuration: pre MapDB 2.0 alpha 1, Java 8 x86_64, Xmx: ~5GB, SSD disk, CPU up to 4 cores

This is only first run with these data but it looks very promising. It's a shame that now I have to work on another project. So I can continue with MapDB and OSM data on spare time :-(. Later on I will test latest MapDB 2.0.

Thank you very much

`Martin <https://groups.google.com/forum/#!topic/mapdb/EaU4vV7Gyhk>`_


OpenTripPlanner
~~~~~~~~~~~~~~~~~~~~~~

First, thanks for all your work on MapDB.
It is becoming an essential part of our work on the
`OpenTripPlanner project <https://github.com/opentripplanner/OpenTripPlanner>`_
and `our other geospatial/transport analysis work <http://conveyal.com/>`_.
We are currently modifying OpenTripPlanner to use MapDB
for its street and public transport schedule import step.

`Andrew Byrd <https://groups.google.com/forum/#!msg/mapdb/EaU4vV7Gyhk/tmjYVvZa3GEJ>`_


Norconex
~~~~~~~~~~~~~~~~~~~~~~

I am glad to have discovered this ultra-fast Java Map implementation that scales linearly, is not limited by JVM memory, and persists to disk as you write to it.  For an embedded key/value storage solution, MapDB tops the list for being so efficient and easy to use. It has been used as the default URL storage mechanism for Norconex Collectors for a few years now.

Pascal Essiembre - President at `Norconex <http://www.norconex.com>`_


bliss
~~~~~~~~~~~

Our app uses MapDB as a key-value store. We originally started with JDBM
back in 2009 and made the port to MapDB in 2015.

The main reason I chose MapDB is because I dislike the impedance mismatch
of trying to force a Java object graph into a relational database. Not
having any particular need for a relational database, I decided a key-value
store storing Java objects is a nicer fit. MapDB appears very performant
under highly concurrent loads. I like how the dependencies are basically
zero, and the code itself is very concise, which helps when you are trying
to work out how it works.

Our application is downloadable software, and MapDB is used to write to the
user's local hard drive. We have around 500,000 installations, so we know
that MapDB works well on a wide variety of OSes and hardware, from desktops
to NASes and music servers.

Dan Gravell - `bliss <http://www.blisshq.com/>`_

