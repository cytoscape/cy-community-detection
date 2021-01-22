.. _tally-attributes-on-hierarchy:

Tally Attributes on Hierarchy
===============================

**Tally Attributes on Hierarchy** provides a way to annotate the hierarchy network created
when running a Community Detection algorithm from this App. 

More specifically, this menu option provides a way to count the number of members 
in each hierarchy cluster node that have a 
**true** or positive value for a specified set of attributes/columns in 
the parent network.

These counts are stored as new columns/attributes on the 
hierarchy network with the same name as seen in the parent network, but prefixed with 
**CommunityDetectionTally** namespace.

In addition, any members in the hierarchy 
cluster that do **NOT** match any of the specified set of attributes/columns are 
counted in the **Unmatched** column/attribute.  

To invoke select a hierarchy network created by CDAPS in Cytoscape and click on 
**Apps -> Community Detection -> Tally Attributes on Hierarchy** menu option.

.. warning::

      For attribute(s)/column(s) of type **Double**, the value is rounded to nearest integer before checking to see if the value is positive