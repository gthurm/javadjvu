//C- -------------------------------------------------------------------
//C- Java DjVu (r) (v. 0.8)
//C- Copyright (c) 2004-2005 LizardTech, Inc.  All Rights Reserved.
//C- Java DjVu is protected by U.S. Pat. No.C- 6,058,214 and patents
//C- pending.
//C-
//C- This software is subject to, and may be distributed under, the
//C- GNU General Public License, Version 2. The license should have
//C- accompanied the software or you may obtain a copy of the license
//C- from the Free Software Foundation at http://www.fsf.org .
//C-
//C- The computer code originally released by LizardTech under this
//C- license and unmodified by other parties is deemed "the LIZARDTECH
//C- ORIGINAL CODE."  Subject to any third party intellectual property
//C- claims, LizardTech grants recipient a worldwide, royalty-free,
//C- non-exclusive license to make, use, sell, or otherwise dispose of
//C- the LIZARDTECH ORIGINAL CODE or of programs derived from the
//C- LIZARDTECH ORIGINAL CODE in compliance with the terms of the GNU
//C- General Public License.   This grant only confers the right to
//C- infringe patent claims underlying the LIZARDTECH ORIGINAL CODE to
//C- the extent such infringement is reasonably necessary to enable
//C- recipient to make, have made, practice, sell, or otherwise dispose
//C- of the LIZARDTECH ORIGINAL CODE (or portions thereof) and not to
//C- any greater extent that may be necessary to utilize further
//C- modifications or combinations.
//C-
//C- The LIZARDTECH ORIGINAL CODE is provided "AS IS" WITHOUT WARRANTY
//C- OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//C- TO ANY WARRANTY OF NON-INFRINGEMENT, OR ANY IMPLIED WARRANTY OF
//C- MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
//C-
//C- In addition, as a special exception, LizardTech Inc. gives permission
//C- to link the code of this program with the proprietary Java
//C- implementation provided by Sun (or other vendors as well), and
//C- distribute linked combinations including the two. You must obey the
//C- GNU General Public License in all respects for all of the code used
//C- other than the proprietary Java implementation. If you modify this
//C- file, you may extend this exception to your version of the file, but
//C- you are not obligated to do so. If you do not wish to do so, delete
//C- this exception statement from your version.
//C- -------------------------------------------------------------------
//C- Developed by Bill C. Riemers, Foxtrot Technologies Inc. as work for
//C- hire under US copyright laws.
//C- -------------------------------------------------------------------
//
package com.lizardtech.djvubean.toolbar;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Array;
import java.util.*;


/**
 * This class is designed as an AWT replacement to JComboBox.  The main
 * difference between this class and the Choice class, is editable fields
 * are supported, and better control is given to the rendering of the
 * selection field.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.5 $
 */
public class ComboBox
  extends Container
  implements ItemSelectable
{
  //~ Instance fields --------------------------------------------------------

  /**
   * A CardLayout to swap between the Choice box, and the Text selection box.
   */
  protected final CardLayout cardLayout = new CardLayout();

  /**
   * The container used for the Text selection box, and the selection button.
   */
  protected final Container editContainer = new Panel();

  /** A table of Objects mapped to their selection strings. */
  protected final Hashtable itemMap = new Hashtable();

  /** The last item selected. */
  protected Object lastSelectedItem = null;

  /**
   * An extension of the Choice class, designed to deliver events to the
   * ComboBox.
   */
  protected final PopupChoice choice;

  /** The text selection field, used for editing values. */
  protected final TextField textField = new TextField();

  /** The text indicating the last selected value. */
  protected String customText = null;

  /** The button used to activate/deactivate popup selection menu. */
  protected ToggleButton button;

  // support for ItemListeners
  private final ListenerSupport itemState =
    new ListenerSupport(ItemListener.class);

  // True if this ComboBox is editable.
  private boolean isEditable;

  // True if the Choice selection menu is currently being displayed.
  private boolean isPopupVisible = false;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new ComboBox object.
   *
   * @param tip Tool tip to display with this button.  (For future use.)
   * @param text Text label to display with this button.
   * @param imageArg Image to display with this button.
   * @param defaultSize set the default size of this button.
   */
  ComboBox(
    final String    tip,
    final String    text,
    final Object    imageArg,
    final Dimension defaultSize)
  {
    choice = new PopupChoice(this);
    enableEvents(AWTEvent.ITEM_EVENT_MASK);
    setLayout(cardLayout);
    textField.setColumns(0);
    button = new ToggleButton(tip, text, imageArg, defaultSize);
    button.setBorderType(ToggleButton.SIMPLE_BORDER);
    editContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));

