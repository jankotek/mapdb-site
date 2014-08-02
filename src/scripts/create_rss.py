#!/usr/bin/python
# -*- coding: utf-8 -*-
from libs import *
import sys,os

RSS_FEED_TITLE = "MapDB"
RSS_FEED_LINK = "www.mapdb.org"
RSS_FEED_DESCRIPTION = "MapDB provides concurrent Maps, Sets and Queues backed by disk storage or off-heap-memory. It is a fast and easy to use embedded Java database engine."
RSS_FEED_LANG = "en"

files = [(get_date(f), get_header(f), get_preview(f), f) for f in get_files("") if get_header(f)]
files.sort()

output = u"""<?xml version="1.0" encoding="utf-8"?>
<rss version="2.0"><channel><title>{title}</title><link>http://{link}</link><description>{description}</description><language>{language}</language>""".format(title=RSS_FEED_TITLE, link=RSS_FEED_LINK, description=RSS_FEED_DESCRIPTION, language=RSS_FEED_LANG)
for f in files:
    output = output + u"<item><title>{title}</title><link>http://{link}</link><description>{description}</description><pubDate>{date}</pubDate><guid>http://{link}</guid></item>".format(
        date=f[0].strftime("%a, %d %b %Y %H:%M:%S %z")+"GMT", title=f[1], description=f[2],link=u"www.mapdb.org/"+f[3])
output = output + u"</channel></rss>"
rss = codecs.open(PATH+"news.xml","w", "utf-8")
rss.write(output)
rss.close()

output = u"<ul>"
files.reverse()
for f in files[:5]:
    output = output + u'<li><p>{date} <a class="externalLink" href="{link}">{title}</a>. {description}</p></li>'.format(
        date=f[0].strftime("%Y-%m-%d"), title=f[1], description=f[2],link=f[3]
    )
output = output + u"</ul>"
f = codecs.open(PATH + 'index.html', 'r', 'utf-8')
text = f.read().replace('{news}',output)
f.close()
f = codecs.open(PATH + 'index.html', 'w', 'utf-8')
f.write(text)
f.close()
