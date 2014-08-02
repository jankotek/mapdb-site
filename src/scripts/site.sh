#!/bin/sh

echo "Download javadocs"
sh src/scripts/down_apidocs.sh $2

echo "Create ebooks"
sh src/scripts/create_ebook.sh

echo "Create blog index"
python src/scripts/blog_preview.py $3

echo "Create rss and news"
python src/scripts/create_rss.py $3

rm src/scripts/libs.pyc