//    editContainer.add(textField);
//    editContainer.add(button);
    final Panel panel = new Panel();
    panel.setLayout(new BorderLayout());
    panel.add(textField, BorderLayout.CENTER);
    panel.add(button, BorderLayout.EAST);
    editContainer.add(panel);
    add("Editor", editContainer);
    add("Choice", choice);
    showChoice();
    button.addActionListener(
      new ActionListener()
      {
        public void actionPerformed(ActionEvent event)
        {
          showPopup();
        }
      });
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Set the background color.
   *
   * @param bgColor the new background color to use.
   */
  public void setBackground(final Color bgColor)
  {
    editContainer.setBackground(bgColor);
    button.setBackground(bgColor);
    choice.setBackground(bgColor);
    super.setBackground(bgColor);
  }

  /**
   * Query the toggle button used for selection.
   *
   * @return the ToggleButton.
   */
  public ToggleButton getButton()
  {
    return button;
  }

  /**
   * Set the editability of this ComboBox
   *
   * @param editable true if this ComboBox should be editable.
   */
  public void setEditable(final boolean editable)
  {
    if(editable != isEditable())
    {
      isEditable = editable;

      if(!editable)
      {
        showChoice();
      }
      else
      {
        if(isPopupVisible())
        {
          hidePopup();
        }

        showEditor();
      }
    }
  }

  /**
   * Test if the ComboBox is editable.
   *
   * @return true if the ComboBox is editable.
   */
  public boolean isEditable()
  {
    return isEditable;
  }

  /**
   * Returns the text editor field.
   *
   * @return the TextField used for editable values.
   */
  public TextField getEditor()
  {
    return textField;
  }

  /**
   * Used to enable this ComboBox.
   *
   * @param enabled true if enabled.
   */
  public void setEnabled(final boolean enabled)
  {
    editContainer.setEnabled(enabled);
    button.setEnabled(enabled);
    choice.setEnabled(enabled);
    textField.setEnabled(enabled);
    super.setEnabled(enabled);
  }

  /**
   * Set the current foreground color.
   *
   * @param fgColor the new foreground color.
   */
  public void setForeground(final Color fgColor)
  {
    editContainer.setForeground(fgColor);
    button.setForeground(fgColor);
    choice.setForeground(fgColor);
    super.setForeground(fgColor);
  }

  /**
   * Set the value of the isPopupVisible.  This should be private, but is
   * public due to linking restrictions with JDK 1.1.8.
   *
   * @param isPopupVisible true if the pop-up is visible.
   */
  public void setIsPopupVisible(boolean isPopupVisible)
  {
    this.isPopupVisible = isPopupVisible;
  }

  /**
   * Query the number of items registered.
   *
   * @return the number of items registered.
   */
  public int getItemCount()
  {
    return choice.getItemCount();
  }

  /**
   * Query all ItemListeners registered with this component.
   *
   * @return An array of Item Listeners.
   */
  public ItemListener[] getItemListeners()
  {
    return (ItemListener[])itemState.getListeners();
  }

  /**
   * Query the Hashtable of items.
   *
   * @return Hashtable of items.
   */
  public Hashtable getItemMap()
  {
    return itemMap;
  }

  /**
   * Returns true if the pop-up might be currently visible.
   *
   * @return false if the pop-up definitely is not visible.
   */
  public boolean isPopupVisible()
  {
    return isPopupVisible;
  }

  /**
   * Set the scale factor used to reduce image sizes.
   *
   * @param value scale factor to use.
   */
  public void setScaleFactor(double value)
  {
    button.setScaleFactor(value);
  }

  /**
   * Query the scale factor used to reduce image sizes.
   *
   * @return scale factor to use.
   */
  public double getScaleFactor()
  {
    return button.getScaleFactor();
  }

