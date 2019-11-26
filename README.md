[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/
[make]: https://www.gnu.org/software/make
[cytoscape]: https://cytoscape.org/

Community Detection App for Cytoscape
======================================

[![Build Status](https://travis-ci.org/idekerlab/cy-community-detection.svg?branch=master)](https://travis-ci.org/idekerlab/cy-community-detection) [![Coverage Status](https://coveralls.io/repos/github/idekerlab/cy-community-detection/badge.svg?branch=master)](https://coveralls.io/github/idekerlab/cy-community-detection?branch=master)


**Publication**

TODO ADD

Requirements to use
=====================

* [Cytoscape][cytoscape] 3.7 or above
* Internet connection to allow App to connect to remote services



Installation
==============

TODO



Requirements to build (for developers)
========================================

* [Java][java] 8+ with jdk
* [Maven][maven] 3.4 or above


Building manually
====================

Commands below assume [Git][git] command line tools have been installed

```Bash
# Can also just download repo and unzip it
git clone https://github.com/idekerlab/cy-community-detection

cd cy-community-detection
mvn clean test install
```

The above command will create a jar file under **target/** named
**cy-community-detection-\<VERSION\>.jar** that can be installed
into [Cytoscape][cytoscape]

COPYRIGHT AND LICENSE
=======================

[Click here](LICENSE)

Acknowledgements
=================

* TODO denote funding sources