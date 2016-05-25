#!/bin/bash

# terminate script if any command fails
set -e
set -o pipefail

# cleanup
ablog clean
# run maven tests in this project
mvn clean test

# make PDF manual
(cd doc && make latexpdf)
cp doc/_build/latex/MapDB.pdf down/mapdb-manual.pdf
rm doc/_build -rf

# create html site`
ablog build

# create dokka documentation from ../mapdb
#(cd ../mapdb && mvn clean compile dokka:dokka)

JAVA_HOME=${1:-/usr/lib/jvm/java-8-openjdk-amd64}

mkdir _website/dokka/latest/mapdb -p
java  -cp ../bin/dokka-fatjar.jar:$JAVA_HOME/lib/tools.jar org.jetbrains.dokka.MainKt ../mapdb/src/main/java/  -output _website/dokka/latest/mapdb/

mkdir _website/javadoc/latest/mapdb -p
java  -cp ../bin/dokka-fatjar.jar:$JAVA_HOME/lib/tools.jar org.jetbrains.dokka.MainKt ../mapdb/src/main/java/  -output _website/javadoc/latest/mapdb/ -format javadoc
