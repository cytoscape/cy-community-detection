.. _faq:


Frequently Asked Questions
============================


How to set alternate parent network
--------------------------------------------------

#. To set an alternate parent network for a hierarchy, change the 
   value of the network attribute ``__CD_OriginalNetwork``
   to a negative number such as ``-1`` as seen here:

   .. image:: images/faq/cd_originalnetwork.png
      :class: with-border with-shadow

#. Load the alternate parent network into Cytoscape. Then select a node in the hierarchy 
   and invoke :ref:`View Interactions <view-interactions>` to, if needed, display 
   the Parent network dialog as seen below:

   .. image:: images/faq/parentdialogchooser.png
      :class: with-border with-shadow


   .. note::
		Parent network dialog will only be displayed if there is more 
		then one potential parent network

How to make a network look like a hierarchy network
------------------------------------------------------

To make hierarchy network from scratch, do the following:

#. Create/load a network and add nodes and edges to represent the
   hierarchy

#. Add ``__CD_OriginalNetwork`` network attribute column with type set to ``Long Integer``
   and the value set to ``-1``. This column tells this tool the network is a hierarchy.

#. Add ``CD_MemberList`` node column to each node in network with type ``String`` and with
   the value set to a list of space delimited node names that correspond to ``name`` column 
   values from the parent network. This column is needed when doing 
   :ref:`View Interactions <view-interactions>`

#. Add ``CD_MemberList_Size`` node to each node in hierarchy with type ``Integer`` and with
   the value set to the number of values in ``CD_MemberList`` node column. This column is
   needed when doing :ref:`Term Mapping <perform-term-mapping>`

   .. image:: images/faq/fake_hierarchy.png
      :class: with-border with-shadow

#. If not already done, load the parent network into Cytoscape. In screenshot above,
   *HIV-human PPI* network has been loaded


.. note::
	To get the same look and feel, copy the `style <https://manual.cytoscape.org/en/stable/Styles.html#introduction-to-the-style-interface>`__ 
	from a hierarchy already run by this tool onto the hierarchy made from scratch