//  /**
//   * Query all EventListeners of the specified class registered with this
//   * component.
//   *
//   * @param listenerType Class to query.
//   *
//   * @return an array of EventListener of the specified type.
//   */
//  public EventListener[] getListeners(Class listenerType)
//  {
//    EventListener[] retval = super.getListeners(listenerType);
//
//    if(
//      (listenerType != null)
//      && listenerType.isAssignableFrom(ItemListener.class))
//    {
//      final ItemListener[] retval2 = getItemListeners();
//
//      if((retval2 != null) && (retval2.length > 0))
//      {
//        final int length = (retval != null)
//          ? retval.length
//          : 0;
//
//        if((length == 0) && (listenerType == ItemListener.class))
//        {
//          retval = retval2;
//        }
//        else
//        {
//          final EventListener[] retval3 =
//            (EventListener[])Array.newInstance(
//              listenerType,
//              length + retval2.length);
//          int                   i = 0;
//
//          for(; i < retval2.length; i++)
//          {
//            retval3[i] = retval2[i];
//          }
//
//          for(int j = 0; j < length;)
//          {
//            retval3[i++] = retval[j++];
//          }
//
//          retval = retval3;
//        }
//      }
//    }
//
//    return retval;
//  }

  /**
   * Query the index of the currently selected item.
   *
   * @return the currently selected index.
   */
  public int getSelectedIndex()
  {
    return choice.getSelectedIndex();
  }

  /**
   * Set the specified item to be selected.
   *
   * @param item the item to select.
   */
  public void setSelectedItem(Object item)
  {
    if(item != null)
    {
      final String lastSelectedItemString =
        (lastSelectedItem != null)
        ? lastSelectedItem.toString()
        : null;
      final String itemString = item.toString();

      if(itemString.equals(lastSelectedItemString))
      {
        return;
      }

      if(!itemMap.containsKey(itemString))
      {
        if(!isEditable())
        {
          return;
        }

        choice.add(itemString);
        choice.select(itemString);
        choice.remove(itemString);
      }

      choice.select(itemString);

      if(lastSelectedItem != null)
      {
        processEvent(
          new ItemEvent(
            this,
            ItemEvent.ITEM_STATE_CHANGED,
            lastSelectedItem,
            ItemEvent.DESELECTED));
      }

      lastSelectedItem = item;
      textField.setText(itemString);

      int length = itemString.length();
      textField.setColumns((length > 4)
        ? (length + 1)
        : 4);
      textField.invalidate();
      validate();
      processEvent(
        new ItemEvent(
          this,
          ItemEvent.ITEM_STATE_CHANGED,
          item,
          ItemEvent.SELECTED));
    }
  }

  /**
   * Query selected objects.
   *
   * @return an array of selected objects.
   */
  public Object[] getSelectedObjects()
  {
    Object[] retval = choice.getSelectedObjects();

    for(int i = 0; i < retval.length; i++)
    {
      if(retval[i] instanceof String)
      {
        Object item = itemMap.get(retval[i]);

        if(item != null)
        {
          retval[i] = item;
        }
      }
    }

    return choice.getSelectedObjects();
  }

  /**
   * Add an Item to the selection list.
   *
   * @param item to add.
   */
  public void addItem(Object item)
  {
    if(item != null)
    {
      final String itemString = item.toString();

      if(itemString.equals(customText))
      {
        itemMap.put(itemString, item);
        choice.add(itemString);
        choice.select(itemString);
      }
      else
      {
        itemMap.put(itemString, item);
        choice.add(itemString);
      }

      if(itemString != null)
      {
        textField.setColumns(0);
      }

      if(textField.getText().length() == 0)
      {
        textField.setText(itemString);

        int length = itemString.length();
        textField.setColumns((length > 4)
          ? (length + 1)
          : 4);
      }
    }
  }

  /**
   * Add an ItemListener.
   *
   * @param listener to add.
   */
  public void addItemListener(final ItemListener listener)
  {
    itemState.addListener(listener);
  }

  /**
   * Hide the popup selection menu.
   */
  public void hidePopup()
  {
    if(isPopupVisible)
    {
      showEditor();

      if(!isEditable())
      {
        showChoice();
      }
    }
  }

  /**
   * Called to process an ItemEvent.
   *
   * @param event to process.
   */
  public void processItemEvent(ItemEvent event)
  {
    if(isEnabled())
    {
      itemState.fireItemEvent(event);
    }
  }

  /**
   * Remove an item from the selection list.
   *
   * @param item to remove.
   */
  public void removeItem(Object item)
  {
    if(item != null)
    {
      final String itemString = item.toString();
      itemMap.remove(itemString);
      choice.remove(itemString);
    }
  }

  /**
   * Remove an ItemListener.
   *
   * @param listener to remove.
   */
  public void removeItemListener(final ItemListener listener)
  {
    itemState.removeListener(listener);
  }

  /**
   * Called to replace the selection string with the selection Object in an
   * ItemEvent.
   *
   * @param event the ItemEvent generated by the ItemSelectable
   *
   * @return the replacement ItemEvent
   */
  public ItemEvent replaceItemEvent(ItemEvent event)
  {
    if(event != null)
    {
      final Object oldItem = event.getItem();
      final Object item = itemMap.get(oldItem);

      if((item != null) && (item != oldItem))
      {
        event =
          new ItemEvent(
            (ItemSelectable)event.getSource(),
            event.getID(),
            item,
            event.getStateChange());
      }
    }

    return event;
  }

  // Switch to the Choice card.
  public void showChoice()
  {
    cardLayout.show(this, "Choice");
  }

  // Switch to the Editor card.
  public void showEditor()
  {
    cardLayout.show(this, "Editor");
  }

  /**
   * Show the item selection menu.  This is done by sending a mouse event to
   * the Choice box.
   */
  public void showPopup()
  {
    if(!isPopupVisible())
    {
      showChoice();
      choice.requestFocus();
      choice.dispatchEvent(
        new MouseEvent(
          this,
          MouseEvent.MOUSE_PRESSED,
          System.currentTimeMillis() - 10L,
          InputEvent.BUTTON1_MASK,
          getSize().width - 10,
          10,
          1,
          true));
      choice.dispatchEvent(
        new MouseEvent(
          this,
          MouseEvent.MOUSE_RELEASED,
          System.currentTimeMillis() - 10L,
          InputEvent.BUTTON1_MASK,
          getSize().width - 10,
          10,
          1,
          true));
    }
  }

  /**
   * Called to process an event.
   *
   * @param event to process.
   */
  protected void processEvent(final AWTEvent event)
  {
    if(event instanceof ItemEvent)
    {
      processItemEvent((ItemEvent)event);
    }
    else
    {
      super.processEvent(event);
    }
  }

