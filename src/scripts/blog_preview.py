#!/usr/bin/python
from libs import *
import sys

data = [(get_date(f).strftime("%Y-%m-%d") , get_header(f), get_preview(f,max_words=int(sys.argv[1])), f) for f in get_files("blog/") if get_header(f)]
data.sort()
lines = "<hr>".join([
        """
    <h2><a href="{url}">{header}</a></h2>
    <p class="perex"><b>{date}</b> | {preview}</p>
<<<<<<< HEAD
        """.format(date=d[0], header=d[1], preview=d[2], url=d[3])
        for d in data])
=======
        """.format(date=d, header=get_header(f),
            preview=get_preview(f,max_words=int(sys.argv[1])),
            url=f.split('/')[-1])
        for d,f in data])
>>>>>>> 926ce3e... Appending disqus from kotek.net/blog
f = codecs.open(PATH + 'blog/index.html', 'r', 'utf-8')
text = f.read().replace('{lines}',lines)
f.close()
f = codecs.open(PATH + 'blog/index.html', 'w', 'utf-8')
f.write(text)
f.close()
