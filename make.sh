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

# create html site
ablog build

# create dokka documentation from ../mapdb
(cd ../mapdb && mvn clean compile dokka:dokka)
mkdir _website/dokka
mv ../mapdb/target/dokka _website/dokka/latest
