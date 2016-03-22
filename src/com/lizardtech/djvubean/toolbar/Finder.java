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
 * @version $Revision: 1.12 $
 */
public class Finder
  extends Container
  implements ActionListener, PropertyChangeListener
{
  //~ Static fields/initializers ---------------------------------------------

  /** Arguments used to create the first page button. */
  public static final Object[] SEARCH_DOCUMENT_BACK_BUTTON =
  {"Search To First Page", "<<", "searchbackdoc.djvu", new Dimension(24, 24)};

  /** Arguments used to create the last page button. */
  public static final Object[] SEARCH_DOCUMENT_FWD_BUTTON =
  {"Search To Last Page", ">>", "searchfwddoc.djvu", new Dimension(24, 24)};

  /** Arguments used to create the next page button. */
  public static final Object[] SEARCH_FORWARD_BUTTON =
  {"Search Forward", "?>", "searchfwd.djvu", new Dimension(24, 24)};

  /** Arguments used to create the previous page button. */
  public static final Object[] SEARCH_BACK_BUTTON =
  {"Search Backwards", "<?", "searchback.djvu", new Dimension(24, 24)};

  private static final Class  classTextField;
  private static final Method addActionListenerMethod;
  private static final Method getTextMethod;
  private static final Method setColumnsMethod;
  private static final Method setTextMethod;
  
  static
  {
    Class xclassTextField = null;
    Method xaddActionListenerMethod;
    Method xgetTextMethod;
    Method xsetColumnsMethod;
    Method xsetTextMethod;
    try
    {
      xclassTextField          = Class.forName("javax.swing.JTextField");
      final Class[] c1         = {ActionListener.class};
      xaddActionListenerMethod = xclassTextField.getMethod("addActionListener", c1);
      xgetTextMethod           = xclassTextField.getMethod("getText", null);
      final Class[] c2         = {Integer.TYPE};
      xsetColumnsMethod        = xclassTextField.getMethod("setColumns", c2);
      final Class[] c3         = {String.class};
      xsetTextMethod           = xclassTextField.getMethod("setText", c3);
    }
    catch(final Throwable exp)
    {
      xclassTextField=TextField.class;
      xaddActionListenerMethod=null;
      xgetTextMethod=null;
      xsetColumnsMethod=null;
      xsetTextMethod=null;
    }
    classTextField=xclassTextField;
    addActionListenerMethod=xaddActionListenerMethod;
    getTextMethod=xgetTextMethod;
    setColumnsMethod=xsetColumnsMethod;
    setTextMethod=xsetTextMethod;
  }

  //~ Instance fields --------------------------------------------------------

  /** A text field for the string we are searching for. */
  protected Component searchField;

  /** The search backward button. */
  protected final ToggleButton searchBack;

  /** The button to search to the last page. */
  protected final ToggleButton searchDocumentBack;

  /** The button to search to the first page. */
  protected final ToggleButton searchDocumentFwd;

  /** The search forward button. */
  protected final ToggleButton searchFwd;

  /** The previous search button pressed. */
  protected ToggleButton lastButton=null;
  
  // DjVuBean which will be searched.
  private DjVuBean djvuBean = null;

  // Vector of all buttons used by the finder bar.
  private Vector buttonList = new Vector();

  // True if not initialized
  private boolean firstTime = true;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new Finder object.
   *
   * @param bean the DjVuBean being monitored and controled.
   */
  public Finder(final DjVuBean bean)
  {
    searchField=null;
    if(classTextField != TextField.class)
    {
      try
      {
        searchField = (Component)classTextField.newInstance();
      }
      catch(final Throwable exp) {}
    }
    if(searchField == null)
    {
      searchField = new TextField();
    }

    searchBack           = createButton(SEARCH_BACK_BUTTON);
    searchFwd            = createButton(SEARCH_FORWARD_BUTTON);
    searchDocumentBack   = createButton(SEARCH_DOCUMENT_BACK_BUTTON);
    searchDocumentFwd    = createButton(SEARCH_DOCUMENT_FWD_BUTTON);

    setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
    add(searchDocumentBack);
    add(searchBack);
    add(searchField);
    add(searchFwd);
    add(searchDocumentFwd);
    djvuBean = bean;
    setBackground(Toolbar.LIGHT_GRAY);
    setForeground(Toolbar.BLACK);

    for(Enumeration e = buttonList.elements(); e.hasMoreElements();)
    {
      final ToggleButton button = (ToggleButton)e.nextElement();
      button.setBackground(Toolbar.LIGHT_GRAY);
      button.setForeground(Toolbar.BLACK);
      button.addActionListener(this);
    }

    final int documentSize = bean.getDocumentSize();

    if(TextField.class.isAssignableFrom(searchField.getClass()))
    {
      ((TextField)searchField).addActionListener(this);
    }
    else
    {
      final Object[] args = {this};
      DjVuObject.invoke(addActionListenerMethod,searchField, args);
    }
    
    //searchField.addActionListener(this);
    searchDocumentFwd.setVisible(documentSize > 1);
    searchDocumentBack.setVisible(documentSize > 1);
    lastButton=searchFwd;
    String text=bean.getSearchText();
    if((text == null)||(text.trim().length() == 0))
    {
      bean.properties.put("addOn.finder", "false");
    }
    else
    {
      if(TextField.class.isAssignableFrom(searchField.getClass()))
      {
        ((TextField)searchField).setText(text);
      }
      else
      {
        final Object[] args = {text};
        DjVuObject.invoke(setTextMethod,searchField, args);
      }      
      bean.properties.put("addOn.finder", "true");
    }
    bean.addPropertyChangeListener(this);
    updatePage(new Integer(bean.getPage()));
    updateTargetWidth(
      -1,
      bean.getTargetWidth());
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Set the size of the Window.  This is used to determine how the toolbar
   * should be layed out.
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
   * Set the visible property of this component.
   *
   * @param value true if visible.
   */
  public void setVisible(boolean value)
  {
    if(value)
    {
      value = (djvuBean.getTextSearchObject() != null);
    }

    if(isVisible() != value)
    {
      final int mode = djvuBean.getMode();

      if(mode == DjVuBean.TEXT_MODE)
      {
        djvuBean.setMode(djvuBean.getLastMode());
      }

      djvuBean.setCaretPosition(-1);
      super.setVisible(value);
      invalidate();
      djvuBean.recursiveRevalidate();

      if(mode == DjVuBean.TEXT_MODE)
      {
        djvuBean.setMode(mode);
      }
      if(value)
      {
        searchField.requestFocus();
      }
      else
      {
        djvuBean.requestFocus();
      }
    }
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

      final ToggleButton last=lastButton;
      if(source instanceof ToggleButton)
      {
        lastButton=(ToggleButton)source;
        if(searchDocumentFwd == source)
        {
          searchDocument(true);
        }
        else if(searchBack == source)
        {
          searchPage(false);
        }
        else if(searchFwd == source)
        {
          searchPage(true);
        }
        else if(searchDocumentBack == source)
        {
          searchDocument(false);
        }
        else
        {
          lastButton=last;
        }
      }
      else if(searchField == source)
      {
        final String text = event.getActionCommand();
        if((text != null) && !text.trim().equals(djvuBean.getSearchText()))
        {
          djvuBean.setSearchText(null);
        }
        if(last != null)
        {
          last.setSelected(true);
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
   * Called to paint this container.
   *
   * @param g The graphics device to draw to.
   */
  public void paint(Graphics g)
  {
    if(firstTime)
    {
      firstTime = false;
      setVisible(
        DjVuBean.stringToBoolean(
          djvuBean.properties.getProperty("addOn.finder"),
          false));
      final String text=(TextField.class.isAssignableFrom(searchField.getClass()))?
        ((TextField)searchField).getText():  
        (String)DjVuObject.invoke(getTextMethod,searchField, null);
      if((text != null)&&(text.trim().length() != 0))
      {
        searchDocumentFwd.setSelected(true);
      }
    }

    if(!isVisible())
    {
      invalidate();
      djvuBean.recursiveRevalidate();
    }
    else
    {
      final Color c = g.getColor();
      g.setColor(getBackground());
      g.fill3DRect(0, 0, getSize().width - 1, getSize().height - 1, true);
      g.setColor(c);
      super.paint(g);
    }
  }

  /**
   * Called with a DjVuBean property has changed.
   *
   * @param event the PropertyChangeEvent.
   */
  public void propertyChange(final PropertyChangeEvent event)
  {
    try
    {
      final String name = event.getPropertyName();

      if("TargetWidth".equalsIgnoreCase(name))
      {
        updateTargetWidth(
          ((Number)event.getOldValue()).intValue(),
          ((Number)event.getNewValue()).intValue());
      }
      else if(name.equals("page"))
      {
        updatePage((Integer)event.getNewValue());
      }
      else if("propertyName".equalsIgnoreCase(name))
      {
        final String propertyName = (String)event.getNewValue();

        if("addOn.finder".equalsIgnoreCase(propertyName))
        {
          setVisible(
            DjVuBean.stringToBoolean(
              djvuBean.properties.getProperty(propertyName),
              false));
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
   * Search for text forward or backwards from the last Caret Position from
   * the current page.
   *
   * @param searchText text to search for.
   * @param forward true if searching forward, otherwise search backward.
   * @param matchCase true if case sensative.
   * @param wholeWord true if a whole word match is required.
   * @param searchDocument true if a whole document should be searched.
   *
   * @return True if the text was found.
   */
  public boolean search(
    final String  searchText,
    final boolean forward,
    final boolean matchCase,
    final boolean wholeWord,
    final boolean searchDocument)
  {
    djvuBean.setSearchText(searchText);
    djvuBean.setSearchMask(
      (forward
      ? 0
      : DjVuBean.SEARCH_BACKWARD_MASK)
      | (matchCase
      ? DjVuBean.MATCH_CASE_MASK
      : 0) | (wholeWord
      ? DjVuBean.WHOLE_WORD_MASK
      : 0) | (searchDocument
      ? DjVuBean.WHOLE_DOCUMENT_MASK
      : 0));

    final Runnable textSearch = djvuBean.getTextSearchObject();

    if(textSearch != null)
    {
      textSearch.run();
    }

    final boolean retval = (djvuBean.getCaretPosition() != -1);

    if(!retval)
    {
      djvuBean.properties.put("addOn.finder", "false");
    }
    else
    {
      final TextArea textArea = djvuBean.getTextArea();

      if(textArea.isVisible())
      {
        textArea.requestFocus();
      }
    }

    return retval;
  }

  /**
   * Called to do a search on the document.
   *
   * @param forward true if the search should be forward.
   *
   * @return the position where the text was found, or -1 if no text was
   *         found.
   */
  public boolean searchDocument(final boolean forward)
  {
    final  String text=
      (TextField.class.isAssignableFrom(searchField.getClass()))?
        ((TextField)searchField).getText():  
        (String)DjVuObject.invoke(getTextMethod,searchField, null);

    //final String  text      = searchField.getText();
    final String  textTrim  = text.trim();
    final boolean matchCase = !textTrim.equals(textTrim.toLowerCase());
    final boolean wholeWord = !text.equals(textTrim);

    return search(textTrim, forward, matchCase, wholeWord, true);
  }

  /**
   * Called to do a search on the current page.
   *
   * @param forward true if the search should be forward.
   *
   * @return the position where the text was found, or -1 if no text was
   *         found.
   */
  public boolean searchPage(final boolean forward)
  {
    final  String text=
      (TextField.class.isAssignableFrom(searchField.getClass()))?
        ((TextField)searchField).getText():  
        (String)DjVuObject.invoke(getTextMethod,searchField, null);

//    final String  text      = searchField.getText();
    final String  textTrim  = text.trim();
    final boolean matchCase = !textTrim.equals(textTrim.toLowerCase());
    final boolean wholeWord = !text.equals(textTrim);

    return search(textTrim, forward, matchCase, wholeWord, false);
  }

  /**
   * Called when the "TargetWidth" value has been updated.
   *
   * @param targetWidth old target width.
   * @param width new target width.
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

        final int wmax=(int)((5 * ((Dimension)SEARCH_DOCUMENT_BACK_BUTTON[3]).width) * scaleFactor);
        if(TextField.class.isAssignableFrom(searchField.getClass()))
        {
          for(int i = 30; i > 5; i--)
          {
            ((TextField)searchField).setColumns(i);  
            if(
              searchField.getPreferredSize().width <= wmax)
            {
              break;
            }
          }
        }
        else
        {
          for(int i = 30; i > 5; i--)
          {
            final Object[] args = {new Integer(i)};
            DjVuObject.invoke(setColumnsMethod,searchField, args);
            if(
              searchField.getPreferredSize().width <= wmax)
            {
              break;
            }
          }
        }

        for(Enumeration e = buttonList.elements(); e.hasMoreElements();)
        {
          final ToggleButton button = (ToggleButton)e.nextElement();
          button.setScaleFactor(scaleFactor);
          button.setSize(button.getPreferredSize());
        }
      }
    }
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
    return Toolbar.createButton(buttonList, args);
  }

  // Called to update the page buttons.
  private void updatePage(final Integer pagenoNumber)
  {
    final int documentSize = djvuBean.getDocumentSize();
    searchDocumentBack.setEnabled(documentSize != 1);
    searchDocumentFwd.setEnabled(documentSize != 1);

    final boolean enable =
      djvuBean.isDecoding() || (djvuBean.getTextCodec(0L) != null);
    searchBack.setEnabled(enable);
    searchFwd.setEnabled(enable);
  }
}
