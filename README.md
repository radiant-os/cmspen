CM S Pen Add-on
======

Add-on to detect various S Pen related events on CM/AOSP/AOKP based ROMs, updated to support Android 11.

We decided to open the source of our fork because this is a really useful app which is also built into Radiant.

## Broadcasts

This app works using Broadcasts.

When the S Pen is detached/inserted a Broadcast is sent out with action "com.samsung.pen.INSERT" with a boolean extra "penInsert" which is true when the S Pen is inserted and false when it is detached.

When the S Pen button is pressed a Broadcast is sent out with action "com.tushar.cm_spen.SPEN_EVENT" with an integer extra "EVENT_CODE" which has following values for the various events:

  1 - S Pen is touching screen and Button is pressed
  
  2 - S Pen is touching screen and Button is long-pressed
  
  3 - S Pen is hovering over the screen and button is pressed
  
  4 - S Pen is hovering over the screen and button is long-pressed
  
  5 - S Pen is hovering over the screen and button is pressed twice in quick succession
