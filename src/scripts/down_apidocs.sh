#!/bin/sh

wget http://search.maven.org/remotecontent?filepath=org/mapdb/mapdb/$1/mapdb-$1-javadoc.jar -O /tmp/$$-javadocs.jar;
mkdir target/site/apidocs
cd target/site/apidocs;
jar xf /tmp/$$-javadocs.jar
rm /tmp/$$-javadocs.jar
