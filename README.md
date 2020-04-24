[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/
[make]: https://www.gnu.org/software/make
[cytoscape]: https://cytoscape.org/
[cdapsreadthedocs]: https://cdaps.readthedocs.io/en/latest/
[directappinstall]: http://manual.cytoscape.org/en/stable/App_Manager.html#installing-apps
[cd]: https://en.wikipedia.org/wiki/Hierarchical_clustering_of_networks
[appstore]: http://apps.cytoscape.org/apps/cycommunitydetection
[directcytoscapeinstall]: https://cdaps.readthedocs.io/en/latest/Installation.html
[cdservice]: https://github.com/cytoscape/communitydetection-rest-server

Community Detection App for Cytoscape
=======================================

[![Build Status](https://travis-ci.com/cytoscape/cy-community-detection.svg?branch=master)](https://travis-ci.com/cytoscape/cy-community-detection) [![Coverage Status](https://coveralls.io/repos/github/cytoscape/cy-community-detection/badge.svg?branch=master)](https://coveralls.io/github/cytoscape/cy-community-detection?branch=master)
[![Documentation Status](https://readthedocs.org/projects/cdaps/badge/?version=latest&token=d51549910b0a9d03167cce98f0f550cbacc48ec26e849a72a75a36c1cb474847)](https://cdaps.readthedocs.io/en/latest/?badge=latest)


Community Detection App is a Cytoscape App that leverages third party algorithms (via [REST service][cdservice])
to perform [hierarchical clustering/community detection][cd] on a given network. Leveraging
the [REST service][cdservice] allows incorporation of algorithms not easily portable/distributable
with this App. In addition, this tool offers for biologists Term Mapping/Enrichment (also via [service][cdservice]) on the
hierarchies generated by this App.

**NOTE:** This service is experimental. The interface is subject to change.

**Publication**

TODO ADD

Requirements to use
=====================

* [Cytoscape][cytoscape] 3.7 or above
* Internet connection to allow App to connect to remote services



Installation via from Cytoscape
======================================

CDAPS is in the [Cytoscape App Store][appstore]
and can be installed by following these [instructions][directcytoscapeinstall]


Requirements to build (for developers)
========================================

* [Java][java] 8+ with jdk
* [Maven][maven] 3.4 or above

To build documentation

* Make
* Python 3+
* Sphinx (install via `pip install sphinx`)
* Sphinx rtd theme (install via `pip install sphinx_rtd_theme`)


Building manually
====================

Commands below assume [Git][git] command line tools have been installed

```Bash
# Can also just download repo and unzip it
git clone https://github.com/cytoscape/cy-community-detection

cd cy-community-detection
mvn clean test install
```

The above command will create a jar file under **target/** named
**cy-community-detection-\<VERSION\>.jar** that can be installed
into [Cytoscape][cytoscape]


Open Cytoscape and follow instructions [here][directappinstall] and click on
**Install from File...** button to load the jar created above.


Building documentation
=========================

Documentation is stored under `docs/` directory and
uses Sphinx & Python to generate documentation that
is auto uploaded from **master** branch to [Read the Docs][cdapsreadthedocs]

```Bash
# The clone and directory change can be
# omitted if done above
git clone https://github.com/cytoscape/cy-community-detection

cd cy-community-detection
make docs
```
Once `make docs` is run the documentation should automatically
be displayed in default browser, but if not open `docs/_build/html/index.html` in
a web browser
 
COPYRIGHT AND LICENSE
========================

[Click here](LICENSE)

Acknowledgements
=================

* TODO denote funding sources
