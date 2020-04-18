Quick Tutorial
==============

Open a network
--------------

To run Community Detection, a network must be loaded in Cytoscape.

From within Cytoscape click on **Affinity Purification** network
on the starter panel:

.. image:: images/quicktutorial/starterpanel.png
   :class: with-border with-shadow

If **not** displayed, the **Starter Panel** can
be displayed by invoking the menu option **View -> Show Starter Panel**.

   .. image:: images/quicktutorial/loadstarterpanel.png


Run Community Detection
-----------------------

With the network loaded click on **Apps -> Community Detection -> Run Community Detection** menu option.

.. image:: images/quicktutorial/runcommunitydetection.png
   :class: with-border with-shadow

The above step will display a dialog seen below.

Select **Louvain** from algorithm dropdown and
click **Run** button.

.. image:: images/quicktutorial/communitydetectiondialog.png
   :class: with-border with-shadow

A new network/hierarchy should be generated as seen here (The current default layout will be used):

.. image:: images/quicktutorial/resultinghierarchy.png
   :class: with-border with-shadow

Each node in the network/hierarchy above represents a cluster
with the members of that cluster set in the **CD_MemberList** node column

Perform Term Mapping
--------------------

Using network/hierarchy generated above select a few nodes
in the network view and then right click on a selected node to display the submenu
and select **Apps -> Community Detection -> Run Functional Enrichment**
as seen here:

.. image:: images/quicktutorial/term_map_select_nodes.png
   :class: with-border with-shadow

The above step will display a dialog seen below:

Select **gProfiler** from algorithm dropdown and click
**Run** button.

.. image:: images/quicktutorial/term_map_dialog.png
   :class: with-border with-shadow

**gProfiler** will be run and nodes will be named and
colored according to overlap as seen here:

.. image:: images/quicktutorial/mapped_terms.png
   :class: with-border with-shadow

View Interactions
-----------------

Using network/hierarchy generated above select a **single**
node in the network view and then right click on the
selected node to display the submenu and select
**Apps -> Community Detection -> View Interactions for Selected Node**

.. image:: images/quicktutorial/view_interactions_invoke.png
   :class: with-border with-shadow

Invoking this menu option will show all the nodes pertaining to this cluster
in the parent network like as seen here:

.. image:: images/quicktutorial/view_interactions.png
   :class: with-border with-shadow

Send terms in cluster to iQuery
-------------------------------