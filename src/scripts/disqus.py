#!/usr/bin/python
from libs import *

for f in get_files('blog/'):
    disqus = u"""<div id="disqus_thread"></div>
    <script type="text/javascript">
        /* * * CONFIGURATION VARIABLES: EDIT BEFORE PASTING INTO YOUR WEBPAGE * * */
        var disqus_shortname = 'koteknet'; // required: replace example with your forum shortname
        var disqus_url = '%s';

        /* * * DON'T EDIT BELOW THIS LINE * * */
        (function() {
            var dsq = document.createElement('script'); dsq.type = 'text/javascript'; dsq.async = true;
            dsq.src = 'http://' + disqus_shortname + '.disqus.com/embed.js';
            (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
        })();
    </script>
    <noscript>Please enable JavaScript to view the <a href="http://disqus.com/?ref_noscript">comments powered by Disqus.</a></noscript>
    <a href="http://disqus.com" class="dsq-brlink">blog comments powered by <span class="logo-disqus">Disqus</span></a>
    </div> <!-- /container -->
    """ % ("http://kotek.net/" + f[:-5])
    fp = codecs.open(PATH + f, 'r', 'utf-8')
    text = fp.read().replace('</div><!-- /container -->',disqus)
    fp.close()
    fp = codecs.open(PATH + f, 'w', 'utf-8')
    fp.write(text)
    fp.close()

