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
 * An extension of the Choice class, designed to send events to the ComboBox.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.2 $
 */
public class PopupChoice
  extends Choice
{
  //~ Instance fields --------------------------------------------------------

  private final ComboBox reference;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new PopupChoice object.
   *
   * @param reference ComboBox which uses this PopupChoice.
   */
  public PopupChoice(ComboBox reference)
  {
    this.reference = reference;
    enableEvents(
      AWTEvent.ITEM_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK
      | AWTEvent.FOCUS_EVENT_MASK);

//      enableEvents(-1L);
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Returns the maximum size for this box.
   *
   * @return maximum size.
   */
  public Dimension getMaximumSize()
  {
    return adjustSize(
      super.getMaximumSize(),
      reference.getButton().getMaximumSize(),
      reference.getEditor().getMaximumSize());
  }

  /**
   * Returns the minimum size for this box.
   *
   * @return minimum size.
   */
  public Dimension getMinimumSize()
  {
    return adjustSize(
      super.getMinimumSize(),
      reference.getButton().getMinimumSize(),
      reference.getEditor().getMinimumSize());
  }

  /**
   * Returns the preferred size for this box.
   *
   * @return preferred size.
   */
  public Dimension getPreferredSize()
  {
    return adjustSize(
      super.getPreferredSize(),
      reference.getButton().getPreferredSize(),
      reference.getEditor().getPreferredSize());
  }

  /**
   * Paint this component.
   *
   * @param g Graphics device to draw to.
   */
  public void paint(Graphics g)
  {
    super.paint(g);
    paintArea(
      g,
      getSize());
  }

  /**
   * Update this component.
   *
   * @param g Graphics device to draw to.
   */
  public void update(Graphics g)
  {
    super.update(g);
    paintArea(
      g,
      getSize());
  }

  /**
   * Called to process an event.  If the source of an ItemEvent is this
   * component, the source will be remapped to ComboBox.
   *
   * @param event to process.
   */
  protected void processEvent(AWTEvent event)
  {
    if((event.getSource() == this) && (event instanceof ItemEvent))
    {
      event =
        new ItemEvent(
          reference,
          event.getID(),
          ((ItemEvent)event).getItem(),
          ((ItemEvent)event).getStateChange());
    }

    super.processEvent(event);
  }

  /**
   * Called to process a focus event.  If focus has been lost, then close the
   * selection menu.
   *
   * @param event to process.
   */
  protected void processFocusEvent(final FocusEvent event)
  {
    if(event.getID() == FocusEvent.FOCUS_LOST)
    {
      reference.setIsPopupVisible(false);
      reference.showEditor();

      if(!reference.isEditable())
      {
        reference.showChoice();
      }
    }

    super.processFocusEvent(event);
  }

  /**
   * Called to process an event.  If the source of an ItemEvent is this
   * component, the source will be remapped to ComboBox.
   *
   * @param event to process.
   */
  protected void processItemEvent(ItemEvent event)
  {
    if(event.getSource() == this)
    {
      event =
        new ItemEvent(
          reference,
          event.getID(),
          event.getItem(),
          event.getStateChange());
    }

    reference.setIsPopupVisible(false);
    reference.showEditor();

    if(!reference.isEditable())
    {
      reference.showChoice();
    }

    final Object item = event.getItem();

    if(event.getStateChange() == ItemEvent.SELECTED)
    {
      reference.setSelectedItem(reference.getItemMap().get(item));
    }
    else
    {
      reference.processEvent(reference.replaceItemEvent(event));
    }

    super.processItemEvent(event);
  }

  /**
   * Called to process a mouse event.  If the mouse has been clicked or
   * released, then close the popup selection menu.
   *
   * @param event to process.
   */
  protected void processMouseEvent(final MouseEvent event)
  {
    switch(event.getID())
    {
      case MouseEvent.MOUSE_RELEASED :
      {
        if(event.isPopupTrigger())
        {
          reference.setIsPopupVisible(true);

          break;
        }

        // fall through
      }
      case MouseEvent.MOUSE_CLICKED :
      {
        reference.setIsPopupVisible(false);
        reference.showEditor();

        if(!reference.isEditable())
        {
          reference.showChoice();
        }
        else
        {
          reference.getEditor().requestFocus();
        }

        break;
      }
    }

    super.processMouseEvent(event);
  }

  // Called to compute preferred, minimum, and maximum sizes.
  private Dimension adjustSize(
    final Dimension size,
    final Dimension buttonSize,
    final Dimension textFieldSize)
  {
    final int minWidth  = buttonSize.width + textFieldSize.width;
    final int minHeight = Math.max(buttonSize.height, textFieldSize.height);

    if(minWidth > size.width)
    {
      size.width = minWidth;
    }

    if(minHeight > size.height)
    {
      size.height = minHeight;
    }

    return size;
  }

  // Called to paint this component.  The normal Choice box will be
  // drawn over with something that looks more like the Editor card.
  private void paintArea(
    Graphics  g,
    Dimension size)
  {
    final int w       = size.width;
    final int h       = size.height;
    final int buttonW = reference.getButton().getPreferredSize().width;
    final int buttonH = reference.getButton().getPreferredSize().height;
    g.clearRect(w - buttonW, 0, buttonW, h);
    reference.getButton().update(
      g.create(w - buttonW, (h - buttonH) / 2, buttonW, buttonH));
    g.clearRect(0, 0, w - buttonW, h);
    g.drawRect(0, 0, w - 1, h - 1);

    FontMetrics  metrics    = getFontMetrics(getFont());
    final int    height     = metrics.getHeight();
    final byte[] labelBytes = reference.getEditor().getText().getBytes();
    final Font   f          = g.getFont();
    g.setFont(getFont());
    g.drawBytes(labelBytes, 0, labelBytes.length, 2, height);
    g.setFont(f);
  }
}
