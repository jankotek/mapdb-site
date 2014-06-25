#!/bin/sh
touch /tmp/$$.mk
for f in `find . | grep -e "/src/site/markdown/[0-9]" | sort`
do
 awk 1 $f >> /tmp/$$.mk
done
mkdir ./target/site/doc/
pandoc -s -o ./target/site/doc/mapdb-ebook.html --toc --toc-depth=2 /tmp/$$.mk
pandoc -s -o ./target/site/doc/mapdb-ebook.pdf --toc --toc-depth=2 /tmp/$$.mk
pandoc -s -o ./target/site/doc/mapdb-ebook.epub --toc --toc-depth=2 /tmp/$$.mk
ebook-convert ./target/site/doc/mapdb-ebook.epub ./target/site/doc/mapdb-ebook.mobi >/dev/null 2>/dev/null
rm /tmp/$$.mk
