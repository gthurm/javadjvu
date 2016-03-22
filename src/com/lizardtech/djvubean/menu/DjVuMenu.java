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
package com.lizardtech.djvubean.menu;

import com.lizardtech.djvu.DjVuOptions;
import com.lizardtech.djvubean.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.accessibility.*;


/**
 * This class implements a pop-up menu the user may use to navigate the
 * DjVu Document.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.9 $
 */
public class DjVuMenu
  extends PopupMenu
  implements MouseListener, PropertyChangeListener
{
  //~ Instance fields --------------------------------------------------------

  // about listener
  private final ActionListener aboutListener = new Listener(4);

  // help listener
  private final ActionListener helpListener = new Listener(5);

  // navigation listener
  private final ActionListener navigationListener = new Listener(3);

  // find listener
  private final ItemListener findListener = new Listener(2);

  // nav pane listener
  private final ItemListener navPaneListener = new Listener(1);

  // zoom listener 
  private final Listener zoomListener = new Listener(0);

  // page layout listener 
  private final Listener pageLayoutListener = new Listener(6);

  // The menuitem used for activating and deactiving the nav pane.
  private Menu navPaneMenu = null;

  // The menuitem used for selected the zoom value.
  private Menu zoomMenu = null;

  // The menuitem used for selected the page layout value.
  private Menu pageLayoutMenu = null;

  // The menuitem used for about.
  private MenuItem aboutMenuItem = null;

  // The menuitem used for text search.
  private MenuItem findMenuItem = null;

  // The menuitem used for help.
  private MenuItem helpMenuItem = null;

  //~ Constructors -----------------------------------------------------------

//  private DjVuBean djvuBean;

  /**
   * Creates a new MenuBar object.
   *
   * @param bean DjVuBean to add this menu to.
   */
  public DjVuMenu(final DjVuBean bean)
  {
    bean.add(this);
    bean.addMouseListener(this);
    bean.addPropertyChangeListener(this);
    bean.properties.put(
      "addOn.menu",
      bean.properties.getProperty("menu", "true"));
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Query and/or create the about menu item.
   *
   * @return about menu item.
   */
  public MenuItem getAboutMenuItem()
  {
    MenuItem retval = aboutMenuItem;

    if(retval == null)
    {
      retval = new MenuItem("About");
      retval.addActionListener(aboutListener);
      aboutMenuItem = retval;
    }

    return retval;
  }

  /**
   * Query the about url.
   *
   * @return about url
   */
  public String getAboutURL()
  {
    return getDjVuBean().properties.getProperty(
      "abouturl",
      "http://javadjvu.foxtrottechnologies.com/"+(com.lizardtech.djvu.DjVuOptions.VERSION.replace('_', '/'))+"/releasenotes/");
  }

  /**
   * Query the DjVuBean this DjVuMenu has been associated with.
   *
   * @return the parent cast as a DjVuBean.
   */
  public DjVuBean getDjVuBean()
  {
    return (DjVuBean)getParent();
  }

  /**
   * Query and/or create the text search menu item.
   *
   * @return the text search menu item.
   */
  public MenuItem getFindMenuItem()
  {
    CheckboxMenuItem retval = null;
    final String     value =
      getDjVuBean().properties.getProperty("addOn.finder");

    if(value != null)
    {
      retval = (CheckboxMenuItem)findMenuItem;

      if(retval == null)
      {
        retval = new CheckboxMenuItem("Find");
        retval.addItemListener(findListener);
        findMenuItem = retval;
      }

      retval.setState(DjVuBean.stringToBoolean(value, false));
    }

    return retval;
  }

  /**
   * Query and/or create the help menu item.
   *
   * @return the help menu item.
   */
  public MenuItem getHelpMenuItem()
  {
    MenuItem retval = helpMenuItem;

    if(retval == null)
    {
      retval = new MenuItem("Help");
      retval.addActionListener(helpListener);
      helpMenuItem = retval;
    }

    return retval;
  }

  /**
   * Query the help url
   *
   * @return the help url.
   */
  public String getHelpURL()
  {
    return getDjVuBean().properties.getProperty(
      "helpurl",
      "http://javadjvu.sourceforge.net");
  }

  /**
   * Query the navpane setting.
   *
   * @return the navpane setting.
   */
  public String getNavPane()
  {
    return getDjVuBean().properties.getProperty("navpane", "None");
  }

  /**
   * Query and/or create the nav pane menu.
   *
   * @return the nav pane menu.
   */
  public Menu getNavPaneMenu()
  {
    Menu retval = navPaneMenu;

    if(retval == null)
    {
      final String navPane =
        getDjVuBean().properties.getProperty("addOn.NavPane", "None");
      int          i = navPane.indexOf(',');

      if(i >= 0)
      {
        retval = new Menu("Navigation Pane");

        int j = 0;

        do
        {
          final String           xname = navPane.substring(j, i);
          final CheckboxMenuItem xitem = new CheckboxMenuItem(xname);
          xitem.addItemListener(navPaneListener);
          retval.add(xitem);
          j   = i + 1;
          i   = navPane.indexOf(',', j);
        }
        while(i >= 0);

        final String           name = navPane.substring(j);
        final CheckboxMenuItem item = new CheckboxMenuItem(name);
        item.addItemListener(navPaneListener);
        retval.add(item);
        navPaneMenu = retval;
      }
    }

    return retval;
  }

  /**
   * Query and/or create the zoom menu.
   *
   * @return the zoom menu.
   */
  public Menu getZoomMenu()
  {
    Menu retval = zoomMenu;

    if(retval == null)
    {
      retval = new Menu("Zoom");

      for(int i = 0; i < DjVuBean.ZOOM_STANDARD_LIST.length;)
      {
        final CheckboxMenuItem item =
          new CheckboxMenuItem(DjVuBean.ZOOM_STANDARD_LIST[i++]);
        item.addItemListener(zoomListener);
        retval.add(item);
      }

      retval.addSeparator();

      for(int i = 0; i < DjVuBean.ZOOM_SPECIAL_LIST.length;)
      {
        final CheckboxMenuItem item =
          new CheckboxMenuItem(DjVuBean.ZOOM_SPECIAL_LIST[i++]);
        item.addItemListener(zoomListener);
        retval.add(item);
      }

      retval.addSeparator();

      for(int i = 0; i < DjVuBean.ZOOM_BUTTON_LIST.length;)
      {
        final MenuItem item = new MenuItem(DjVuBean.ZOOM_BUTTON_LIST[i++]);
        item.addActionListener(zoomListener);
        retval.add(item);
      }

      zoomMenu = retval;
    }

    return retval;
  }

  /**
   * Query and/or create the zoom menu.
   *
   * @return the zoom menu.
   */
  public Menu getPageLayoutMenu()
  {
    Menu retval = pageLayoutMenu;
    if(retval == null)
    {
      retval = new Menu("Page Layout");

      for(int i = 0; i < DjVuBean.PAGE_LAYOUT_LIST.length;)
      {
        final CheckboxMenuItem item =
          new CheckboxMenuItem(DjVuBean.PAGE_LAYOUT_LIST[i++]);
        item.addItemListener(pageLayoutListener);
        retval.add(item);
      }
      pageLayoutMenu = retval;
    }

    return retval;
  }

  /**
   * Called to initialize or reinitialize the menu layout.
   */
  public void init()
  {
    if(getItemCount() == 0)
    {
      synchronized(this)
      {
        if(getItemCount() == 0)
        {
          final DjVuBean djvuBean = getDjVuBean();
          final Menu     zoomMenu = getZoomMenu();

          if(zoomMenu != null)
          {
            add(zoomMenu);
            updateMenu(
              zoomMenu,
              djvuBean.getZoom());
          }

          final int pageCount = djvuBean.getDocumentSize();

          if(pageCount > 1)
          {
            createNavigation(pageCount);

            final Menu navPaneMenu = getNavPaneMenu();

            if(navPaneMenu != null)
            {
              add(navPaneMenu);
              updateMenu(
                navPaneMenu,
                getNavPane());
            }
            
            final Menu pageLayoutMenu=getPageLayoutMenu();
            if(pageLayoutMenu != null)
            {
                add(pageLayoutMenu);
                updateMenu(
                  pageLayoutMenu,
                  djvuBean.getPageLayout());
            }
          }

          final MenuItem findMenuItem = getFindMenuItem();

          if(findMenuItem != null)
          {
            addSeparator();
            add(findMenuItem);
          }

          final String helpURL  = getHelpURL();
          final String aboutURL = getAboutURL();

          if((helpURL != null) && (helpURL.length() > 0))
          {
            addSeparator();
            add(getHelpMenuItem());

            if((aboutURL != null) && (aboutURL.length() > 0))
            {
              add(getAboutMenuItem());
            }
          }
          else if((aboutURL != null) && (aboutURL.length() > 0))
          {
            addSeparator();
            add(getAboutMenuItem());
          }
        }
      }
    }
  }

  /**
   * Called when the mouse is clicked.  If a right mouse click or a control
   * key is pressed, then the menu will be displayed.
   *
   * @param event indicating mouse action.
   */
  public void mouseClicked(MouseEvent event)
  {
    try
    {
      final DjVuBean djvuBean = getDjVuBean();

      if((event.getModifiers() & ~InputEvent.BUTTON1_MASK) != 0)
      {
        if(
          djvuBean.stringToBoolean(
            djvuBean.properties.getProperty("menu"),
            true))
        {
          init();

          if(djvuBean instanceof DjVuViewport)
          {
            final Point scrollPosition =
              ((DjVuViewport)djvuBean).getScrollPosition();
            show(
              djvuBean,
              event.getX() - scrollPosition.x,
              event.getY() - scrollPosition.y);
          }
          else
          {
            show(
              djvuBean,
              event.getX(),
              event.getY());
          }
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
   * Called when the mouse enters the DjVuBean.
   *
   * @param event indicating mouse action.
   */
  public void mouseEntered(MouseEvent event) {}

  /**
   * Called when the mouse exits the DjVuBean.
   *
   * @param e indicating mouse action.
   */
  public void mouseExited(MouseEvent e) {}

  /**
   * Called when the mouse button is pressed.
   *
   * @param e indicating mouse action.
   */
  public void mousePressed(MouseEvent e) {}

  /**
   * Called when the mouse button is released.
   *
   * @param e indicating mouse action.
   */
  public void mouseReleased(MouseEvent e) {}

  /**
   * Called with a DjVuBean property has changed.
   *
   * @param event indicating the property change.
   */
  public void propertyChange(final PropertyChangeEvent event)
  {
    try
    {
      final String name = event.getPropertyName();

      if(name.equals("zoom"))
      {
        updateMenu(
          getZoomMenu(),
          (String)event.getNewValue());
      }
      else if(name.equals("propertyName"))
      {
        final String propertyName = (String)event.getNewValue();

        if("HelpURL".equalsIgnoreCase(propertyName))
        {
          synchronized(this)
          {
            removeAll();
          }
        }
        else if("AboutURL".equalsIgnoreCase(propertyName))
        {
          synchronized(this)
          {
            removeAll();
          }
        }
        else if("NavPane".equalsIgnoreCase(propertyName))
        {
          updateMenu(
            getNavPaneMenu(),
            getNavPane());
        }
        else if("pagelayout".equalsIgnoreCase(propertyName))
        {
          updateMenu(
            getPageLayoutMenu(),
            (String)event.getNewValue());
        }
      }
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }

  private static void updateMenu(
    final Menu   menu,
    final String label)
  {
    if((menu != null) && (label != null))
    {
      for(int i = menu.getItemCount(); i-- > 0;)
      {
        final MenuItem item = menu.getItem(i);

        if(item instanceof CheckboxMenuItem)
        {
          final CheckboxMenuItem cItem  = (CheckboxMenuItem)item;
          final boolean          state  = cItem.getState();
          final boolean          isSame =
            label.equalsIgnoreCase(item.getLabel());

          if(isSame != state)
          {
            cItem.setState(isSame);
          }
        }
      }
    }
  }

  private void createNavigation(final int pageCount)
  {
    final MenuItem[] menuItemArray = new MenuItem[pageCount];

    for(int i = 0, j = 1; i < pageCount;)
    {
      final MenuItem item = new MenuItem(Integer.toString(j++));
      item.addActionListener(navigationListener);
      menuItemArray[i++] = item;
    }

    final Menu pageMenu =
      createPageMenu(pageCount, 10, menuItemArray, pageCount);
    pageMenu.addSeparator();

    for(int i = 0; i < DjVuBean.NAVIGATE_LIST.length;)
    {
      final MenuItem item = new MenuItem(DjVuBean.NAVIGATE_LIST[i++]);
      item.addActionListener(navigationListener);
      pageMenu.add(item);
    }

    insert(pageMenu, 0);
  }

  private Menu createPageMenu(
    final int        end,
    final int        nstep,
    final MenuItem[] itemArray,
    final int        length)
  {
    if(length < 2)
    {
      itemArray[0].setLabel("Navigate");

      return (Menu)itemArray[0];
    }

    final int        nlength    = (length + 7) / 10;
    final MenuItem[] nitemArray =
      (nstep == 10)
      ? (new MenuItem[nlength])
      : itemArray;

    for(
      int i = 0, j = 0, k = 0, nstart = 1, nend = 0;
      i < nlength;
      nstart += nstep)
    {
      if((i + 1) == nlength)
      {
        k      = length;
        nend   = end;
      }
      else
      {
        k += 10;
        nend += nstep;
      }

      final Menu menu = new Menu(nstart + "-" + nend);

      do
      {
        menu.add(itemArray[j++]);
      }
      while(j < k);

      nitemArray[i++] = menu;
    }

    return createPageMenu(end, nstep * 10, nitemArray, nlength);
  }

  //~ Inner Classes ----------------------------------------------------------

  private class Listener
    implements ItemListener, ActionListener
  {
    //~ Instance fields ------------------------------------------------------

    // Value which determines the type of listener this is.
    private final int listener;

    //~ Constructors ---------------------------------------------------------

    /**
     * Creates a new Listener object.
     *
     * @param listener indicates the type of listener
     */
    public Listener(final int listener)
    {
      this.listener = listener;
    }

    //~ Methods --------------------------------------------------------------

    /**
     * Called when an item is clicked on and performs the specified action.
     *
     * @param event the ActionEvent generated by the menu item
     */
    public void actionPerformed(final ActionEvent event)
    {
      try
      {
        final DjVuBean bean = getDjVuBean();

        switch(listener)
        {
          case 0 : // zoom
          {
            bean.setZoom(((MenuItem)event.getSource()).getLabel());

            break;
          }
          case 3 : // navigation
          {
            bean.setPageString(((MenuItem)event.getSource()).getLabel());

            break;
          }
          case 4 : // about
          {
            bean.setSubmit(getAboutURL());

            break;
          }
          case 5 : // help
          {
            bean.setSubmit(getHelpURL());

            break;
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
     * Called when a menu item is toggled and performs the specified action.
     *
     * @param event ItemEvent generated by the menu item.
     */
    public void itemStateChanged(final ItemEvent event)
    {
      try
      {
        final CheckboxMenuItem item     = (CheckboxMenuItem)event.getSource();
        final String           label    = item.getLabel();
        final DjVuBean         djvuBean = getDjVuBean();

        switch(listener)
        {
          case 6 : // page layout
          {
            if(item.getState())
            {
               final Menu pageMenu=getPageLayoutMenu();
               for(int i=pageMenu.getItemCount();i-- > 0;)
               {
                 final MenuItem x=pageMenu.getItem(i);
                 if((x instanceof CheckboxMenuItem)&&(item != x))
                 {
                   ((CheckboxMenuItem)x).setState(false);
                 }
               }
               djvuBean.setPageLayout(item.getLabel());
            }
            else if(djvuBean.getPageLayout().equals(label))
            {
              item.setState(true);
            }
            break;
          }
          case 0 : // zoom
          {
            if(item.getState())
            {
              djvuBean.setZoom(item.getLabel());
            }
            else if(djvuBean.getZoom().equals(label))
            {
              item.setState(true);
            }

            break;
          }
          case 1 : // nav pane
          {
            final Properties properties = djvuBean.properties;

            if(item.getState())
            {
              properties.put("navpane", label);
            }
            else if(
              properties.getProperty("navpane", "None").equalsIgnoreCase(
                label))
            {
              item.setState(true);
            }

            break;
          }
          case 2 : // find 
          {
            djvuBean.properties.put(
              "addOn.finder",
              item.getState()
              ? "true"
              : "false");

            break;
          }
        }
      }
      catch(final Throwable exp)
      {
        exp.printStackTrace(DjVuOptions.err);
        System.gc();
      }
    }
  }
}
