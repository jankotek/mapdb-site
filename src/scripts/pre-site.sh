#!/bin/sh
rm -rf target/site/apidocs
if [ -a target/site/blog/index.html]; then rm target/site/blog/index.html; fi;
if [ -a target/site/news.xml]; then rm target/site/news.xml; fi;
