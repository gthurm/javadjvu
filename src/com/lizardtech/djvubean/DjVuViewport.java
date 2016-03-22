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

import com.lizardtech.djvu.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;

//import java.awt.geom.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;


/**
 * This extension to DjVuBean allows a DjVuPage to be viewed without the need
 * for a ScrollPane or JScrollPane.  The main this class should be used is
 * to avoid the annoying flicker caused by ScrollPane repainting the screen
 * when JScrollPane is not available.  This panel will not render the
 * scrollbars.  Instead the scrollbars should be rendered in a parent
 * component.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.9 $
 */
public class DjVuViewport
  extends DjVuBean
  implements AdjustmentListener
{
  //~ Instance fields --------------------------------------------------------

  private Point scrollPosition = new Point();

  // The horizontal scroll bar.
  private Scrollbar hScroll = null;

  // The vertical scroll bar.
  private Scrollbar vScroll = null;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new DjVuViewport object.
   */
  public DjVuViewport() {}

  //~ Methods ----------------------------------------------------------------

  /**
   * Adjust the image scale.
   *
   * @param width to scale the image to.
   * @param height to scale the image to.
   */
  public void setImageSize(
    int width,
    int height)
  {
    DjVuImage       image     = getImageWait();
    final Dimension imageSize = image.getSize();
    final int       owidth    = imageSize.width;
    final int       oheight   = imageSize.height;

    if((width != owidth) || (height != oheight))
    {
      zoom    = 0;
      image   = image.getScaledInstance(width, height);

      if(image != null)
      {
        width    = image.getBounds().width;
        height   = image.getBounds().height;
        setImage(image);
      }

      scaleScrollPosition(owidth, oheight, width, height);
      recursiveRevalidate();
    }
  }

  /**
   * Query the maximum panel size.
   *
   * @return maximum panel size.
   */
  public java.awt.Dimension getMaximumSize()
  {
    return getMaximumPanelSize();
  }

  /**
   * Query the minimum panel size.
   *
   * @return minimum panel size.
   */
  public java.awt.Dimension getMinimumSize()
  {
    return getMinimumPanelSize();
  }

  /**
   * Query the preferred panel size.
   *
   * @return preferred panel size.
   */
  public java.awt.Dimension getPreferredSize()
  {
    return getMinimumPanelSize();
  }

  /**
   * Set the scroll position.
   *
   * @param x coordinate of upper left corner.
   * @param y coordinate of upper left corner.
   */
  public void setScrollPosition(
    int x,
    int y)
  {
    final DjVuImage image=getImage();
    if((image != null)&&((x != scrollPosition.x) || (y != scrollPosition.y)))
    {
      final Dimension imageSize    = image.getSize();
      final Dimension viewportSize = getViewportSize();
      x   = Math.max(
          0,
          Math.min(imageSize.width - viewportSize.width, x));
      y = Math.max(
          0,
          Math.min(imageSize.height - viewportSize.height, y));

      if((x != scrollPosition.x) || (y != scrollPosition.y))
      {
        scrollPosition.setLocation(x, y);
        updateScrollbars();
        repaint(50L);
      }
    }
  }

  /**
   * Query the current scroll position.
   *
   * @return location of upper left corner.
   */
  public java.awt.Point getScrollPosition()
  {
    return scrollPosition;
  }

  /**
   * Set either the horizontal or vertical scrollbar.
   *
   * @param orientation either Scrollbar.HORIZONTAL or Scrollbar.VERTICAL
   * @param scrollbar component.
   */
  public void setScrollbar(
    int       orientation,
    Scrollbar scrollbar)
  {
    if(orientation == Scrollbar.HORIZONTAL)
    {
      hScroll = scrollbar;
    }
    else
    {
      vScroll = scrollbar;
    }

    scrollbar.addAdjustmentListener(this);
    updateScrollbars();
  }

  /**
   * Query the a scrollbar component.
   *
   * @param orientation either Scrollbar.HORIZONTAL or Scrollbar.VERTICAL
   *
   * @return scrollbar component.
   */
  public Scrollbar getScrollbar(int orientation)
  {
    return (orientation == Scrollbar.HORIZONTAL)
    ? hScroll
    : vScroll;
  }

  /**
   * Set the size of this panel.
   *
   * @param width of this panel.
   * @param height of this panel.
   */
  public void setSize(
    int width,
    int height)
  {
    setPanelSize(width, height);
  }

  /**
   * Called to set the URL to browse, and reset the scrollbars.
   *
   * @param url indicates document to view.
   *
   * @throws IOException if an error occures
   */
  public void setURL(final URL url)
    throws IOException
  {
    final URL old = getURL();
    super.setURL(url);

    if(old == null)
    {
//bcr      getImageWait().setBufferMode(DjVuImage.COMPLETE_BUFFER);
      setScrollbar(
        Scrollbar.HORIZONTAL,
        new Scrollbar(Scrollbar.HORIZONTAL));
      setScrollbar(
        Scrollbar.VERTICAL,
        new Scrollbar(Scrollbar.VERTICAL));
      hScroll.setUnitIncrement(16);
      vScroll.setUnitIncrement(16);
    }
  }

  /**
   * Set the size of this panel.
   *
   * @param width of this panel.
   * @param height of this panel.
   */
  public void setViewportSize(
    int width,
    int height)
  {
    setSize(width, height);
  }

  /**
   * Query the size of this panel.
   *
   * @return the panel size.
   */
  public java.awt.Dimension getViewportSize()
  {
    return getSize();
  }

  /**
   * Called to process scrollbar events.
   *
   * @param event to process.
   */
  public void adjustmentValueChanged(AdjustmentEvent event)
  {
    try
    {
      final Scrollbar hScroll        = getScrollbar(Scrollbar.HORIZONTAL);
      final Scrollbar vScroll        = getScrollbar(Scrollbar.VERTICAL);
      final Point     scrollPosition = getScrollPosition();
      setScrollPosition(
        (hScroll != null)
        ? hScroll.getValue()
        : scrollPosition.x,
        (vScroll != null)
        ? vScroll.getValue()
        : scrollPosition.y);
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }

  /**
   * Called to redraw the panel.
   *
   * @param g Graphics device to draw the panel to.
   */
  public void paint(Graphics g)
  {
    super.paint(g);
    updateScrollbars();
  }

  /**
   * Called to draw the panel.
   *
   * @param g Graphics device to draw the panel to.
   */
  public void paintComponent(Graphics g)
  {
    final Point scrollPosition = getScrollPosition();
    g.translate(-scrollPosition.x, -scrollPosition.y);

    try
    {
      super.paintComponent(g);
    }
    finally
    {
      g.translate(scrollPosition.x, scrollPosition.y);
    }
  }

  /**
   * Called to process events.  Mouse events will be rewritten to indicate
   * the position in the document clicked, instead of the position of the
   * panel.
   *
   * @param event to process.
   */
  protected void processEvent(AWTEvent event)
  {
    try
    {
      if(event instanceof MouseEvent)
      {
        final Point scrollPosition = getScrollPosition();

        if(scrollPosition != null)
        {
          final MouseEvent mouseEvent = (MouseEvent)event;
          event =
            new MouseEvent(
              (Component)mouseEvent.getSource(),
              mouseEvent.getID(),
              mouseEvent.getWhen(),
              mouseEvent.getModifiers(),
              mouseEvent.getX() + scrollPosition.x,
              mouseEvent.getY() + scrollPosition.y,
              mouseEvent.getClickCount(),
              mouseEvent.isPopupTrigger());
        }
      }
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }

    super.processEvent(event);
  }

  public void run()
  {
      super.run();
      updateScrollbars();
  }
  
  // Called to update the scrollbar properties.
  private void updateScrollbars()
  {
    final DjVuImage image = getImage();
    if(image != null)
    {
      final Dimension viewportSize   = getViewportSize();
      final Dimension imageSize      = image.getSize();
      final Point     scrollPosition = getScrollPosition();
      final Scrollbar hScroll        = getScrollbar(Scrollbar.HORIZONTAL);
      final Scrollbar vScroll        = getScrollbar(Scrollbar.VERTICAL);

      if(hScroll != null)
      {
        hScroll.setValues(
          scrollPosition.x,
          viewportSize.width,
          0,
          imageSize.width);
      }

      if(vScroll != null)
      {
        vScroll.setValues(
          scrollPosition.y,
          viewportSize.height,
          0,
          imageSize.height);
      }
    }
  }
}
