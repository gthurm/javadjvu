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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * The MouseListener and MouseMotionListener used for panning.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.4 $
 */
class PanMode
  implements MouseListener, MouseMotionListener
{
  //~ Instance fields --------------------------------------------------------

  /** The DjVuBean being panned. */
  protected final DjVuBean djvuBean;

  /** The point where the mouse was last located. */
  protected Point start = null;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new PanMode object.
   *
   * @param djvuBean to pan.
   */
  public PanMode(final DjVuBean djvuBean)
  {
    this.djvuBean = djvuBean;
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Indicates the user has clicked the mouse.  Does nothing.
   *
   * @param e the mouse event.
   */
  public void mouseClicked(MouseEvent e) {djvuBean.requestFocus();}

  /**
   * Pan the image to the new mouse location.
   *
   * @param e the mouse event.
   */
  public void mouseDragged(MouseEvent e)
  {
    if(start != null)
    {
      final Point origin = djvuBean.getScrollPosition();
      final Point point = new Point(e.getX() - origin.x, e.getY() - origin.y);

      if(!start.equals(point))
      {
        djvuBean.setScrollPosition(
          origin.x + (start.x - point.x),
          origin.y + (start.y - point.y));
        start = point;
      }
    }
  }

  /**
   * Indicates the mouse has entered the viewing area.  Does nothing.
   *
   * @param e the mouse event.
   */
  public void mouseEntered(MouseEvent e) {}

  /**
   * Indicates the mouse has exited the viewing area.  Does nothing.
   *
   * @param e the mouse event.
   */
  public void mouseExited(MouseEvent e) {}

  /**
   * Indcites the mouse has moved.  Does nothing.
   *
   * @param e the mouse event.
   */
  public void mouseMoved(MouseEvent e) {}

  /**
   * Indicates the mouse button was pressed.  Initiates panning.
   *
   * @param e the mouse event.
   */
  public void mousePressed(MouseEvent e)
  {
    try
    {
      djvuBean.requestFocus();
      Point origin = djvuBean.getScrollPosition();
      start = new Point(e.getX() - origin.x, e.getY() - origin.y);
      djvuBean.addMouseMotionListener(this);
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }

  /**
   * Indicates the mouse button was released.  Stop panning.
   *
   * @param e the mouse event.
   */
  public void mouseReleased(MouseEvent e)
  {
    try
    {
      start = null;
      djvuBean.removeMouseMotionListener(this);
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }
}
