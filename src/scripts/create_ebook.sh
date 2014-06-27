#!/bin/sh

touch /tmp/$$.html
for f in `find . | grep -e "/target/site/[0-9]" | sort`
do
 awk 1 $f | sed -n '/<!-- Masthead/,/<!-- \/container -->/p' >> /tmp/$$.html
done
pandoc -s -o ./target/site/doc/mapdb-ebook.html --toc --toc-depth=2 /tmp/$$.html -f html
pandoc -s -o ./target/site/doc/mapdb-ebook.pdf --toc --toc-depth=2 /tmp/$$.html -f html
pandoc -s -o ./target/site/doc/mapdb-ebook.epub --toc --toc-depth=2 /tmp/$$.html -f html
ebook-convert ./target/site/doc/mapdb-ebook.epub ./target/site/doc/mapdb-ebook.mobi >/dev/null 2>/dev/null
rm /tmp/$$.html
