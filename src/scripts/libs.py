import re, os, sys, subprocess, codecs, datetime

PATH = 'target/site/'
PATH_MD = 'src/site/markdown/'

def get_files(path):
    files = []
    for (p, sd, fs) in os.walk(PATH):
        pp = p.replace(PATH, "")
        if len(pp):
            pp = pp+"/"
        for f in fs:
            if f[-5:].lower() == ".html":
                files.append(pp+f)
    files = [f for f in files if "apidocs/" not in f and "doc/" not in f and "blog/index.html" not in f and path in f]
    return files

def get_date(f, format=None):
    date = subprocess.check_output(
        'git log --format=%ai {f} | tail -1'.format(
            f=PATH_MD + f[:-5]+'.md'
        ), shell=True)[:10]
    y,m,d = date.split("-")
    date = datetime.datetime(int(y),int(m),int(d))
    return date
    

def get_header(f):
    try:    
        myfile = codecs.open(PATH+f, "r", "utf-8")
        html=myfile.read()
        myfile.close()
        header = re.findall(r'<[h,H]1[^>]*?>(.*?)</[h,H]1>', html)[0]
        return header
    except IndexError:
        return ""

def get_preview(f, max_words=None):
    myfile = codecs.open(PATH+f, "r", "utf-8")
    html=myfile.read()
    myfile.close()
    preview = re.findall(r'<[p,P][^>]*?>(.*?)</[p,P]>', html)[0]
    preview = re.sub('<[^<]+?>', '', preview)
    if max_words and max_words < len(preview.split(" ")):
        preview = " ".join(preview.split(" ")[:max_words])
    return preview
