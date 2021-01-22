.. _whats-new:


What's New
==========

Version 1.12.0
---------------------------

* Added new menu option `Apps -> Community Detection -> Tally Attributes in Hierarchy`
  that annotates the hierarchy network by adding columns using values tallied
  from a set of attributes/columns in the parent network.

* Order of algorithms displayed in **Run Community Detection** and 
  **Run Functional Enrichment** dialogs are now consistent with what is returned
  from CDAPS REST server. UD-1573

Version 1.11.0
---------------------------

* Added support for new edge list format COMMUNITYDETECTRESULTV2 allowing
  Community Detection algorithms to add additional custom node columns. UD-1091

Bug fixes

* Fixed bug where `View Interactions for Selected Node` no longer
  worked if a session was reloaded. UD-1087

* Fixed bug where any values set in the community detection or
  functional enrichment dialogs were lost when when reopening
  those dialogs. `Issue #3 <https://github.com/cytoscape/cy-community-detection/issues/3>`_

Version 1.10.0
---------------------------

* Added new menu option `Apps -> Community Detection -> Settings`
  that lets caller easily change CDAPS REST server. UD-1066

* Added message letting user know using weight
  column in Community Detection dialog is an advanced
  parameter. UD-988

* In Community Detection dialog replaced **About** button
  with info icon next to algorithm selection dropdown. UD-987

Bug fixes

* Fixed bug where changes to properties under
  `Edit -> Properties -> CyCommunityDetection`
  were not being loaded. UD-986


Version 1.0
------------------------

* First release