//  private Vector queueVector=new Vector();
//  private Thread queueThread=null;
//
//  /**
//   * Called to process an event.
//   *
//   * @param event to process.
//   */
//  protected void processEvent(final AWTEvent event)
//  {
//    if(event instanceof ItemEvent)
//    {
//      try
//      {
//        synchronized(queueVector)
//        {
//          queueVector.addElement(event);
//          if(queueThread == null)
//          {
//            queueThread=new Thread(this);
//            queueThread.start();
//          }
//          else
//          {
//            queueVector.notifyAll();
//          }
//        }
//        throw new Exception(((ItemEvent)event).getItem()+","+((ItemEvent)event).getStateChange());
//      }
//      catch(final Throwable ignored) {ignored.printStackTrace(DjVuOptions.err);}
//    }
//    else
//    {
//      super.processEvent(event);
//    }
//  }
//
//  /**
//   * Called to process an events.
//   *
//   * @param event to process.
//   */
//  public void run()
//  {
//    for(int i=0;;)
//    {
//      try
//      {
//        while(true)
//        {
//          ItemEvent event=null;
//          synchronized(queueVector)
//          {
//            event=(ItemEvent)queueVector.elementAt(0);
//            queueVector.removeElementAt(0);
//          }
//          if(event != null)
//          {
//            i=0;
//            try
//            {
//              processItemEvent((ItemEvent)event);
//            }
//            catch(final Throwable ignored) {}
//          }
//        }
//      }
//      catch(final Throwable ignored) {}
//      synchronized(queueVector)
//      {
//        if(queueVector.size() == 0)
//        {
//          try
//          {
//            queueVector.wait(100L);
//          }
//          catch(final Throwable ignored) {}
//          if((queueVector.size() == 0)&&(++i >= 100))
//          {
//            queueThread=null;
//            break;
//          }
//        }
//      }
//      try
//      {
//        Thread.sleep(10L);
//      }
//      catch(final Throwable ignored) {}
//    }
//  }
}
