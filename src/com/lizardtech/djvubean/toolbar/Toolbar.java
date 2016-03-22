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

import com.lizardtech.djvu.*;
import com.lizardtech.djvubean.DjVuBean;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/**
 * The DjVu toolbar.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.15 $
 */
public class Toolbar
  extends Container
  implements ActionListener, ItemListener, PropertyChangeListener
{
  //~ Static fields/initializers ---------------------------------------------

  /** Arguments used to create the first page button. */
  public static final Object[] FIRST_PAGE_BUTTON =
  {"First Page", "|<<", "firstpage.djvu", new Dimension(24, 24)};

  /** Arguments used to create the first page button. */
  public static final Object[] SEARCH_DOCUMENT_BACK_BUTTON =
  {"Search To First Page", "<<", "searchbackdoc.djvu", new Dimension(24, 24)};

  /** Arguments used to create the last page button. */
  public static final Object[] LAST_PAGE_BUTTON =
  {"Last Page", ">>|", "lastpage.djvu", new Dimension(24, 24)};

  /** Arguments used to create the last page button. */
  public static final Object[] SEARCH_DOCUMENT_FWD_BUTTON =
  {"Search To Last Page", ">>", "searchfwddoc.djvu", new Dimension(24, 24)};

  /** Arguments used to create the logo button. */
  public static final Object[] LOGO_BUTTON =
  {"LizardTech Inc.", "LizardTech", "lizardtech.djvu", new Dimension(98, 24)};

  /** Arguments used to create the logo button. */
  public static final Object[] SEPARATOR_BUTTON =
  {"", "", null, new Dimension(1, 1)};

  /** Arguments used to create the next page button. */
  public static final Object[] NEXT_PAGE_BUTTON =
  {"Next Page", ">", "nextpage.djvu", new Dimension(24, 24)};

  /** Arguments used to create the next page button. */
  public static final Object[] SEARCH_FORWARD_BUTTON =
  {"Search Forward", "?>", "searchfwd.djvu", new Dimension(24, 24)};

  /** Arguments used to create the previous page button. */
  public static final Object[] PREVIOUS_PAGE_BUTTON =
  {"Prev Page", "<", "prevpage.djvu", new Dimension(24, 24)};

  /** Arguments used to create the previous page button. */
  public static final Object[] SEARCH_BACK_BUTTON =
  {"Search Backwards", "<?", "searchback.djvu", new Dimension(24, 24)};

  /** Arguments used to create the zoom in button. */
  public static final Object[] ZOOMIN_BUTTON =
  {"ZoomIn", "+", "zoomin.djvu", new Dimension(24, 24)};

  /** Arguments used to create the zoom out button. */
  public static final Object[] ZOOMOUT_BUTTON =
  {"ZoomOut", "-", "zoomout.djvu", new Dimension(24, 24)};

  /** Arguments used to create the page selection combobox. */
  public static final Object[] PAGE_SELECT =
  {"Select Page", "-", "select.djvu", new Dimension(16, 16)};

  /** Arguments used to create the zoom selection combobox. */
  public static final Object[] ZOOM_SELECT =
  {"Select Zoom", "-", "select.djvu", new Dimension(24, 24)};

  /** Arguments used to create the actual size button. */
  public static final Object[] ACTUAL_SIZE_BUTTON =
  {"100%", "100%", "actualsi.djvu", new Dimension(24, 24)};

  /** Arguments used to create the fit page button. */
  public static final Object[] FIT_PAGE_BUTTON =
  {"Fit Page", "FitPage", "fitpage.djvu", new Dimension(24, 24)};

  /** Arguments used to create the fit width button. */
  public static final Object[] FIT_WIDTH_BUTTON =
  {"Fit Width", "FitWidth", "fitwidth.djvu", new Dimension(24, 24)};

  /** Arguments used to create the pan mode button. */
  public static final Object[] SEARCH_BUTTON =
  {"Search", "Search", "search.djvu", new Dimension(24, 24)};

  /** Arguments used to create the pan mode button. */
  public static final Object[] PAN_MODE_BUTTON =
  {"Pan Mode", "Pan", "hand.djvu", new Dimension(24, 24)};

  /** Arguments used to create the pan mode button. */
  public static final Object[] ZOOM_MODE_BUTTON =
  {"Zoom Mode", "Zoom", "zoomselect.djvu", new Dimension(24, 24)};

  /** Arguments used to create the pan mode button. */
  public static final Object[] TEXT_MODE_BUTTON =
  {"Text Mode", "Text", "textselect.djvu", new Dimension(24, 24)};

  /** card name for search mode buttons */
  public static final String SEARCH_SELECTED_STRING = "SearchSelected";

  /** card name for navigation buttons */
  public static final String SEARCH_NOT_SELECTED_STRING = "SearchNotSelected";

  /** the color black */
  public static final Color BLACK = new Color(0, 0, 0);

  /** the color gray */
  public static final Color LIGHT_GRAY               =
    new Color(192, 192, 192);
  private static final Class classComboBox;
  private static Method      addItemMethod;
  private static Method      addItemListenerMethod;
  private static Method      removeItemListenerMethod;
  private static Method      setSelectedItemMethod;
  private static Method      setPreferredSizeMethod;
  private static Method      setEditableMethod;
  private static Method      getEditorMethod;
  private static Method      addActionListenerMethod;

  static
  {
    Class xclassComboBox = null;
    try
    {
      Class classComboBoxEditor=Class.forName("javax.swing.ComboBoxEditor");
      xclassComboBox = Class.forName("javax.swing.JComboBox");  
      final Class[] c = {Object.class};
      addItemMethod           = xclassComboBox.getMethod("addItem", c);
      setSelectedItemMethod   = xclassComboBox.getMethod("setSelectedItem", c);

      final Class[] c2        = {ItemListener.class};
      addItemListenerMethod   = xclassComboBox.getMethod(
        "addItemListener",
        c2);
      removeItemListenerMethod =
        xclassComboBox.getMethod("removeItemListener", c2);

      final Class[] c3        = {Boolean.TYPE};
      setEditableMethod   = xclassComboBox.getMethod("setEditable", c3);
      getEditorMethod     = xclassComboBox.getMethod("getEditor", null);

      final Class[] c4    = {ActionListener.class};
      addActionListenerMethod =
        classComboBoxEditor.getMethod(
          "addActionListener",
          c4);
      try
      {
        final Class[] c5 = {Dimension.class};
        setPreferredSizeMethod = xclassComboBox.getMethod("setPreferredSize", c5);
      }
      catch(final Throwable exp)
      {
        setPreferredSizeMethod = null;
      }
    }
    catch(final Throwable exp)
    {
      xclassComboBox             = ComboBox.class;
      addItemMethod              = null;
      addItemListenerMethod      = null;
      removeItemListenerMethod   = null;
      setSelectedItemMethod      = null;
      setEditableMethod          = null;
      getEditorMethod            = null;
      addActionListenerMethod    = null;
      setPreferredSizeMethod     = null;
    }
    classComboBox=xclassComboBox;
  }

  //~ Instance fields --------------------------------------------------------

  /** The combobox for selecting the current page. */
  protected final Container pageSelect;

  /** The combobox for zooming. */
  protected final Container zoomSelect;

  /** The panel with mode buttons. */
  protected final Panel modeButtonPanel = new Panel();

  /** The panel with page navigation buttons. */
  protected final Panel pagePanel = new Panel();

  /** The panel with zoom and page panels. */
  protected final Panel zoomPagePanel = new Panel();

  /** The panel with zoom buttons. */
  protected final Panel zoomPanel = new Panel();

  /** The mode button to zoom to actual size (100 dpi) */
  protected final ToggleButton actualSize;

  /** The button to go to the first page. */
  protected final ToggleButton firstPage;

  /** The mode button to fit page. */
  protected final ToggleButton fitPage;

  /** The mode button to fit width. */
  protected final ToggleButton fitWidth;

  /** The button to go to the last page. */
  protected final ToggleButton lastPage;

  /** The LizardTech logo button. */
  protected final ToggleButton logo;

  /** The LizardTech logo button. */
  protected final ToggleButton logo2;

  /** The next page button. */
  protected final ToggleButton nextPage;

  /** The mode button to pan the document. */
  protected final ToggleButton panMode;

  /** The previous page button. */
  protected final ToggleButton prevPage;

  /** The search button to search the document. */
  protected final ToggleButton searchMode;

  /** The mode button to display hiddent text. */
  protected final ToggleButton textMode;

  /** The zoom in button. */
  protected final ToggleButton zoomIn;

  /** The mode button to pan the document. */
  protected final ToggleButton zoomMode;

  /** The zoom out button. */
  protected final ToggleButton zoomOut;

  /** Used for typing in a page number. */
  protected ActionListener pageSelectEditorActionListener;

  // The DjVuBean associated with this toolbar.
  private DjVuBean djvuBean = null;

  // The list of buttons.
  private Vector buttonList = new Vector();

  // The list of toggle buttons.
  private Vector toggleButtonList = new Vector();

  // true if the toolbar is visible.
  private boolean toolbar = false;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new Toolbar object.
   *
   * @param bean the DjVuBean being monitored and controled.
   */
  public Toolbar(final DjVuBean bean)
  {
    djvuBean     = bean;
    pageSelect   = createComboBox(PAGE_SELECT);
    zoomSelect   = createComboBox(ZOOM_SELECT);

    firstPage   = createButton(FIRST_PAGE_BUTTON);
    lastPage    = createButton(LAST_PAGE_BUTTON);
    logo        = createButton(LOGO_BUTTON);
    logo2       = createButton(LOGO_BUTTON);
    nextPage    = createButton(NEXT_PAGE_BUTTON);
    prevPage    = createButton(PREVIOUS_PAGE_BUTTON);
    zoomIn      = createButton(ZOOMIN_BUTTON);
    zoomOut     = createButton(ZOOMOUT_BUTTON);

    searchMode   = createToggleButton(SEARCH_BUTTON);
    actualSize   = createToggleButton(ACTUAL_SIZE_BUTTON);
    fitPage      = createToggleButton(FIT_PAGE_BUTTON);
    fitWidth     = createToggleButton(FIT_WIDTH_BUTTON);
    panMode      = createToggleButton(PAN_MODE_BUTTON);
    zoomMode     = createToggleButton(ZOOM_MODE_BUTTON);
    textMode     = createToggleButton(TEXT_MODE_BUTTON);

    logo.setBorderType(ToggleButton.NOBORDER);
    logo2.setBorderType(ToggleButton.NOBORDER);

    modeButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
    modeButtonPanel.add(panMode);
    modeButtonPanel.add(zoomMode);
    modeButtonPanel.add(textMode);
    modeButtonPanel.add(searchMode);
    modeButtonPanel.setLayout(new GridLayout(1, 4));

//    zoomPagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
    zoomPagePanel.setLayout(new GridLayout(1, 2));
    zoomPagePanel.add(zoomPanel);
    zoomPagePanel.add(pagePanel);
    setLayout(new BorderLayout());

    if(bean.getTextSearchObject() != null)
    {
      add(
        new Finder(bean),
        BorderLayout.SOUTH);
    }

    add(zoomPagePanel, BorderLayout.CENTER);
    add(modeButtonPanel, BorderLayout.WEST);
    add(logo, BorderLayout.EAST);

    try
    {
      toolbar =
        DjVuBean.stringToBoolean(
          bean.properties.getProperty("toolbar"),
          true);
    }
    catch(Throwable ignored) {}

    zoomPagePanel.setVisible(toolbar);
    modeButtonPanel.setVisible(toolbar);
    logo.setVisible(toolbar);

    setBackground(LIGHT_GRAY);
    setForeground(BLACK);

    for(Enumeration e = buttonList.elements(); e.hasMoreElements();)
    {
      final ToggleButton button = (ToggleButton)e.nextElement();
      button.setBackground(LIGHT_GRAY);
      button.setForeground(BLACK);
      button.addActionListener(this);
    }

    for(Enumeration e = toggleButtonList.elements(); e.hasMoreElements();)
    {
      final ToggleButton button = (ToggleButton)e.nextElement();
      button.setBackground(LIGHT_GRAY);
      button.setForeground(BLACK);
      button.addItemListener(this);
    }

    for(int i = 0; i < DjVuBean.ZOOM_STANDARD_LIST.length;)
    {
      addItemComboBox(zoomSelect, DjVuBean.ZOOM_STANDARD_LIST[i++]);
    }

    for(int i = 0; i < DjVuBean.ZOOM_SPECIAL_LIST.length;)
    {
      addItemComboBox(zoomSelect, DjVuBean.ZOOM_SPECIAL_LIST[i++]);
    }

    editorActionListener(zoomSelect, "%");
    addItemListenerComboBox(zoomSelect, this);

    final int documentSize = bean.getDocumentSize();

    for(int i = 0; i < documentSize;)
    {
      addItemComboBox(
        pageSelect,
        new Integer(++i));
    }

    setSelectedItemComboBox(
      pageSelect,
      new Integer(1));
    editorActionListener(pageSelect, " ");
    pageSelect.setEnabled(documentSize > 1);
    addItemListenerComboBox(pageSelect, this);

    pageSelect.setVisible(documentSize > 1);
    firstPage.setVisible(documentSize > 1);
    prevPage.setVisible(documentSize > 1);
    lastPage.setVisible(documentSize > 1);
    nextPage.setVisible(documentSize > 1);

    bean.addPropertyChangeListener(this);
    updatePage(new Integer(bean.getPage()));
    updateMode(bean.getMode());
    updateSearchMode();
    updateZoom();
    updateTargetWidth(
      -1,
      bean.getTargetWidth());
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Set the target size of the Window.  This is used to determine how the
   * toolbar should be layed out.
   *
   * @param width target window width
   * @param height target window height
   */
  public void setSize(
    final int width,
    final int height)
  {
    djvuBean.setTargetWidth(width);
    super.setSize(width, height);
  }

  /**
   * Called when a button is pressed.
   *
   * @param event indicating the action performed.
   */
  public void actionPerformed(final ActionEvent event)
  {
    try
    {
      final Object source = event.getSource();

      if(source instanceof ToggleButton)
      {
        if(lastPage == source)
        {
          djvuBean.setPageString(DjVuBean.LAST_PAGE);
        }
        else if((logo == source) || (logo2 == source))
        {
          djvuBean.setSubmit(
            djvuBean.properties.getProperty(
              "logourl",
              "http://www.lizardtech.com"));
        }
        else if(firstPage == source)
        {
          djvuBean.setPageString(DjVuBean.FIRST_PAGE);
        }
        else if(prevPage == source)
        {
          djvuBean.setPageString(DjVuBean.PREV_PAGE);
        }
        else if(nextPage == source)
        {
          djvuBean.setPageString(DjVuBean.NEXT_PAGE);
        }
        else if(fitWidth == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM_FIT_WIDTH);
        }
        else if(fitPage == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM_FIT_PAGE);
        }
        else if(actualSize == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM100);
        }
        else if(zoomIn == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM_IN);
        }
        else if(zoomOut == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM_OUT);
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
   * Called to create a new ToggleButton.
   *
   * @param buttonList Vector of buttons to append this item to
   * @param args the arguments used to create this button
   *
   * @return the newly created button
   */
  public static ToggleButton createButton(
    final Vector   buttonList,
    final Object[] args)
  {
    final String tip =
      ((args.length > 0) && (args[0] != null))
      ? (args[0].toString())
      : null;
    final String text =
      ((args.length > 1) && (args[1] != null))
      ? args[1].toString()
      : null;
    final Object    imageArg = args[2];
    final Dimension size =
      ((args.length > 3) && (args[3] instanceof Dimension))
      ? (Dimension)args[3]
      : new Dimension(-1, -1);
    final ToggleButton retval =
      new ToggleButton(tip, (imageArg == null)
        ? text
        : null, imageArg, size);
    retval.setActionCommand(tip);
    buttonList.addElement(retval);

    return retval;
  }

  /**
   * Called when an item state changes.
   *
   * @param event indicating the changed item.
   */
  public void itemStateChanged(ItemEvent event)
  {
    try
    {
      final Object source = event.getSource();

      if(source == null)
      {
        return;
      }

      if(panMode == source)
      {
        if(panMode.isSelected())
        {
          djvuBean.setMode(DjVuBean.PAN_MODE);
        }
        else if(!(textMode.isSelected() || zoomMode.isSelected()))
        {
          djvuBean.setMode(DjVuBean.LAST_MODE);
        }
      }
      else if(textMode == source)
      {
        if(textMode.isSelected())
        {
          djvuBean.setMode(DjVuBean.TEXT_MODE);
        }
        else if(!(panMode.isSelected() || zoomMode.isSelected()))
        {
          djvuBean.setMode(DjVuBean.LAST_MODE);
        }
      }
      else if(zoomMode == source)
      {
        if(zoomMode.isSelected())
        {
          djvuBean.setMode(DjVuBean.ZOOM_MODE);
        }
        else if(!(panMode.isSelected() || textMode.isSelected()))
        {
          djvuBean.setMode(DjVuBean.LAST_MODE);
        }
      }
      else if(searchMode == source)
      {
        djvuBean.properties.put(
          "addOn.finder",
          (searchMode.isSelected())
          ? "true"
          : "false");
      }
      else if(event.getStateChange() == ItemEvent.SELECTED)
      {
        if(pageSelect == source)
        {
          djvuBean.setPageString(event.getItem().toString());
        }
        else if(zoomSelect == source)
        {
          final Object item = event.getItem();
          djvuBean.setZoom((item != null)
            ? item.toString()
            : null);
        }
        else if(fitWidth == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM_FIT_WIDTH);
        }
        else if(fitPage == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM_FIT_PAGE);
        }
        else if(actualSize == source)
        {
          djvuBean.setZoom(DjVuBean.ZOOM100);
        }
      }
      else if(
        (fitWidth == source)
        || (actualSize == fitWidth)
        || (fitPage == source))
      {
        updateZoom();
      }
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }
  }

  /**
   * Called to paint this container.
   *
   * @param g The graphics device to draw to.
   */
  public void paint(Graphics g)
  {
    Color c = g.getColor();
    g.setColor(getBackground());
    g.fill3DRect(0, 0, getSize().width - 1, getSize().height - 1, true);
    g.setColor(c);
    super.paint(g);
  }

  /**
   * Called when a DjVuBean property is changed.
   *
   * @param event describing the change
   */
  public void propertyChange(final PropertyChangeEvent event)
  {
    try
    {
      final String name = event.getPropertyName();

      if("text".equalsIgnoreCase(name))
      {
        final boolean oldValue = textMode.isEnabled();
        final boolean newValue =
          textMode.isSelected() || (djvuBean.getTextCodec(0L) != null);

        if(oldValue != newValue)
        {
          textMode.setEnabled(newValue);
          repaint(20L);
        }
      }
      else if("TargetWidth".equalsIgnoreCase(name))
      {
        updateTargetWidth(
          ((Number)event.getOldValue()).intValue(),
          ((Number)event.getNewValue()).intValue());
      }
      else if("zoom".equalsIgnoreCase(name))
      {
        updateZoom();
      }
      else if("page".equalsIgnoreCase(name))
      {
        updatePage((Integer)event.getNewValue());
      }
      else if("mode".equalsIgnoreCase(name))
      {
        updateMode(((Number)event.getNewValue()).intValue());
      }
      else if("propertyName".equalsIgnoreCase(name))
      {
        final String propertyName = (String)event.getNewValue();

        if("addOn.finder".equalsIgnoreCase(propertyName))
        {
          updateSearchMode();
        }
        else if("addOn.toolbar.page".equalsIgnoreCase(propertyName))
        {
          final boolean selected = DjVuBean.stringToBoolean(djvuBean.properties.getProperty(propertyName), false);
          if(selected)
          {
            pageSelect.requestFocus();
            djvuBean.properties.put(propertyName,"false");
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
   * Called to update the target width of the toolbar.
   *
   * @param targetWidth The target width for the toolbar. (-1 if unknown)
   * @param width The target width of the parent container.
   */
  public void updateTargetWidth(
    int targetWidth,
    int width)
  {
    if(
      (width > 0)
      && (width != targetWidth)
      && ((width < 750) || (targetWidth < 750)))
    {
      if((width < 480) || (targetWidth < 480))
      {
        final double scaleFactor =
          Math.min((double)(width - 60) / 400.0, 1.0);

        for(Enumeration e = buttonList.elements(); e.hasMoreElements();)
        {
          final ToggleButton button = (ToggleButton)e.nextElement();
          button.setScaleFactor(
            (button == logo2)
            ? (scaleFactor * scaleFactor)
            : scaleFactor);
          button.setSize(button.getPreferredSize());
        }

        for(Enumeration e = toggleButtonList.elements(); e.hasMoreElements();)
        {
          final ToggleButton button = (ToggleButton)e.nextElement();
          button.setScaleFactor(scaleFactor);
          button.setSize(button.getPreferredSize());
        }

//        pageSelect.setScaleFactor(scaleFactor);
//        zoomSelect.setScaleFactor(scaleFactor);
        Dimension s0 = pageSelect.getPreferredSize();

        if(s0.width > 60)
        {
          s0.width = 60;
        }

        setPreferredSizeComboBox(pageSelect, s0);
        pageSelect.setSize(s0);

        Dimension s1 = pageSelect.getPreferredSize();

        if(s1.width > 72)
        {
          s1.width = 72;
        }

        setPreferredSizeComboBox(zoomSelect, s1);
        zoomSelect.setSize(s1);
      }

      logo.setVisible(toolbar && (width > 500));
      logo2.setVisible((width > 220) && (width <= 500));

      if(width >= 750)
      {
        if(targetWidth < 750)
        {
          modeButtonPanel.removeAll();
          zoomPanel.removeAll();
          pagePanel.removeAll();
          modeButtonPanel.setLayout(new GridLayout(1, 4));
          modeButtonPanel.add(panMode);
          modeButtonPanel.add(zoomMode);
          modeButtonPanel.add(textMode);
          modeButtonPanel.add(searchMode);
          zoomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
          zoomPanel.add(actualSize);
          zoomPanel.add(fitWidth);
          zoomPanel.add(fitPage);
          zoomPanel.add(zoomIn);
          zoomPanel.add(zoomSelect);
          zoomPanel.add(zoomOut);
          pagePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
          pagePanel.add(firstPage);
          pagePanel.add(prevPage);
          pagePanel.add(pageSelect);
          pagePanel.add(nextPage);
          pagePanel.add(lastPage);
        }
      }
      else if(width < 750)
      {
        if((targetWidth <= 0) || (targetWidth >= 750))
        {
          modeButtonPanel.removeAll();
          zoomPanel.removeAll();
          pagePanel.removeAll();
          modeButtonPanel.setLayout(new GridLayout(2, 2));
          modeButtonPanel.add(panMode);
          modeButtonPanel.add(searchMode);
          modeButtonPanel.add(zoomMode);
          modeButtonPanel.add(textMode);

          Panel top = new Panel();
          top.setLayout(new GridLayout(1, 5));
          top.add(actualSize);
          top.add(fitWidth);
          top.add(fitPage);
          top.add(zoomIn);
          top.add(zoomOut);

          Panel bottom = new Panel();
          bottom.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 1));
          bottom.add(zoomSelect);
          zoomPanel.setLayout(new GridLayout(2, 1));
          zoomPanel.add(top);
          zoomPanel.add(bottom);
          top = new Panel();
          top.setLayout(new GridLayout(1, 5));
          top.add(firstPage);
          top.add(prevPage);
          top.add(nextPage);
          top.add(lastPage);

          bottom = new Panel();
          add(modeButtonPanel, BorderLayout.WEST);
          add(logo, BorderLayout.EAST);

//          bottom.setLayout(new FlowLayout(FlowLayout.RIGHT, 1, 1));
//          bottom.add(logo2);
//          bottom.add(pageSelect);
          bottom.setLayout(new BorderLayout());
          bottom.add(logo2, BorderLayout.WEST);
          bottom.add(pageSelect, BorderLayout.CENTER);
          pagePanel.setLayout(new GridLayout(2, 1));
          pagePanel.add(top);
          pagePanel.add(bottom);
        }
      }

      invalidate();
      validate();
    }
  }

  /**
   * Called to enable or disable the zoom buttons.
   *
   * @param value true if zoom buttons should be enabled.
   */
  protected void setZoomEnabled(boolean value)
  {
    zoomIn.setEnabled(value);
    zoomOut.setEnabled(value);
    fitWidth.setEnabled(value);
    fitPage.setEnabled(value);
    actualSize.setEnabled(value);
    zoomSelect.setEnabled(value);
    repaint(20L);
  }

  /**
   * Create a normal button using the specified arguments.  Children classes
   * can change the button properties by overloading this method.  For
   * example, if one wanted to replace the LizardTech logo with a  Foxtrot
   * Technologies logo one could use the following code:
   * <pre>
   *   class FoxtrotApplet extends com.lizardtech.djview.Applet
   *   {
   *     protected ToggleButton createButton(Object[] args)
   *     {
   *       if(args == LOGO_BUTTON)
   *       {
   *         final Object [] FOXTROTLOGO={
   *           "Foxtrot Technologies Inc.", 
   *           "FoxtrotTechnologies",
   *           "image/foxtrotlogo.djvu",
   *           new Dimension(24,24) }; 
   *         return super.createButton(FOXTROTLOGO);
   *       }
   *       return super.createButton(args);
   *     }
   *   }
   * </pre>
   *
   * @param args array consisting of tooltip, alt text, and getIcon argument.
   *
   * @return the newly created button.
   */
  protected ToggleButton createButton(final Object[] args)
  {
    return createButton(buttonList, args);
  }

  /**
   * Creates an editable combobox.
   *
   * @param args used to create the combobox.
   *
   * @return the newly created combobox.
   */
  protected Container createComboBox(final Object[] args)
  {
    final String tip =
      ((args.length > 0) && (args[0] != null))
      ? args[0].toString()
      : null;
    final String text =
      ((args.length > 1) && (args[1] != null))
      ? args[1].toString()
      : null;
    final Object    imageArg = args[2];
    final Dimension size =
      ((args.length > 3) && (args[3] instanceof Dimension))
      ? (Dimension)args[3]
      : new Dimension(-1, -1);

    if(classComboBox != ComboBox.class)
    {
      try
      {
        return (Container)classComboBox.newInstance();
      }
      catch(final Throwable exp) {}
    }

    return new ComboBox(tip, (imageArg == null)
      ? text
      : null, imageArg, size);
  }

  /**
   * Create a logo button using the specified arguments.  Children classes
   * can change the button properties by overloading this class.  For
   * example, if one wanted to replace the text used for Pan mode,  one
   * could use the following code:
   * <pre>
   *   class MyApplet extends com.lizardtech.djview.Applet
   *   {
   *     protected ToggleButton createToggleButton(Object[] args)
   *     {
   *       if(args == PAN_MODE_BUTTON)
   *       {
   *         final Object [] button={
   *           "Mouse Scroll",
   *           "MouseScroll",
   *           PAN_MODE_BUTTON[2],
   *           PAN_MODE_BUTTON[3]}; 
   *         return super.createToggleButton(button);
   *       }
   *       return super.createToggleButton(args);
   *     }
   *   }
   * </pre>
   *
   * @param args array consisting of tooltip, alt text, getIcon argument, and
   *        initial selection value.
   *
   * @return the newly created toggle button.
   */
  protected ToggleButton createToggleButton(final Object[] args)
  {
    final String tip =
      ((args.length > 0) && (args[0] != null))
      ? args[0].toString()
      : null;
    final String text =
      ((args.length > 1) && (args[1] != null))
      ? args[1].toString()
      : null;
    final Object    imageArg = args[2];
    final Dimension size =
      ((args.length > 3) && (args[3] instanceof Dimension))
      ? (Dimension)args[3]
      : new Dimension(-1, -1);
    final ToggleButton retval =
      new ToggleButton(
        tip,
        (imageArg == null)
        ? text
        : null,
        imageArg,
        size,
        false);

//    JToggleButton retval =
//      new ToggleButton((icon == null)
//        ? text
//        : null, icon, selected);
    retval.setActionCommand(tip);
    toggleButtonList.addElement(retval);

    return retval;
  }

  /**
   * Create an editor class which simply sends the edited value with the
   * specified string appended.
   *
   * @param select the combobox being edited.
   * @param appendString String to append.
   */
  protected void editorActionListener(
    final Container select,
    final String    appendString)
  {
    ActionListener listener =
      new ActionListener()
      {
        public void actionPerformed(ActionEvent event)
        {
          try
          {
            setSelectedItemComboBox(
              select,
              event.getActionCommand() + appendString);
          }
          catch(final Throwable exp)
          {
            exp.printStackTrace(DjVuOptions.err);
            System.gc();
          }
        }
      };

    setEditableComboBox(select, true);
    addActionListenerEditor(
      getEditorComboBox(select),
      listener);
  }

  private static void setEditableComboBox(
    final Container container,
    final boolean   value)
  {
    if(ComboBox.class.isAssignableFrom(container.getClass()))
    {
      ((ComboBox)container).setEditable(value);
    }
    else
    {
      final Object[] args = {new Boolean(value)};
      DjVuObject.invoke(setEditableMethod,container, args);  
    }
  }

  private static Object getEditorComboBox(final Container container)
  {
    return ComboBox.class.isAssignableFrom(container.getClass())?
      ((ComboBox)container).getEditor():
      DjVuObject.invoke(getEditorMethod,container, null);
  }


  private static void setPreferredSizeComboBox(
    final Container container,
    final Dimension size)
  {
    if(setPreferredSizeMethod != null)
    {
      try
      {
        final Object[] o = {size};
        setPreferredSizeMethod.invoke(container, o);
      }
      catch(final Throwable ignored) {}
    }
  }

  private static void setSelectedItemComboBox(
    final Container container,
    final Object    value)
  {
    if(ComboBox.class.isAssignableFrom(container.getClass()))
    {
      ((ComboBox)container).setSelectedItem(value);
    }
    else    
    {
      final Object[] args = {value};
      DjVuObject.invoke(setSelectedItemMethod,container, args);
    }
  }

  private static void addActionListenerEditor(
    final Object         editor,
    final ActionListener listener)
  {
    if(TextField.class.isAssignableFrom(editor.getClass()))
    {
      ((TextField)editor).addActionListener(listener);
    }
    else
    {
      final Object[] args = {listener};
      DjVuObject.invoke(addActionListenerMethod,editor, args);
    }
  }

  // Add a component to the toolbar, setting both the foreground and
  // background to the toolbar foreground and background.
  private void addComponent(
    Container          container,
    Component          comp,
    GridBagLayout      gridBag,
    GridBagConstraints c)
  {
    if(comp != null)
    {
      comp.setBackground(getBackground());
      comp.setForeground(getForeground());
      gridBag.setConstraints(comp, c);
      container.add(comp);
    }
  }

  private static void addItemComboBox(
    final Container container,
    final Object    value)
  {
    if(ComboBox.class.isAssignableFrom(container.getClass()))
    {
      ((ComboBox)container).addItem(value);
    }
    else
    {
      final Object[] args = {value};
      DjVuObject.invoke(addItemMethod,container, args);
    }
  }

  private static void addItemListenerComboBox(
    final Container    container,
    final ItemListener listener)
  {
    if(ComboBox.class.isAssignableFrom(container.getClass()))
    {
      ((ComboBox)container).addItemListener(listener);
    }
    else
    {
      final Object[] args = {listener};
      DjVuObject.invoke(addItemListenerMethod,container, args);
    }
  }

  private static void removeItemListenerComboBox(
    final Container    container,
    final ItemListener listener)
  {
    if(ComboBox.class.isAssignableFrom(container.getClass()))
    {
      ((ComboBox)container).removeItemListener(listener);
    }
    else
    {
      final Object[] args = {listener};
      DjVuObject.invoke(removeItemListenerMethod,container,args);
    }
  }

  // Called to update the display mode buttons.
  private void updateMode(final int mode)
  {
//    try { throw new Exception("updateMode("+mode+")"); } catch(final Throwable exp) { exp.printStackTrace(DjVuOptions.err); }
    switch(mode)
    {
      case DjVuBean.TEXT_MODE :
        textMode.setSelected(true);

        if(panMode.isSelected())
        {
          panMode.setSelected(false);
        }

        if(zoomMode.isSelected())
        {
          zoomMode.setSelected(false);
        }

        setZoomEnabled(false);

        break;
      case DjVuBean.ZOOM_MODE :
        zoomMode.setSelected(true);

        if(panMode.isSelected())
        {
          panMode.setSelected(false);
        }

        if(textMode.isSelected())
        {
          textMode.setSelected(false);
        }

        setZoomEnabled(true);

        break;
      case DjVuBean.PAN_MODE :default :
        panMode.setSelected(true);

        if(zoomMode.isSelected())
        {
          zoomMode.setSelected(false);
        }

        if(textMode.isSelected())
        {
          textMode.setSelected(false);
        }

        setZoomEnabled(true);

        break;
    }
  }

  // Called to update the page buttons.
  private void updatePage(final Integer pagenoNumber)
  {
    final int pageno = pagenoNumber.intValue();
    removeItemListenerComboBox(pageSelect, this);

    try
    {
      setSelectedItemComboBox(pageSelect, pagenoNumber);
    }
    finally
    {
      addItemListenerComboBox(pageSelect, this);
    }

    final int documentSize = djvuBean.getDocumentSize();
    firstPage.setEnabled(pageno != 1);
    prevPage.setEnabled(pageno != 1);
    lastPage.setEnabled(pageno != documentSize);
    nextPage.setEnabled(pageno != documentSize);
    textMode.setEnabled(
      textMode.isSelected()
      || !(djvuBean.isDecoding() || (djvuBean.getTextCodec(0L) == null)));
  }

  private void updateSearchMode()
  {
    String value = djvuBean.properties.getProperty("addOn.finder");

    if(value != null)
    {
      searchMode.setEnabled(true);

      boolean selected = DjVuBean.stringToBoolean(value, false);
      searchMode.setSelected(selected);

      if(!selected)
      {
        djvuBean.setSearchMask(-1);
      }
    }
    else
    {
      searchMode.setEnabled(false);
    }
  }

  // Called to update the zoom mode buttons
  private void updateZoom()
  {
    final String item = djvuBean.getZoom();
    setSelectedItemComboBox(zoomSelect, item);

    if(DjVuBean.ZOOM100.equals(item))
    {
      actualSize.setSelected(true);
      fitWidth.setSelected(false);
      fitPage.setSelected(false);
    }
    else if(DjVuBean.ZOOM_FIT_WIDTH.equals(item))
    {
      fitWidth.setSelected(true);
      fitPage.setSelected(false);
      actualSize.setSelected(false);
    }
    else if(DjVuBean.ZOOM_FIT_PAGE.equals(item))
    {
      fitPage.setSelected(true);
      fitWidth.setSelected(false);
      actualSize.setSelected(false);
    }
    else
    {
      fitPage.setSelected(false);
      fitWidth.setSelected(false);
      actualSize.setSelected(false);
    }
  }
}
