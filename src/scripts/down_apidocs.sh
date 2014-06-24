#!/bin/sh

mkdir /tmp/$$; wget http://search.maven.org/remotecontent?filepath=org/mapdb/mapdb/1.0.3/mapdb-1.0.3-javadoc.jar -O /tmp/$$-javadocs.jar;
mkdir target
mkdir target/site
mkdir target/site/apidocs
cd target/site/apidocs;
jar xf /tmp/$$-javadocs.jar
rm /tmp/$$-javadocs.jar
