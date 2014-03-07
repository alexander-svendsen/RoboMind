RoboMind
========
No real effort has been made to optimize program load time so far. However in general with a fully featured JVM and Java system there are additional costs and one of those is the much larger number of classes that end up being loaded. There may be things that can be done to improve this to some degree, but I suspect it will always be slower than on the NXT.
- gloomyandy (link http://www.lejos.org/forum/viewtopic.php?f=18&t=5822&start=15)

epascual wrote:
Thanks for the precision. 

Excuse me if the question sounds stupid, but why has the "old" Java environment been replaced by Oracles's one ? The leJOS class ecosystem was already working quite well with this version.

The "old" Java environment consisted of a self written JVM, that was designed for very confined environment (e.g. 64KB RAM, embedded system without an OS) and the EV3 is basically a Linux system with 64MB, enough to run a proper JVM. Also, if you're using the NXT version of java.lang.String, you'll notice that some stuff is missing, like the split() method. That's simply because in addition to rewriting a JVM from scratch, we also had to write a basic java runtime library from scratch. On the EV3, you can finally use a fully fledged java runtime.
- skoehler