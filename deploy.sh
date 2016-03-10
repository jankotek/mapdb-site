#!/bin/bash

set -e
set -o pipefail

(cd target && git clone https://github.com/jankotek/mapdb-site --depth 1 -b gh-pages)

rm target/mapdb-site/* -rf

cp CNAME target/mapdb-site/
cp _website/* target/mapdb-site/ -rf

(git add -A && git commit -m "update site"  && git push)

