.. _columns:


Columns
=======

This page describes the columns created by CDAPS in the network and
node tables.

Network Columns
---------------

* Columns created in network table when invoking **Run Community Detection**

 .. image:: images/columns/networkcolumns.png
   :class: with-border with-shadow

 * ``name`` - String in format:

   .. code-block::

     <ALGORITHM NAME>_(<WEIGHT COLUMN USED OR no column used>)_<NAME OF PARENT NETWORK>

   Example:

   .. code-block::

     infomap_(none)_HIV_human PPI

 * ``__CD_OriginalNetwork`` - SUID of parent network. This value can
   change upon session save/reload. If CDAPS is unable to find the
   parent network a dialog will be displayed to the user letting
   them select the correct parent network.

	.. note::

	   Setting the value of ``__CD_OriginalNetwork`` network attribute to a negative value will display the Parent network dialog chooser the next time :ref:`View Interactions <view-interactions>` is invoked

 * ``description`` - Contains string in this format:

   .. code-block::

     Original network: <NAME OF PARENT NETWORK>
     Algorithm used for community detection: <INTERNAL NAME OF COMMUNITY DETECTION ALGORITHM>
     Edge table column used as weight: <WEIGHT COLUMN USED OR (none)>
     CustomParameters: {<CUSTOM PARAMETERS SET BY CALLER>}

   Example:

   .. code-block::

     Original network: HIV-human PPI
     Algorithm used for community detection: infomap
     Edge table column used as weight: (none)
     CustomParameters: {--markovtime=0.75}

 * ``prov:wasDerivedFrom`` - Name of parent network

 * ``prov:wasGeneratedBy`` - Denotes version of CDAPS and algorithm used in this format:

   .. code-block::

     App: CyCommunityDetection (<VERSION>) Docker Image: <DOCKER IMAGE USED>

   Example:

   .. code-block::

    App: CyCommunityDetection (1.1) Docker Image: coleslawndex/cdinfomap:0.1.0

Node Columns
------------

In the node table columns created by CDAPS are
prefixed with ``CD_``

* Columns created in node table when invoking **Run Community Detection**

 .. image:: images/columns/cdetectnodecolumns.png
   :class: with-border with-shadow

 * ``CD_MemberList`` - String of space delimited node names representing members of this cluster

 * ``CD_MemberList_Size`` - Size of ``CD_MemberList``

 * ``CD_MemberList_LogSize`` - Log of ``CD_MemberList_Size``

 * ``CD_CommunityName`` - Name of community set by invocation of **Run Functional Enrichment**

 * ``CD_AnnotatedMembers`` - String of space delimited node names used to set value in ``CD_CommunityName``

 * ``CD_AnnotatedMembers_Size`` - Size of ``CD_AnnotatedMembers``

 * ``CD_AnnotatedMembers_Overlap`` - ``CD_AnnotatedMembers_Size`` divided by ``CD_MemberList_Size``

 * ``CD_AnnotatedMembers_Pvalue`` - Pvalue obtained from term mapping algorithm invoked
   by **Run Functional Enrichment**

 * ``CD_Labeled`` - Boolean denoting if ``CD_CommunityName`` was set
   to a value other then blank or `(none)`

* Columns created in node table when invoking **Run Functional Enrichment**

 .. image:: images/columns/enrichnodecolumns.png
   :class: with-border with-shadow

 * ``CD_AnnotatedAlgorithm`` - Algorithm used to set value in ``CD_CommunityName`` in format:

   .. code-block::

     Annotated by <ALGORITHM NAME> [Docker: <DOCKER IMAGE>] {<CUSTOM PARAMETERS>} via CyCommunityDetection Cytoscape App (<VERSION>)

   Example:

   .. code-block::

     Annotated by gProfiler [Docker: coleslawndex/cdgprofilergenestoterm:0.3.0] {{--maxpval=0.00001, --minoverlap=0.05, --maxgenelistsize=5000}} via CyCommunityDetection Cytoscape App (1.1)

 * ``CD_NonAnnotatedMembers`` - String of space delimited node names **NOT** used by algorithm to set mapped term in ``CD_CommunityName``

 * ``CD_AnnotatedMembers_SourceDB`` Source database used by algorithm to set mapped term in ``CD_CommunityName``

 * ``CD_AnnotatedMembers_SourceTerm`` Id of mapped term set in ``CD_CommunityName``

* Columns created in node table when invoking **Tally Attributes on Hierarchy**

 .. image:: images/columns/resultingtallycols.png
   :class: with-border with-shadow

 * ``CommunityDetectionTally::<TALLY SELECTED COLUMNS>`` - Each column selected in the tally will have an entry and the value is the count of members that did have a positive or **true** value in the column on the parent network

 * ``CommunityDetectionTally::Unmatched`` - Count of members in hierarchy cluster node that did **NOT** have a positive or **true** value in any of the tally columns

 .. note::

    **CommunityDetectionTally** is the namespace prefixed onto columns created when invoking **Tally Attributes on Hierarchy**

 For more information click: :ref:`Tally Attributes on Hierarchy <tally-attributes-on-hierarchy>`