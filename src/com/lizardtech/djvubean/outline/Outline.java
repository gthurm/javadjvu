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
package com.lizardtech.djvubean.outline;

import com.lizardtech.djvu.*;
import com.lizardtech.djvu.outline.Bookmark;
import com.lizardtech.djvubean.DjVuBean;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;


/**
 * This class is used for outline navigation.  Logically this equivalent to a
 * JTree, but the look and feel is slightly different.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.3 $
 */
public class Outline
  extends Canvas
  implements PropertyChangeListener, java.io.Serializable
{
  //~ Static fields/initializers ---------------------------------------------

  private static final Class[] visibleParms = {Integer.TYPE};

  //~ Instance fields --------------------------------------------------------

  /** The DjVuComponent used when creating this Outline */
  public final DjVuBean  djvuBean;
  private Hashtable      depthMap     = new Hashtable();
  private Hashtable      parentMap    = new Hashtable();
  private Vector         activeVector = new Vector();
  private Vector         pagenoVector = new Vector();
  private final Object[] visibleArgs  = {null};
  private boolean        checked      = false;
  private boolean        firstTime    = true;
  private int            fontHeight   = -1;
  private int            fontWidth    = -1;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new Outline object.
   *
   * @param djvuBean the DjVuBean to navigate.
   *
   * @throws ArrayIndexOutOfBoundsException if the document has less than 2
   *         pages.
   */
  public Outline(final DjVuBean djvuBean)
  {
    this.djvuBean = djvuBean;

    if(djvuBean.getDocument().size() < 2)
    {
      throw new ArrayIndexOutOfBoundsException(
        "Can not navigate documents with only one page.");
    }

    final MouseListener mouseListener =
      new MouseAdapter()
      {
        public void mouseClicked(final MouseEvent e)
        {
          try
          {
            clickLocation(
              e.getX(),
              e.getY());
          }
          catch(final Throwable exp)
          {
            exp.printStackTrace(DjVuOptions.err);
            System.gc();
          }
        }
      };

    addMouseListener(mouseListener);

    final Document document = djvuBean.getDocument();
    final Bookmark bookmark = (Bookmark)document.getBookmark();
    bookmark.setDjVmDir(document.getDjVmDir());
    setFirstBookmark(bookmark);

    final Properties properties = djvuBean.properties;
    properties.put(
      "addOn.NavPane",
      "Outline," + properties.getProperty("addOn.NavPane", "None"));
    djvuBean.addPropertyChangeListener(this);
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Get the specified bookmark.
   *
   * @param item the row number of the bookmark to retrieve.
   *
   * @return the specified bookmark.
   */
  public Bookmark getBookmark(final int item)
  {
    final Vector activeVector = getActiveVector();

    synchronized(activeVector)
    {
      return (Bookmark)((item < activeVector.size())
      ? activeVector.elementAt(item)
      : null);
    }
  }

  /**
   * Set the checked property of the specified bookmark.
   *
   * @param item the row number of the bookmark to check or uncheck.
   * @param checked true if checked.
   */
  public void setCheckedItem(
    final int     item,
    final boolean checked)
  {
    final Vector activeVector = getActiveVector();

    synchronized(activeVector)
    {
      if(item < activeVector.size())
      {
        setCheckedItem(item, checked, activeVector);
      }
    }
  }

  /**
   * Query if the specified bookmark is checked.
   *
   * @param item row number of the bookmark to query.
   *
   * @return true if the specified bookmark is checked.
   */
  public boolean isCheckedItem(final int item)
  {
    boolean      retval       = false;
    final Vector activeVector = getActiveVector();

    synchronized(activeVector)
    {
      if((item + 1) < activeVector.size())
      {
        final Bookmark bookmark = getBookmark(item);

        if(bookmark.size() > 0)
        {
          final Bookmark next  = getBookmark(item + 1);
          final Bookmark child = (Bookmark)bookmark.elementAt(0);
          retval = (next == child);
        }
      }
    }

    return retval;
  }

  /**
   * Called to check any bookmarks linked to the specified page number.
   *
   * @param pageno specifies page number.
   */
  public void setCheckedPage(final int pageno)
  {
    final Vector activeVector = getActiveVector();

    synchronized(activeVector)
    {
      final Vector pagenoVector = getPagenoVector();

      if(pageno < pagenoVector.size())
      {
        final Object bookmark = pagenoVector.elementAt(pageno);

        if(bookmark != null)
        {
          setCheckedBookmark((Bookmark)bookmark, activeVector);
        }
      }
    }
  }

  /**
   * Called to initialize the outline by setting the top level bookmark.
   *
   * @param bookmark top level bookmark.
   */
  public void setFirstBookmark(final Bookmark bookmark)
  {
    final Vector activeVector = getActiveVector();

    synchronized(activeVector)
    {
      final Hashtable parentMap = getParentMap();
      parentMap.clear();

      final Vector pagenoVector = getPagenoVector();
      pagenoVector.setSize(0);
      getDepthMap().clear();
      mapChildren(
        bookmark,
        parentMap,
        pagenoVector,
        new Integer(0));
      activeVector.setSize(0);
      activeVector.addElement(bookmark);
      setCheckedItem(0, true);
    }
  }

  /**
   * Set the font height used in calculating line spacing.
   *
   * @param fontHeight to use to calculate line spacing.
   */
  public void setFontHeight(final int fontHeight)
  {
    this.fontHeight = fontHeight;
  }

  /**
   * Query the font height used in calculating line spacing.
   *
   * @return fontHeight to use to calculate line spacing.
   */
  public int getFontHeight()
  {
    int retval = fontHeight;

    if(retval < 0)
    {
      final FontMetrics fontMetrics = getFontMetrics(getFont());
      retval = fontMetrics.getHeight();
      setFontHeight(retval);
    }

    return retval;
  }

  /**
   * Set the font width used in calculating line indenting.
   *
   * @param fontWidth to use to calculate line indenting.
   */
  public void setFontWidth(final int fontWidth)
  {
    this.fontWidth = fontWidth;
  }

  /**
   * Query the font width used in calculating line indenting.
   *
   * @return fontWidth to use to calculate line indenting.
   */
  public int getFontWidth()
  {
    int retval = fontWidth;

    if(fontWidth < 0)
    {
      final FontMetrics fontMetrics = getFontMetrics(getFont());
      retval = fontMetrics.stringWidth("_");
      setFontWidth(retval);
    }

    return retval;
  }

  /**
   * Query the maximum image size allowed.
   *
   * @return the maximum image size allowed.
   */
  public Dimension getMaximumSize()
  {
    final Dimension retval   = new Dimension();
    final Hashtable depthMap = getDepthMap();
    int             item     = 0;

    synchronized(getActiveVector())
    {
      final Enumeration keys = getDepthMap().keys();

      while(keys.hasMoreElements())
      {
        final Bookmark  bookmark = (Bookmark)keys.nextElement();
        final Rectangle bounds =
          getTextBounds(
            item++,
            bookmark,
            depthMap.get(bookmark));
        final int width = bounds.x + bounds.width;

        if(width > retval.width)
        {
          retval.width = width;
        }

        retval.height = bounds.y + bounds.height;
      }
    }

    retval.width += (2 * getFontMetrics(getFont()).getMaxAdvance());
    retval.height += (2 * getFontHeight());

    return retval;
  }

  /**
   * Query the minimum image size allowed.
   *
   * @return the minimum image size allowed.
   */
  public Dimension getMinimumSize()
  {
    return getMaximumSize();
  }

  /**
   * Query the preferred image size allowed.
   *
   * @return the preferred image size allowed.
   */
  public Dimension getPreferredSize()
  {
    return getMaximumSize();
  }

  /**
   * Query the row (item number) of the specified y coordinate.
   *
   * @param y position along the Y axis.
   *
   * @return the corresponding row number.
   */
  public int getRow(final int y)
  {
    final int fontHeight = getFontHeight();

    return (fontHeight > 0)
    ? (y / fontHeight)
    : 0;
  }

  /**
   * Called to change the visibility of the outline.
   *
   * @param value true if visible.
   */
  public void setVisible(final boolean value)
  {
    if(value != isVisible())
    {
      super.setVisible(value);

      Container parent = getParent();

      if((parent != null) && (parent.getComponentCount() == 1))
      {
        parent.setVisible(value);
      }

      invalidate();

      try
      {
        for(; parent != null; parent = parent.getParent())
        {
          try
          {
            parent.getClass().getMethod("resetToPreferredSizes", null).invoke(
              parent,
              null);
            visibleArgs[0] = new Integer(value
                ? 10
                : 0);
            parent.getClass().getMethod("setDividerSize", visibleParms)
                  .invoke(parent, visibleArgs);

            break;
          }
          catch(final Throwable ignored) {}
        }
      }
      catch(final Throwable ignored) {}

      djvuBean.recursiveRevalidate();
    }
  }

  /**
   * Called when the user clicks the mouse on an outline item.  If the check
   * box is checked, the checked value will be toggled.  If the name is
   * clicked, the bookmark page will be displayed.
   *
   * @param x position along the X axis clicked.
   * @param y position along the Y axis clicked.
   */
  public void clickLocation(
    final int x,
    final int y)
  {
    final int      item     = getRow(y);
    final Bookmark bookmark = getBookmark(item);

    if(bookmark != null)
    {
      Rectangle bounds =
        getCheckboxBounds(
          item,
          bookmark,
          getDepthMap().get(bookmark));

      if(
        (x >= bounds.x)
        && (y > bounds.y)
        && (x < (bounds.x + bounds.width))
        && (y < (bounds.y + bounds.height)))
      {
        setCheckedItem(item, !isCheckedItem(item));
      }
      else
      {
        bounds = getTextBounds(
            item,
            bookmark,
            getDepthMap().get(bookmark));

        if(
          (x >= bounds.x)
          && (y > bounds.y)
          && (x < (bounds.x + bounds.width))
          && (y < (bounds.y + bounds.height)))
        {
          final int pageno = bookmark.getPageno() + 1;

          if(pageno > 0)
          {
            djvuBean.setPage(pageno);
          }
        }
      }
    }
  }

  /**
   * Called to paint the outline.
   *
   * @param g graphics object to paint.
   */
  public void paint(final Graphics g)
  {
    final FontMetrics fontMetrics = getFontMetrics(getFont());
    setFontWidth(fontMetrics.stringWidth("_"));
    setFontHeight(fontMetrics.getHeight());

    if(firstTime)
    {
      firstTime = false;
      setVisible(
        "Outline".equalsIgnoreCase(
          djvuBean.properties.getProperty("navpane")));
    }

    if(!isVisible())
    {
      getParent().setVisible(false);
      invalidate();
      djvuBean.recursiveRevalidate();
    }
    else
    {
      synchronized(activeVector)
      {
        paintItem(
          0,
          g,
          getBookmark(0));
        paintCheckbox(
          0,
          g,
          getBookmark(0));
      }
    }
  }

  /**
   * Called to recursively paint the check box for the specified item.
   *
   * @param item row number
   * @param g graphics object to paint.
   * @param bookmark to check selected value.
   *
   * @return the next row number to paint.
   */
  public int paintCheckbox(
    final int      item,
    final Graphics g,
    final Bookmark bookmark)
  {
    int nextItem = item + 1;

    if(bookmark != null)
    {
      final Rectangle checkboxBounds =
        getCheckboxBounds(
          item,
          bookmark,
          getDepthMap().get(bookmark));
      final int       yCheckboxMidPoint =
        checkboxBounds.y + (checkboxBounds.height / 2);
      final int       xCheckboxMidPoint =
        checkboxBounds.x + (checkboxBounds.width / 2);
      final Enumeration e = bookmark.elements();

      if(e.hasMoreElements())
      {
        // clear any lines crossing through the checkbox.
        g.clearRect(
          checkboxBounds.x,
          checkboxBounds.y,
          checkboxBounds.width,
          checkboxBounds.height);

        // draw box around checkbox
        g.drawRect(
          checkboxBounds.x,
          checkboxBounds.y,
          checkboxBounds.width,
          checkboxBounds.height);

        // draw dash inside checkbox
        g.drawLine(
          checkboxBounds.x + 2,
          yCheckboxMidPoint,
          (checkboxBounds.x + checkboxBounds.width) - 2,
          yCheckboxMidPoint);

        boolean drawPlus = true;

        do
        {
          final Bookmark child = (Bookmark)e.nextElement();
          final Bookmark next = getBookmark(nextItem);

          if(child != next)
          {
            break;
          }

          drawPlus   = false;
          nextItem   = paintCheckbox(nextItem, g, child);
        }
        while(e.hasMoreElements());

        if(drawPlus)
        {
          g.drawLine(
            xCheckboxMidPoint,
            checkboxBounds.y + 2,
            xCheckboxMidPoint,
            (checkboxBounds.y + checkboxBounds.height) - 2);
        }
      }
    }

    return nextItem;
  }

  /**
   * Called to recursively paint text and lines for the specified item.
   *
   * @param item row number
   * @param g graphics object to paint.
   * @param bookmark to check selected value.
   *
   * @return the next row number to paint.
   */
  public int paintItem(
    final int      item,
    final Graphics g,
    final Bookmark bookmark)
  {
    int nextItem = item + 1;

    if(bookmark != null)
    {
      final Rectangle textBounds =
        getTextBounds(
          item,
          bookmark,
          getDepthMap().get(bookmark));
      final int       yTextMidPoint = textBounds.y + (textBounds.height / 2);
      final Enumeration e           = bookmark.elements();

      final String    displayName = bookmark.getDisplayName();

      if(displayName != null)
      {
        g.drawString(
          displayName,
          textBounds.x,
          textBounds.y + textBounds.height);
      }

      if(e.hasMoreElements())
      {
        // draw line from checkbox to text
        g.drawLine(
          textBounds.x - getFontWidth(),
          yTextMidPoint,
          textBounds.x - 2,
          yTextMidPoint);

        final int xLineToChildren = textBounds.x - (getFontWidth() / 2);
        int       southLine = yTextMidPoint;

        do
        {
          final Bookmark child = (Bookmark)e.nextElement();
          final Bookmark next = getBookmark(nextItem);

          if(child != next)
          {
            break;
          }

          southLine = (((2 * nextItem) + 1) * getFontHeight()) / 2;
          g.drawLine(xLineToChildren, southLine, textBounds.x - 2, southLine);
          nextItem = paintItem(nextItem, g, child);
        }
        while(e.hasMoreElements());

        if(southLine > yTextMidPoint)
        {
          g.drawLine(
            xLineToChildren,
            yTextMidPoint,
            xLineToChildren,
            southLine);
        }
      }
      else if(displayName != null)
      {
        g.drawLine(
          textBounds.x - getFontWidth(),
          yTextMidPoint,
          textBounds.x - 2,
          yTextMidPoint);
      }
    }

    return nextItem;
  }

  /**
   * Called with a DjVuBean property has changed.
   *
   * @param e the PropertyChangeEvent.
   */
  public void propertyChange(final PropertyChangeEvent e)
  {
    try
    {
      final String name = e.getPropertyName();

      if("page".equalsIgnoreCase(name))
      {
        final Object object = e.getNewValue();

        if(object instanceof Number)
        {
          setCheckedPage(((Number)object).intValue() - 1);
        }
      }
      else if("propertyName".equalsIgnoreCase(name))
      {
        final String propertyName = (String)e.getNewValue();

        if("navpane".equalsIgnoreCase(propertyName))
        {
          setVisible(
            "Outline".equalsIgnoreCase(
              djvuBean.properties.getProperty(propertyName)));
        }
      }
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }

  /**
   * Called to query the vector of active bookmarks.
   *
   * @return active bookmarks.
   */
  protected Vector getActiveVector()
  {
    return activeVector;
  }

  /**
   * Query the bounding rectangle for the specified checkbox.
   *
   * @param item row number
   * @param bookmark to check selected value.
   * @param depthObject depth of the bookmark.
   *
   * @return bounding rectangle.
   */
  protected Rectangle getCheckboxBounds(
    final int      item,
    final Bookmark bookmark,
    final Object   depthObject)
  {
    final int depth =
      (depthObject instanceof Number)
      ? ((Number)depthObject).intValue()
      : 0;
    final int fontWidth  = getFontWidth();
    final int fontHeight = getFontHeight();
    int       s          = ((fontWidth < fontHeight)
      ? fontWidth
      : fontHeight);

    return new Rectangle(
      ((((2 * depth) + 1) * fontWidth) - s) / 2,
      ((((2 * item) + 1) * fontHeight) - s) / 2,
      s,
      s);
  }

  /**
   * Set the checked value of the specified bookmark.
   *
   * @param bookmark to check.
   * @param activeVector of active bookmarks.
   *
   * @return the row number of the bookmark.
   */
  protected int setCheckedBookmark(
    final Bookmark bookmark,
    final Vector   activeVector)
  {
    final Bookmark parent = (Bookmark)(getParentMap().get(bookmark));
    int            item = 0;

    if(parent != null)
    {
      item = setCheckedBookmark(parent, activeVector);
    }

    while(item < activeVector.size())
    {
      final Bookmark current = (Bookmark)activeVector.elementAt(item++);

      if(current == bookmark)
      {
        setCheckedItem(item - 1, true, activeVector);

        break;
      }
    }

    return item;
  }

  /**
   * Check bookmark at the specified row number.
   *
   * @param item row number.
   * @param checked true if checked.
   * @param activeVector vector of active bookmarks.
   */
  protected void setCheckedItem(
    final int     item,
    final boolean checked,
    final Vector  activeVector)
  {
    final Bookmark bookmark = getBookmark(item);

    if(bookmark.size() > 0)
    {
      final Enumeration e = bookmark.elements();
      int               i = item + 1;

      if(e.hasMoreElements())
      {
        if(checked)
        {
          do
          {
            final Bookmark child = (Bookmark)e.nextElement();
            final Bookmark next = getBookmark(i);

            if(next == child)
            {
              break;
            }

            activeVector.insertElementAt(child, i++);
          }
          while(e.hasMoreElements());
        }
        else
        {
          do
          {
            final Bookmark child = (Bookmark)e.nextElement();
            final Bookmark next = getBookmark(i);

            if(next != child)
            {
              break;
            }

            setCheckedItem(i, false);
            activeVector.removeElementAt(i);
          }
          while(e.hasMoreElements());
        }
      }

      repaint(20L);
    }
  }

  /**
   * Query the Hashtable listing the depths of bookmarks.
   *
   * @return Hashtable of bookmark depths.
   */
  protected Hashtable getDepthMap()
  {
    return depthMap;
  }

  /**
   * Query the vector of page numbers mapped to bookmarks.
   *
   * @return vector of page numbers.
   */
  protected Vector getPagenoVector()
  {
    return pagenoVector;
  }

  /**
   * Query the Hashtable of bookmark parents.
   *
   * @return Hashtable of bookmark parents.
   */
  protected Hashtable getParentMap()
  {
    return parentMap;
  }

  /**
   * Query the bounding rectangle of the specified row.
   *
   * @param item row number.
   * @param bookmark for the specified row.
   * @param depthObject indicating depth.
   *
   * @return bounding rectangle.
   */
  protected Rectangle getTextBounds(
    final int      item,
    final Bookmark bookmark,
    final Object   depthObject)
  {
    final int    fontWidth   = getFontWidth();
    final int    fontHeight  = getFontHeight();
    final String displayName = bookmark.getDisplayName();
    final int    depth       =
      (depthObject instanceof Number)
      ? ((Number)depthObject).intValue()
      : 0;

    return new Rectangle(
      (depth + 2) * fontWidth,
      item * fontHeight,
      (displayName != null)
      ? (getFontMetrics(getFont()).stringWidth(displayName))
      : fontWidth,
      fontHeight);
  }

  /**
   * Called to create a map the parent of each bookmark.
   *
   * @param parent top level bookmark.
   * @param parentMap map to add children to.
   * @param pagenoVector vector of page numbers.
   * @param depth object indicating bookmark depth.
   */
  protected void mapChildren(
    final Bookmark  parent,
    final Hashtable parentMap,
    final Vector    pagenoVector,
    final Number    depth)
  {
    getDepthMap().put(parent, depth);

    final int pageno = parent.getPageno();

    if(pageno >= 0)
    {
      while(pageno >= pagenoVector.size())
      {
        pagenoVector.addElement(null);
      }

      pagenoVector.setElementAt(parent, pageno);
    }

    final Enumeration e = parent.elements();

    if(e.hasMoreElements())
    {
      final Number childDepth = new Integer(depth.intValue() + 1);

      do
      {
        final Bookmark child = (Bookmark)e.nextElement();
        parentMap.put(child, parent);
        mapChildren(child, parentMap, pagenoVector, childDepth);
      }
      while(e.hasMoreElements());
    }
  }
}
