.. _faq:


Frequently Asked Questions
============================


How to set alternate parent network
--------------------------------------------------

To set an alternate parent network for a CDAPS
hierarchy, change the value of the network attribute ``__CD_OriginalNetwork``
to a negative number such as ``-1`` and invoke :ref:`View Interactions <view-interactions>` 
to display the Parent network dialog chooser.


How to make a network look like a CDAPS hierarchy
---------------------------------------------------

To make CDAPS think a network is a hierarchy, do the following:

* Add ``__CD_OriginalNetwork`` network attribute column with type set to ``Long Integer``
  and the value set to the SUID of the parent network. If the SUID of the parent network
  is not known, set the value to ``-1`` and CDAPS will prompt the user to select a 
  parent network when :ref:`View Interactions <view-interactions>` is invoked

* Add ``CD_MemberList`` node column to each node in hierarchy with type ``String`` and with
  the value set to a list of space delimited node names that correspond to ``name`` column 
  values from the parent network

* Add ``CD_MemberList_Size`` node to each node in hierarcy with type ``Integer`` and with
  the value set to the number of values in ``CD_MemberList`` node column. This column is
  needed when doing :ref:`Term Mapping <perform-term-mapping>`