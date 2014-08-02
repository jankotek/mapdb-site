#!/usr/bin/python

import re, os, subprocess

PATH = './target/site/blog/'
PATH_MD = './src/site/markdown/' #FIXME: add blog folder
def get_file_list():
    files = [ f for f in os.listdir(PATH)
        if os.path.isfile(os.path.join(PATH,f)) and f[-5:] == '.html']
    files.remove("index.html")
    return files

def get_date(f):
    d = subprocess.check_output(
        'git log --format=%ai {f} | tail -1'.format(
            f=PATH_MD + f[:-5]+'.md'
        ), shell=True)
    return "2014-04-23"#d[:10]

def get_header(f):
    with open (PATH+f, "r") as myfile:
        html=myfile.read()
    header = re.findall(r'<[h,H]1[^>]*?>(.*?)</[h,H]1>', html)[0]
    return header

def get_preview(f):
    with open (PATH+f, "r") as myfile:
        html=myfile.read()
    preview = re.findall(r'<[p,P][^>]*?>(.*?)</[p,P]>', html)[0]
    preview = re.sub('<[^<]+?>', '', preview)
    return preview

data = []
for f in get_file_list():
    data.append((get_date(f), get_header(f), get_preview(f), f))
data.sort()    
lines = "<hr>".join([
        """
    <h2><a href="{url}">{header}</a></h2>
    <p class="perex"><b>{date}</b> | {preview}</p>
        """.format(date=d[0], header=d[1], preview=d[2], url=d[3])
        for d in data])
f = open(PATH + 'index.html', 'r')
text = f.read().replace('{lines}',lines)
f.close()
f = open(PATH + 'index.html', 'w')
f.write(text)
f.close()
