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
package com.lizardtech.djvubean;

import com.lizardtech.djvu.DjVuOptions;
import java.awt.*;
import java.awt.event.*;


/**
 * ZoomMode is ue mouse listener to implement selection zoom on a DjVuBean.
 */
class ZoomMode
  implements MouseListener, MouseMotionListener
{
  //~ Instance fields --------------------------------------------------------

  /** DjVuBean to zoom. */
  protected final DjVuBean djvuBean;

  /** Most recient coordinate. */
  protected final Point last = new Point();

  /** Start point where the user pressed down on the mouse. */
  protected Point start = null;

  /** The area selected. */
  protected final Rectangle select = new Rectangle();

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new ZoomMode object.
   *
   * @param bean DjVuBean object to listen to.
   */
  public ZoomMode(DjVuBean bean)
  {
    djvuBean = bean;
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Called when the mouse is clicked.  Performs no operation.
   *
   * @param event describing mouse action.
   */
  public void mouseClicked(MouseEvent event) {djvuBean.requestFocus();}

  /**
   * Called when the mouse is dragged.  Highlights the rectangle if the start
   * position has been set.
   *
   * @param event describing mouse action.
   */
  public void mouseDragged(MouseEvent event)
  {
    if(start != null)
    {
      int i = event.getX();
      int j = event.getY();

      if((last.x != i) || (last.y != j))
      {
        int k = (i <= start.x)
          ? i
          : start.x;
        int l = (j <= start.y)
          ? j
          : start.y;
        int i1 = (i <= start.x)
          ? (start.x - i)
          : (i - start.x);
        int j1 = (j <= start.y)
          ? (start.y - j)
          : (j - start.y);
        select.setBounds(k, l, i1, j1);
        djvuBean.setSelect(select);
        last.setLocation(i, j);
      }
    }
  }

  /**
   * Called when the mouse pointer enters the component.  Performs no
   * operation.
   *
   * @param event describing mouse action.
   */
  public void mouseEntered(MouseEvent event) {}

  /**
   * Called when the mouse pointer exits the component.  Performs no
   * operation.
   *
   * @param event describing mouse action.
   */
  public void mouseExited(MouseEvent event) {}

  /**
   * Called when the mouse pointer is moved.  Performs no operation.
   *
   * @param event describing mouse action.
   */
  public void mouseMoved(MouseEvent event) {}

  /**
   * Called when the mouse button is pressed.  The start location will be
   * set.
   *
   * @param event describing mouse action.
   */
  public void mousePressed(MouseEvent event)
  {
    try
    {
      djvuBean.requestFocus();
      start = new Point(
          event.getX(),
          event.getY());
      last.setLocation(start);
      select.setBounds(start.x, start.y, 0, 0);
      djvuBean.addMouseMotionListener(this);
      djvuBean.setSelect(select);
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }

  /**
   * Called when the mouse button is released. The selected area will be
   * zoomed to.
   *
   * @param event describing mouse action.
   */
  public void mouseReleased(MouseEvent event)
  {
    try
    {
      start = null;
      djvuBean.removeMouseMotionListener(this);
      djvuBean.zoomSelect();
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }
}
