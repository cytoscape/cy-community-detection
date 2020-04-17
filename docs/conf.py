# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# http://www.sphinx-doc.org/en/master/config

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
# sys.path.insert(0, os.path.abspath('.'))

from xml.etree import ElementTree
import sphinx_rtd_theme


# -- Project information -----------------------------------------------------

project = 'Community Detection APplication and Service'
copyright = '2020, Song Cao, Christopher Churas, Akshat Singhal, Fan Zheng'
author = 'Song Cao, Christopher Churas, Akshat Singhal, Fan Zheng'

# The version info for the project you're documenting, acts as replacement for
# |version| and |release|, also used in various other places throughout the
# built documents.
#
with open('../pom.xml', 'rt') as f:
    tree = ElementTree.parse(f)

pom_ns = {'xmlns': 'http://maven.apache.org/POM/4.0.0'}

# The full version, including alpha/beta/rc tags.
release = tree.getroot().findall('.//xmlns:version', namespaces=pom_ns)[0].text

# The short X.Y version.
version = release[0:release.rindex('.')]

# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx_rtd_theme'
]

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']


# The master toctree document.
master_doc = 'index'

# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = "sphinx_rtd_theme"
html_theme_path = [sphinx_rtd_theme.get_html_theme_path()]


# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']
