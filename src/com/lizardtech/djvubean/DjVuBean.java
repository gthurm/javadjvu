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
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/**
 * This Panel is designed for rendering a DjVuImage.  Since normally  a
 * DjVuImage requires too much memory to render at full resolution, this
 * panel should be added to either a JScrollPane or a ScrollPane. The
 * borders of that pane will be used to automatically segment the DjVuImage,
 * to avoid excessive memory usage.
 *
 * @author $author$
 * @version $Revision: 1.43 $
 */
public class DjVuBean
  extends Panel
  implements PropertyChangeListener, DjVuInterface, java.io.Serializable, Runnable
{
  //~ Static fields/initializers ---------------------------------------------

  // Just an empty string. 
  private static final String NILL = "";

  /** String used for scrolling up. */
  public static final String SCROLL_UP = "up";

  /** String used for scrolling down. */
  public static final String SCROLL_DOWN = "down";

  /** String used for scrolling left. */
  public static final String SCROLL_LEFT = "left";

  /** String used for scrolling right. */
  public static final String SCROLL_RIGHT = "right";

  /** String used for scrolling page up. */
  public static final String SCROLL_PAGE_UP = "page up";

  /** String used for scrolling down. */
  public static final String SCROLL_PAGE_DOWN = "page down";

  /** Magic scale value used to zoom to fit width. */
  public static final int FIT_WIDTH = -1;

  /** Magic scale value used to zoom to fit page. */
  public static final int FIT_PAGE = -2;

  /** Constant to switch to single page layout. */
  public static final int SINGLE_PAGE_LAYOUT = 0;

  /** Constant to switch to book layout. */
  public static final int BOOK_PAGE_LAYOUT = 1;

  /** Constant to switch to book with coverpage layout. */
  public static final int COVER_PAGE_LAYOUT = 2;

  /** Constant to switch to the previous mode. */
  public static final int LAST_MODE = 0;

  /** Constant to switch to panning mode. */
  public static final int PAN_MODE = 1;

  /** Constant to switch to text mode. */
  public static final int TEXT_MODE = 2;

  /** Constant to switch to zoom mode. */
  public static final int ZOOM_MODE = 3;

  /** String used for selecting 300% zoom. */
  public static final String ZOOM300 = "300%";

  /** String used for selecting 150% zoom. */
  public static final String ZOOM150 = "150%";

  /** String used for selecting 100% zoom. */
  public static final String ZOOM100 = "100%";

  /** String used for selecting 75% zoom. */
  public static final String ZOOM75 = "75%";

  /** String used for selecting 50% zoom. */
  public static final String ZOOM50 = "50%";

  /** String used for selecting 25% zoom. */
  public static final String ZOOM25 = "25%";

  /** String used for selecting fit width zoom. */
  public static final String ZOOM_FIT_WIDTH = "Fit Width";

  /** String used for selecting fit page zoom. */
  public static final String ZOOM_FIT_PAGE = "Fit Page";

  /** String used for selecting fit one to one zoom. */
  public static final String ZOOM_ONE_TO_ONE = "One to One";

  /** String used for selecting stretch zoom */
  public static final String ZOOM_STRETCH = "Stretch";

  /** String used for selecting zoom in */
  public static final String ZOOM_IN = "Zoom In";

  /** String used for selecting zoom out */
  public static final String ZOOM_OUT = "Zoom Out";

  /** The list of strings standard zoom settings. */
  public static final String[] ZOOM_STANDARD_LIST =
  {ZOOM300, ZOOM150, ZOOM100, ZOOM75, ZOOM50, ZOOM25};

  /** The list of strings for the setZoom() method. */
  public static final String[] ZOOM_SPECIAL_LIST =
  {ZOOM_FIT_WIDTH, ZOOM_FIT_PAGE, ZOOM_ONE_TO_ONE};

  /** The list of strings for the setZoom() method. */
  public static final String[] ZOOM_BUTTON_LIST = {ZOOM_IN, ZOOM_OUT};

  /** Navigate to the first page. */
  public static final String FIRST_PAGE = "First Page";

  /** Navigate to the previous page. */
  public static final String PREV_PAGE = "Previous Page";

  /** Navigate to the next page. */
  public static final String NEXT_PAGE = "Next Page";

  /** Navigate to the last page. */
  public static final String LAST_PAGE = "Last Page";

  /** This is a list of special values used when navigating documents. */
  public static final String[] NAVIGATE_LIST =
  {FIRST_PAGE, PREV_PAGE, NEXT_PAGE, LAST_PAGE};

  /** String used for selecting SINGLE page display. */
  public static final String SINGLE="single";

  /** String used for selecting book display. */
  public static final String BOOK="book";

  /** String used for selecting book with cover page display. */
  public static final String COVER="cover";
  
  /** The list of strings page layout settings. */
  public static final String[] PAGE_LAYOUT_LIST =
  {SINGLE, BOOK, COVER};

  /** This mask is used for backwards searches. */
  public static final int SEARCH_BACKWARD_MASK = 0x1;

  /** This mask is used for case sensative searches. */
  public static final int MATCH_CASE_MASK = 0x2;

  /** This mask is used for whole word searches. */
  public static final int WHOLE_WORD_MASK = 0x4;

  /** This mask is used for multiple document searches. */
  public static final int WHOLE_DOCUMENT_MASK = 0x8;
  
  public static final boolean NEED_PAINT_STATUS = System.getProperty("java.version").startsWith("1.3.");

  //~ Instance fields --------------------------------------------------------

  // Keeps track of the current status
  private String status=null;
  
  /**
   * Properties which may be used to initialize addOn's, and to pass extra
   * values between add-on's.
   */
  public final Properties properties;

  /** The current mouse listener. */
  protected MouseListener mlistener = null;
  
  /** Current zoom factor. */
  protected int zoom = 0;

  // Object for holding the DjVuOptions
  private DjVuObject djvuObject = new DjVuObject();

  // Used to propigate change events
  private final PropertyChangeSupport change;

  // The outline navigation
  private Component outline = null;

  // The toolbar bar.
  private Component toolBar = null;

  // The annoManager.
  private Object annoManager = null;
  
  // The annoManager drawMapAreaMethod
  private Method drawMapAreaMethod=null;

  /** The DjVuImage to be displayed. */
  protected DjVuImage image = null;

  // For simple predecoding ...
  private DjVuPage nextPage = null;

  // For simple predecoding ...
  private DjVuPage prevPage = null;

  // Document being displayed
  private Document document = null;

  // index used for text searches
  private int caretIndex = -1;

  // position used for text searches
  private int caretPosition = -1;

  // Current page number being viewed
  private Integer page = new Integer(1);

  // current search mask.
  private Integer searchMask = new Integer(0);

  // previous display mode
  private Number lastMode = new Integer(-1);

  // current display mode
  private Number mode = new Integer(ZOOM_MODE);

  // current display layout
  private Number pageLayout = new Integer(SINGLE_PAGE_LAYOUT);

  // current select bounds
  private Rectangle select = new Rectangle();

  // object for performing searches 
  private Runnable textSearchObject = null;

//  // Temporarily store the results of getZoom().  Used inside setZoom().
  private String oldZoom = null;

  // string to search for
  private String searchText = null;

  // TextArea for displaying textCodec data.
  private final TextArea textArea = new TextArea();

  // This corresponds to the current page.
  private int currentIndex=0;
  
  // the target width for toolbar type objects
  private Number targetWidth = new Integer(-1);

  // the hidden text for this page
  private String text = null;

  // The url being browsed.
  private URL url = null;

  // Causes repaint requests to be requeued.
  private boolean defereRepaint = false;

  // When 0 causes the page to be cleared before display
  private long clearPage = 0;

  // Used for initializations
  private boolean firstTime = true;

  // old viewport sizes
  private int oViewportHeight = 0;

  // old viewport sizes
  private int oViewportWidth = 0;
  
  /** Thread used to get the current image for display */
  protected Thread getImageThread=null;
    
  // the offset of the first page
  private int pageOffset=0;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new DjVuBean object.
   */
  public DjVuBean()
  {
    properties =
      new Properties()
        {
          public Object put(
            final Object name,
            final Object value)
          {
            Object retval = getProperty((String)name);

            if(!value.equals(retval))
            {
              retval = super.put(name, (String)value);
              setPropertyName((String)name);
            }

            return retval;
          }
        };
    change = new PropertyChangeSupport(this);
    textArea.setEditable(false);
    setMode(PAN_MODE);
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Set the status string and fire a property change event "status".
   *
   * @param status new status string
   */
  public void setStatus(final String status)
  {
    final String s=this.status;
    this.status=status;
    change.firePropertyChange("status", s, status);
  }

  /**
   * Query the status string.
   *
   * @return the status string
   */
  public String getStatus()
  {
      return status;
  }

  /**
   * Sets the background color of this component.
   *
   * @param color The color to become the components background color.  If
   *        null the component will inherit the background color of its
   *        parent.
   */
  public void setBackground(final Color color)
  {
    textArea.setBackground(color);
    super.setBackground(color);
  }

  /**
   * Query the current caret index used for text searches.  -1 may be used
   * to reset to the first visible page for forward searches and the 
   * last visible page for backward searches.
   *
   * @return the current search index.
   */
  public int getCaretIndex()
  {
      return caretIndex;
  }
  
  /**
   * Query the current caret position used for text searches.  -1 may be
   * used to reset to the beginning of the document for forward searches and
   * the end of the document for backward searches.
   *
   * @return the current search position.
   */
  public int getCaretPosition()
  {
    return caretPosition;
  }

  /**
   * Query the resolution of the DjVuPage.
   *
   * @return The scanned resolution of the DjVuPage.
   */
  public int getDPI()
  {
    return getImageWait().getMaxDPI();
  }

  /**
   * Set the DjVuOptions used by this object.
   *
   * @param options The DjVuOptions used by this object.
   */
  public void setDjVuOptions(final DjVuOptions options)
  {
    djvuObject.setDjVuOptions(options);
  }

  /**
   * Query the DjVuOptions used by this object.
   *
   * @return the DjVuOptions used by this object.
   */
  public DjVuOptions getDjVuOptions()
  {
    return djvuObject.getDjVuOptions();
  }

  /**
   * Query the document being browsed.
   *
   * @return the document being browsed.
   */
  public final Document getDocument()
  {
    return document;
  }

  /** 
   * Query the number of pages to show at once.  Currently only the values of 1 or 2 are supported.
   *
   * @return number of pages to display.
   */
  public int getVisiblePageCount()
  {
    return ((this.mode.intValue() == TEXT_MODE)||(this.pageLayout.intValue() == SINGLE_PAGE_LAYOUT))?1:2;
  }
  
  /** 
   * Query the page offset.  Use a value of 1 to start bookmode on an even page number.
   * 
   * @return offset
   */
  public int getPageOffset()
  {
    return (this.pageLayout.intValue() == COVER_PAGE_LAYOUT)?1:0;
  }
  
  /**
   * Query the current DjVuImage being displayed.
   *
   * @return the DjVuImage being displayed.
   */
  public DjVuImage getImageWait()
  {
    DjVuPage [] pageArray=null;
    for(;;)
    {
      final int page=this.page.intValue();
      int zoom=this.zoom;
      DjVuImage retval = image;
      if((retval == null) && (document != null))
      {
//        final long lockTime=System.currentTimeMillis();
        synchronized(this)
        {
          currentIndex=0;
          retval = image;

          if(retval == null)
          {
            final int step=getVisiblePageCount();
            final int offset=getPageOffset()%step;
            final int xpage=page-((step+page-offset-1)%step);
            if(pageArray == null || pageArray.length != step)
            {
              pageArray=new DjVuPage[step];
              for(int i=0;i<step;i++)
              {
                pageArray[i]=null;
              }
            }
            try
            {
              int i=0;
              for(int p=xpage-1;i<step;i++,p++)
              {
                if(p < document.size())
                {
                  if(p >= 0)
                  {
                    pageArray[i]=document.getPage(p, DjVuPage.MAX_PRIORITY,false);
                    if(p == this.page.intValue()-1)
                    {
                      currentIndex=i;
                    }
                  }
                  else
                  {
                    pageArray[i]=null;
                  }
                }
                else
                {
                  int j=i;
                  do
                  {
                    pageArray[j++]=null;  
                  } while(j<step);
                  break;
                }
              }
              while(i-- > 0)
              {
                if((i+xpage > offset)&&(pageArray[i] == null))
                {
                  break;
                }
              }
              if(i >= 0)
              {
                try { Thread.sleep(20L); } catch( final Throwable ignored) {}
                continue;
              }
//              if(p.waitForCodec(p.infoLock,500L) == null)
//              {
//                continue;
//              }
              retval   = new DjVuImage(pageArray,
                          stringToBoolean(properties.getProperty("zoomfast"), false),
                          zoom,getViewportSize());
              if(zoom != this.zoom || page != this.page.intValue())
              {
                continue;
              }
              setImage(retval);
              final Rectangle bounds=retval.getBounds();
              setImageSize(bounds.width, bounds.height);
              setScrollPosition(0, 0);
//            final int scale = zoom;
//            zoom = 0;
//            zoom(scale);
              propertyChange(null);
            }
            catch(final IOException exp)
            {
              exp.printStackTrace(DjVuOptions.err);
              System.gc();
            }
          }
//          DjVuObject.checkLockTime(lockTime,10000);
        }
      }
      if((zoom == this.zoom)&&(page == this.page.intValue()))
      {
        return retval;
      }
    }
  }

  /**
   * Set the scaled size of the DjVuImage.
   *
   * @param width scaled Width.
   * @param height scaled Height.
   */
  public void setImageSize(
    int width,
    int height)
  {
    //DjVuOptions.err.println("1. setImageSize "+width+","+height);
    DjVuImage       image     = getImageWait();
    final Dimension imageSize = image.getSize();
    final int       owidth    = imageSize.width;
    final int       oheight   = imageSize.height;

    if((width != owidth) || (height != oheight))
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(this)
      {
        //zoom    = 0;
        image   = image.getScaledInstance(width, height);

        if(image == null)
        {
          image = getImageWait();
        }

        width    = image.getBounds().width;
        height   = image.getBounds().height;
        setImage(image);
        setPanelSize(image.getSize());
        scaleScrollPosition(owidth, oheight, width, height);
//        DjVuObject.checkLockTime(lockTime,10000);
      }

      recursiveRevalidate();
    }
  }

  /**
   * Set the scaled DjVuImage size.
   *
   * @param size Scaled size.
   */
  public final void setImageSize(final Dimension size)
  {
    setImageSize(size.width, size.height);
  }

  /**
   * Query the previous mode value.  Usefull for "undo" type operations.
   *
   * @return the previous mode setting.
   */
  public Number getLastMode()
  {
    return lastMode;
  }

  /**
   * Query the maximum size.
   *
   * @return The scaled Image size.
   */
  public Dimension getMaximumSize()
  {
    return getImageWait().getSize();
  }

  /**
   * Query the minimum size.
   *
   * @return minimum size of the panel.
   */
  public Dimension getMinimumPanelSize()
  {
    return super.getMinimumSize();
  }

  /**
   * Query the minimum size.
   *
   * @return The scaled Image size.
   */
  public Dimension getMinimumSize()
  {
    return getImageWait().getSize();
  }

  /**
   * Set the layout from the specified String or Number. This method
   * fires a "pagelayout" PropertyChangeEvent.
   *
   * @param layout object containing either a layout number or layout string.
   */
  public void setPageLayout(Object layout)
  {
      if(layout instanceof Number)
      {
        setPageLayout(((Number)layout).intValue());
      }
      else if(layout instanceof String)
      {
          if(BOOK.equalsIgnoreCase((String)layout))
          {
              setPageLayout(BOOK_PAGE_LAYOUT);
          }
          else if(COVER.equalsIgnoreCase((String)layout))
          {
              setPageLayout(COVER_PAGE_LAYOUT);
          }
          else if(SINGLE.equalsIgnoreCase((String)layout))
          {
              setPageLayout(SINGLE_PAGE_LAYOUT);
          }
          else 
          {
            try
            {
              setPageLayout(Integer.parseInt((String)layout));
            }
            catch(final Throwable ignored) {}
          }
      }
  }
  
  /**
   * Set the layout value. This method fires a "pagelayout" PropertyChangeEvent.
   *
   * @param layout value to use
   */
  private void setPageLayout(final int layout)
  {
    if(layout != this.pageLayout.intValue())
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(this)
      {
        if(layout != this.pageLayout.intValue())
        {
          final Number oldLayout = this.pageLayout;
          this.pageLayout=new Integer(layout);
          change.firePropertyChange("pagelayout", oldLayout, this.pageLayout);
          setImage(null);
          getImage();
          repaint(50L);
        }
      }
    }
  }
  
  /**
   * Set the display mode from the specified String or Number. This method
   * fires a "mode" PropertyChangeEvent.
   *
   * @param mode object containing eithe a mode number or mode string.
   */
  public void setMode(Object mode)
  {
    if(mode instanceof Number)
    {
      setMode(((Number)mode).intValue());
    }
    else if(mode instanceof String)
    {
      if("pan".equalsIgnoreCase((String)mode))
      {
        setMode(PAN_MODE);
      }
      else if("zoom".equalsIgnoreCase((String)mode))
      {
        setMode(ZOOM_MODE);
      }
      else if("text".equalsIgnoreCase((String)mode))
      {
        setMode(TEXT_MODE);
      }
      else
      {
        try
        {
          setMode(Integer.parseInt((String)mode));
        }
        catch(final Throwable ignored) {}
      }
    }
  }

  /**
   * Used to set the mouselistener for PAN_MODE (panning), or ZOOM_MODE (zoom
   * selection).  Other values such as  TEXT_MODE may be used to indicate no
   * internal mouse listener (except for the mouse over hyperlink listener)
   * should be used. This method fires a "mode" PropertyChangeEvent.
   *
   * @param mode the new mouse listening mode.
   */
  public void setMode(int mode)
  {
    if(mode != this.mode.intValue())
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(this)
      {
        if(mode != this.mode.intValue())
        {
          final Number oldMode = this.mode;
          this.mode = (mode != LAST_MODE)
            ? (new Integer(mode))
            : getLastMode();

          switch(this.mode.intValue())
          {
            case ZOOM_MODE :
            {
              if(!(mlistener instanceof ZoomMode))
              {
                if(mlistener != null)
                {
                  removeMouseListener(mlistener);
                }

                mlistener = new ZoomMode(this);
                if(this.image != null)
                {
                  addMouseListener(mlistener);
                }
              }

              break;
            }
            case PAN_MODE :
            {
              if(!(mlistener instanceof PanMode))
              {
                if(mlistener != null)
                {
                  removeMouseListener(mlistener);
                }

                mlistener = new PanMode(this);
                if(this.image != null)
                {
                  addMouseListener(mlistener);
                }
              }

              break;
            }
            default :
            {
              if(mlistener != null)
              {
                removeMouseListener(mlistener);
              }

              mlistener = null;
            }
          }

          if(this.mode.intValue() != oldMode.intValue())
          {
            lastMode = oldMode;
            change.firePropertyChange("mode", oldMode, this.mode);
          }
        }
//        DjVuObject.checkLockTime(lockTime,10000);
      }
    }
  }

  /**
   * Return the current mode.  May be ZOOM_MODE or PAN_MODE.
   *
   * @return the current mode.
   */
  public int getMode()
  {
    return mode.intValue();
  }

  /**
   * Return the current layout.  May be SINGLE_PAGE_LAYOUT, BOOK_PAGE_LAYOUT, or COVEP_LAYOUT.
   * 
   * @return the current layout.
   */
  public String getPageLayout()
  {
    return PAGE_LAYOUT_LIST[pageLayout.intValue()];
  }


  /**
   * Always returns true.
   *
   * @return true.
   */
  public boolean isOpaque()
  {
    return true;
  }

  /**
   * Browse to the specified page number, starting with page 1. This method
   * fires a "page" PropertyChangeEvent.
   *
   * @param page new page number to browse to.
   */
  public void setPage(final int page)
  {
    if(page != getPage())
    {
      final DjVuImage image=getImage();
      if(image != null)
      {
        prevPage = stringToBoolean(properties.getProperty("cache"),DjVuObject.hasReferences)
          ? image.getDjVuPage(image.getIndexMax()-1)
          : null;
      }

      int zoom = this.zoom;

      if(zoom == 0)
      {
        zoom = 100;
      }

      final Integer oldPage = this.page;
      clearPage=50;
      defereRepaint=true;

      try
      {
        int xpage = Math.max(
            1,
            Math.min(
              page,
              getDocumentSize()));

        if(xpage != this.page.intValue())
        {
          this.page = new Integer(xpage);

          setImage(null);
//          this.zoom   = 0;
          text        = null;
          setSearchText(null);
        }
        zoom(zoom);
        getImage();
      }
      finally
      {
        defereRepaint=false;
        change.firePropertyChange("page", oldPage, this.page);
        repaint(50L);
      }
    }
  }

//  /**
//   * Browse to the specified page at the specified scale. This method fires a
//   * "page" PropertyChangeEvent.
//   *
//   * @param page number to browse to.
//   * @param zoom factor to use.
//   */
//  public void updatePage(
//    int page,
//    int zoom)
//  {
//    final Number oldPage = this.page;
//    defereRepaint=true;
//
//    try
//    {
//      page = Math.max(
//          1,
//          Math.min(
//            page,
//            getDocumentSize()));
//
//      if(page != this.page.intValue())
//      {
//        this.page    = new Integer(page);
//        this.image   = null;
//        this.zoom   = 0;
//        text=null;
//        setSearchText(null);
//        setScrollPosition(0, 0);
//      }
//
//      zoom(zoom);
//    }
//    finally
//    {
//      defereRepaint=false;
//      change.firePropertyChange("page", oldPage, this.page);
//    }
//  }

  /**
   * Optain the current page number, starting from page 1.
   *
   * @return current page number.
   */
  public final int getPage()
  {
    return page.intValue();
  }

  /**
   * Browse to the specified page number, starting with page 1.  Special
   * values of FIRST_PAGE, PREV_PAGE, NEXT_PAGE, and LAST_PAGE will be
   * checked prior to converting the string to an integer. This method fires
   * a "page" PropertyChangeEvent.
   *
   * @param page new page number to browse to.
   */
  public void setPageString(final String page)
  {
    if(FIRST_PAGE.equals(page))
    {
      setPage(1);
    }
    else if(PREV_PAGE.equals(page))
    {
      setPage(getPage() - getVisiblePageCount());
    }
    else if(NEXT_PAGE.equals(page))
    {
      setPage(getPage() + getVisiblePageCount());
    }
    else if(LAST_PAGE.equals(page))
    {
      setPage(Integer.MAX_VALUE);
    }
    else if(page != null)
    {
      final String s     = page;
      final int    space = s.indexOf(' ');

      try
      {
        setPage(Integer.parseInt((space > 0)
            ? s.substring(0, space)
            : s));
      }
      catch(Throwable ignored) {}
    }
  }

  /**
   * Query the preferred size.
   *
   * @return The scaled Image size.
   */
  public Dimension getPreferredSize()
  {
    return getImageWait().getSize();
  }

//  /**
//   * Query the current zoom factor.  Special values of FIT_WIDTH and FIT_PAGE
//   * may be returned as well.
//   *
//   * @return the current zoom factor.
//   */
//  public int getScale()
//  {
//    return scale;
//  }

  /**
   * Set the scroll position of the relevant ScrollPane or JScrollPane.
   *
   * @param p Position to scroll to.
   */
  public final void setScrollPosition(final Point p)
  {
    setScrollPosition(p.x, p.y);
  }

  /**
   * Set the current search mask.  A special value of -1 may be used to fire
   * a property change event with the currrent value. This method fires a
   * "searchMask" PropertyChangeEvent.
   *
   * @param mask consisting of search options.
   */
  public void setSearchMask(final int mask)
  {
    if(mask == -1)
    {
      change.firePropertyChange(
        "searchMask",
        new Integer(-1),
        searchMask);
    }
    else if(searchMask.intValue() != mask)
    {
      final Integer oldMask = searchMask;
      searchMask = new Integer(mask);
      change.firePropertyChange("searchMask", oldMask, searchMask);
    }
  }

  /**
   * Query the current search mask.
   *
   * @return search option mask.
   */
  public int getSearchMask()
  {
    return searchMask.intValue();
  }

  /**
   * Set the new scaled size.
   *
   * @param size to scale to.
   */
  public final void setSize(final Dimension size)
  {
    setSize(size.width, size.height);
  }

  /**
   * Set the scroll position of the relevant ScrollPane or JScrollPane.
   *
   * @param x coordinate of position to scroll to.
   * @param y coordinate of position to scroll to.
   */
  public void setScrollPosition(
    int x,
    int y)
  {
    final DjVuImage image=getImage();
    if(image != null)
    {
      final Dimension imageSize    = image.getSize();
      final Dimension viewportSize = getViewportSize();
      x   = Math.max(
        0,
        Math.min(imageSize.width - viewportSize.width, x));
      y = Math.max(
        0,
        Math.min(imageSize.height - viewportSize.height, y));

      final String[] methodName = {"setScrollPosition", "setViewPosition"};
      final Class[]  parms = {Point.class};
      final Object[] args  = {new Point(x, y)};
      invokeParentsMethod(methodName, parms, args, 2);
    }
  }

  public void setScroll(final String value)
  {
    final Point position=getScrollPosition();
    final Dimension viewportSize = getViewportSize();
    if(SCROLL_PAGE_UP.equalsIgnoreCase(value))
    {
      setScrollPosition(position.x,position.y-(int)Math.ceil(0.9D*(double)viewportSize.height));
    }
    else if(SCROLL_PAGE_DOWN.equalsIgnoreCase(value))
    {
      setScrollPosition(position.x,position.y+(int)Math.ceil(0.9D*(double)viewportSize.height));        
    }
    else if(SCROLL_UP.equalsIgnoreCase(value))
    {
      setScrollPosition(position.x,position.y-(int)Math.ceil(0.05D*(double)viewportSize.height));        
    }
    else if(SCROLL_DOWN.equalsIgnoreCase(value))
    {
      setScrollPosition(position.x,position.y+(int)Math.ceil(0.05D*(double)viewportSize.height));
    }
    else if(SCROLL_LEFT.equalsIgnoreCase(value))
    {
      setScrollPosition(position.x-(int)Math.ceil(0.05D*(double)viewportSize.width),position.y);        
    }
    else if(SCROLL_RIGHT.equalsIgnoreCase(value))
    {
      setScrollPosition(position.x+(int)Math.ceil(0.05D*(double)viewportSize.width),position.y);
    }
  }
  
  /**
   * Query the scroll position of the relevant ScrollPane or JScrollPane.
   *
   * @return the current scroll position.
   */
  public Point getScrollPosition()
  {
    Point retval = null;

    try
    {
      final String[] methodName = {"getScrollPosition", "getViewPosition"};
      retval = (Point)invokeParentsMethod(methodName, null, null, 3);
    }
    catch(final Throwable ignored) {}

    if(retval == null)
    {
      retval = new Point(0, 0);
    }

    return retval;
  }

  /**
   * Set the text used for searching. Setting the text to a non-null value
   * will reset the caretPosition and selectionList. This method fires a
   * "searchText" PropertyChangeEvent.
   *
   * @param searchText text to use in the next search.
   */
  public void setSearchText(final String searchText)
  {
    if(
      (this.searchText != searchText)
      && ((searchText == null) || !searchText.equals(this.searchText)))
    {
      final String oldSearchText = this.searchText;
      this.searchText = searchText;
      setCaretPosition(-1);
      setCaretIndex(-1);
      change.firePropertyChange("searchText", oldSearchText, searchText);
    }
  }

  /**
   * Query the text used for the searching.
   *
   * @return the text used for searching.
   */
  public String getSearchText()
  {
    return searchText;
  }

  /**
   * Set the selected rectangle of the region.  This may be used with the
   * zoomSelect() method.
   *
   * @param select the currently selected rectangle.
   */
  public void setSelect(final Rectangle select)
  {
    final Rectangle bounds = new Rectangle(getSelect());

    if(select != null)
    {
      this.select.setBounds(select);

      if((bounds.width > 0) && (bounds.height > 0))
      {
        bounds.union(select);
      }
      else
      {
        bounds.setBounds(select);
      }
    }
    else
    {
      this.select.setBounds(0, 0, 0, 0);
    }

    bounds.setBounds(
      bounds.x - 3,
      bounds.y - 3,
      bounds.width + 6,
      bounds.height + 6);
    repaintImageCoordinates(bounds);
  }

  /**
   * Query the selected region of the image.
   *
   * @return the selected region.
   */
  public Rectangle getSelect()
  {
    return select;
  }

  /**
   * Set the new scaled size.
   *
   * @param width to scale to.
   * @param height to scale to.
   */
  public void setSize(
    int width,
    int height)
  {
    setImageSize(width, height);

    final DjVuImage image = getImageWait();

    if(image != null)
    {
      setPanelSize(image.getSize());
    }
  }

  /**
   * Called to generate a PropertyChangeEvent to tell the monitor load a new
   * URL. This method fires a "submit" PropertyChangeEvent.
   *
   * @param link a anno.Rect, URL, or String to indicate the link to load.
   */
  public void setSubmit(final Object link)
  {
    change.firePropertyChange("submit", null, link);
  }

  /**
   * Obtain a TextArea appropriate for displaying the hidden text layer. This
   * is mainly intended for cut and paste support.  Any edits will be lost
   * when navigating to a new page.
   *
   * @return represents the hidden text layer.
   */
  public final TextArea getTextArea()
  {
    return textArea;
  }

  /**
   * Query the runnable object used for searching.  This will be null if the
   * the search add-on is not available.
   *
   * @return the runnable object.
   */
  public Runnable getTextSearchObject()
  {
    Runnable retval = textSearchObject;

    if(retval == null)
    {
      textSearchObject =
        retval =
          (Runnable)createAddOn("com.lizardtech.djvubean.text.TextSearch");
    }

    return retval;
  }

  /**
   * Set the URL being browsed. This method fires a "URL"
   * PropertyChangeEvent.
   *
   * @param url to browse
   *
   * @throws IOException if an error occurs decoding the document
   */
  public void setURL(final URL url)
    throws IOException
  {
    final URL oldURL = this.url;

    if((oldURL != url) && ((url == null) || !url.equals(oldURL)))
    {
      Document document = this.document;
      if(document != null)
      {
        document.removePropertyChangeListener(this);
        document=null;
      }
      if(url != null)
      {
        document=Document.createDocument(djvuObject);
        document.addPropertyChangeListener(this);
        document.init(url);
        document.setAsync(true);
        if(stringToBoolean(properties.getProperty("prefetch"),false))
        {
//          document.prefetch();
        }
      }
      this.document   = document;
      this.url        = url;
      change.firePropertyChange("URL", oldURL, url);
      setPageString(this.properties.getProperty(
          "page",
          page.toString()));
      this.zoom = 0;

      //DjVuOptions.out.println("zoom="+this.properties.getProperty("zoom", ZOOM100));
      setZoom(this.properties.getProperty("zoom", ZOOM100));
      setPageLayout(this.properties.getProperty(
          "pagelayout",
          PAGE_LAYOUT_LIST[SINGLE_PAGE_LAYOUT]));
      setSearchText(this.properties.getProperty("searchtext"));
    }
  }

  /**
   * Query the URL being browsed.
   *
   * @return the url being browsed.
   */
  public final URL getURL()
  {
    return url;
  }

  /**
   * Set the viewport size of the relevant ScrollPane or JScrollPane.
   *
   * @param size viewport size.
   */
  public final void setViewportSize(Dimension size)
  {
    setViewportSize(size.width, size.height);
  }

  /**
   * Set the current caret index, used for text searches.  -1 may be used
   * to reset to the first visible page for forward searches and  the
   * the last visible page for backward searches.
   *
   * @param caretIndex the new caret index.
   */
  public void setCaretIndex(final int caretIndex)
  {
    if(caretIndex != this.caretIndex)
    {
      this.caretIndex = caretIndex;
    }
  }

  /**
   * Set the current caret position, used for text searches.  -1 may be used
   * to reset to the beginning of the document for forward searches and  the
   * end of the document for backward searches.
   *
   * @param caretPosition the new caret position.
   */
  public void setCaretPosition(final int caretPosition)
  {
    if(caretPosition != this.caretPosition)
    {
      this.caretPosition = caretPosition;
    }
  }

  /**
   * Query if image decoding is still in progress.
   *
   * @return true if currently decoding.
   */
  public boolean isDecoding()
  {
    final DjVuImage image = this.image;

    return (image == null) || image.isDecoding();
  }

  /**
   * Query and/or create the outline add-on component.
   *
   * @return the outline component, or null if the add-on is not available.
   */
  public Component getOutline()
  {
    Component retval = outline;

    if(retval == null)
    {
      outline =
        retval =
          (Component)createAddOn(
            "com.lizardtech.djvubean.outline.OutlineScrollPane");
    }

    return retval;
  }

  /**
   * Query the number of pages in the current document.
   * 
   * @return the number of pages in the current document.
   */
  public int getDocumentSize()
  {
    return (document != null)
    ? document.size()
    : 0;
  }

  /**
   * Called to set Properties which may be used by the add-ons.
   *
   * @param properties values to set.
   */
  public void setProperties(final Properties properties)
  {
    if(properties != null)
    {
      for(Enumeration e = properties.propertyNames(); e.hasMoreElements();)
      {
        final String name = (String)e.nextElement();
        this.properties.put(
          name,
          properties.getProperty(name));
      }
    }
  }

  /**
   * Called to obtain Properties which may be used by the add-on classes.
   *
   * @return current properties.
   */
  public final Properties getProperties()
  {
    return properties;
  }

  /**
   * Automatically called to indicated the specified property value has been
   * changed. This method fires a "propertyName" PropertyChangeEvent.
   *
   * @param name the name of the property which has changed.
   */
  public void setPropertyName(final String name)
  {
    change.firePropertyChange("propertyName", null, name);

    if("page".equalsIgnoreCase(name))
    {
      setPageString(properties.getProperty(
          "page",
          page.toString()));
    }
    else if("zoom".equalsIgnoreCase(name))
    {
      setZoom(properties.getProperty(
          "zoom",
          getZoom()));
    }
    else if("mousemode".equalsIgnoreCase(name))
    {
      setMode(
        properties.getProperty(
          "mousemode",
          Integer.toString(getMode())));
    }
    else if("pagelayout".equalsIgnoreCase(name))
    {
      setPageLayout(
        properties.getProperty(
          "pagelayout",
          getPageLayout()));
    }
  }

  /**
   * Called to set the list of selected text to highlight.
   *
   * @param index the visible page index this list is for
   * @param list the list of selected GRect rectangles to highlight.
   */
  public void setSelectionList(final int index,final Vector list)
  {
    final DjVuImage image  = getImageWait();
    Rectangle       bounds = image.getHighlightBounds();

    if(!bounds.isEmpty())
    {
      bounds = new Rectangle(bounds);
      image.setHighlightList(index,list);
      repaintImageCoordinates(bounds);
    }
    else if((list != null) && (list.size() > 0))
    {
      image.setHighlightList(index,list);
    }
    else
    {
      return;
    }

    repaintImageCoordinates(image.getHighlightBounds());

//    repaint(50L); // strictly speaking this shouldn't be needed.
  }

  /**
   * Sets the target width for toolbar type components.  Typically this is
   * set by the containing Applet, or when the toolbar is resized. This
   * method fires a "TargetWidth" PropertyChangeEvent.
   *
   * @param width to use for the toolbar layout.
   */
  public void setTargetWidth(final int width)
  {
    if((width != targetWidth.intValue()) && (width > 0))
    {
      final Number old = targetWidth;
      targetWidth = new Integer(width);
      change.firePropertyChange("TargetWidth", old, targetWidth);
    }
  }

  /**
   * Query the target width for toolbar type components.
   *
   * @return target width.
   */
  public int getTargetWidth()
  {
    return targetWidth.intValue();
  }

  /**
   * Query the hidden text for this document.
   *
   * @return the hidden text for this document.
   */
  public String getText()
  {
    String retval = text;

    if(retval == null)
    {
      final Codec textCodec = getTextCodec(currentIndex,0L);

      if(textCodec != null)
      {
        text = retval = textCodec.toString();
      }
    }

    return retval;
  }

  /**
   * Query the current DjVuText Codec.  Use a maxWait of 0L if you wish to
   * wait until either text is available or decoding is complete.
   *
   * @param index to query
   * @param maxWait the maximum time to wait in milliseconds.
   *
   * @return the current DjVuText Codec, or null.
   */
  public Codec getTextCodec(final int index,final long maxWait)
  {
    final DjVuImage image=this.image;
    return (image != null)?image.getTextCodec(index,maxWait):null;
  }

  /**
   * Query the current DjVuText Codec.  Use a maxWait of 0L if you wish to
   * wait until either text is available or decoding is complete.
   *
   * @param maxWait the maximum time to wait in milliseconds.
   *
   * @return the current DjVuText Codec, or null.
   */
  public Codec getTextCodec(final long maxWait)
  {
    final DjVuImage image=this.image;
    return (image != null)?image.getTextCodec(currentIndex,maxWait):null;
  }

  /**
   * Query and/or create the toolbar add-on component.
   *
   * @return the toolbar component, or null if the add-on is not available.
   */
  public Component getToolbar()
  {
    Component retval = toolBar;

    if(retval == null)
    {
      toolBar =
        retval =
          (Component)createAddOn("com.lizardtech.djvubean.toolbar.Toolbar");
    }

    return retval;
  }
  
  /**
   * Set the viewport size of the relevant ScrollPane or JScrollPane.
   *
   * @param width of viewport.
   * @param height of viewport.
   */
  public void setViewportSize(
    int width,
    int height)
  {
    try
    {
      final String[] methodName = {"setViewportSize", "setViewSize"};
      final Class[]  parms = {Dimension.class};
      final Object[] args  = {new Dimension(width, height)};
      invokeParentsMethod(methodName, parms, args, 3);
    }
    catch(final Throwable ignored) {}
  }

  /**
   * Query the viewport size of the relevant ScrollPane or JScrollPane.
   *
   * @return the viewport size.
   */
  public Dimension getViewportSize()
  {
    Dimension retval = null;

    try
    {
      final String[] methodName = {"getViewportSize", "getViewSize"};
      retval = (Dimension)invokeParentsMethod(methodName, null, null, 3);
    }
    catch(final Throwable ignored) {}

    return (retval == null)
    ? getSize()
    : retval;
  }

  /**
   * Scale the current zoom to either a value specified in the
   * ZOOM_SPECIAL_LIST, or a number followed by percent sign. i.e.
   * setZoom("125%"). This method calls setZoom which fires a "zoom"
   * PropertyChangeEvent.
   *
   * @param zoom The zoom factor to scale to.
   */
  public void setZoom(final String zoom)
  {
    //try { throw new Exception( "setZoom("+zoom+"),"+getZoom()); } catch(final Throwable exp) {exp.printStackTrace(DjVuOptions.err);}
    if(!getZoom().equals(zoom))
    {
      final int percent = zoom.indexOf('%');

      if(percent > 0)
      {
        try
        {
          zoom(Integer.parseInt(zoom.substring(0, percent)));
        }
        catch(Throwable ignored) {}
      }
      else if(ZOOM_ONE_TO_ONE.equalsIgnoreCase(zoom))
      {
        zoom(getDPI());
      }
      else if(DjVuBean.ZOOM_FIT_WIDTH.equalsIgnoreCase(zoom))
      {
        zoom(DjVuBean.FIT_WIDTH);
      }
      else if(DjVuBean.ZOOM_FIT_PAGE.equalsIgnoreCase(zoom))
      {
        zoom(DjVuBean.FIT_PAGE);
      }
      else if(DjVuBean.ZOOM_IN.equalsIgnoreCase(zoom))
      {
        final int scale =
          (this.zoom <= 0)
          ? Math.min(
            getXScale(),
            getYScale())
          : this.zoom;
        int       nscale;

        if(scale >= 1051)
        {
          nscale = 1200;
        }
        else if(scale >= 370)
        {
          nscale = 100 * ((scale + 149) / 100);
        }
        else if(scale >= 270)
        {
          nscale = 400;
        }
        else if(scale >= 170)
        {
          nscale = 300;
        }
        else if(scale >= 135)
        {
          nscale = 200;
        }
        else if(scale >= 85)
        {
          nscale = 150;
        }
        else if(scale >= 65)
        {
          nscale = 100;
        }
        else if(scale >= 40)
        {
          nscale = 75;
        }
        else
        {
          nscale = 50;
        }

        zoom(nscale);
      }
      else if(DjVuBean.ZOOM_OUT.equalsIgnoreCase(zoom))
      {
        final int scale =
          (this.zoom <= 0)
          ? Math.max(
            getXScale(),
            getYScale())
          : this.zoom;
        int       nscale;

        if(scale < 60)
        {
          nscale = 25;
        }
        else if(scale < 85)
        {
          nscale = 50;
        }
        else if(scale < 120)
        {
          nscale = 75;
        }
        else if(scale < 165)
        {
          nscale = 100;
        }
        else if(scale < 225)
        {
          nscale = 150;
        }
        else if(scale < 325)
        {
          nscale = 200;
        }
        else if(scale < 450)
        {
          nscale = 300;
        }
        else
        {
          nscale = 100 * ((scale - 51) / 100);
        }

        zoom(nscale);
      }
      else if("width".equalsIgnoreCase(zoom))
      {
        zoom(DjVuBean.FIT_WIDTH);
      }
      else if("page".equalsIgnoreCase(zoom))
      {
        zoom(DjVuBean.FIT_PAGE);
      }
      else
      {
        try
        {
          zoom(Integer.parseInt(zoom));
        }
        catch(Throwable ignored) {}
      }
    }
  }

  /**
   * Called to query the current Zoom setting.  This will either be a value
   * from the ZOOM_SPECIAL_LIST, or the current scale factor appended with a
   * percent sign.
   *
   * @return current zoom setting.
   */
  public String getZoom()
  {
    final int scale = zoom;

    switch(scale)
    {
      case FIT_WIDTH :
        return ZOOM_FIT_WIDTH;
      case FIT_PAGE :
        return ZOOM_FIT_PAGE;
      default :
        return scale + "%";
    }
  }

  /**
   * Add a listener for property change events.
   *
   * @param listener to add
   */
  public void addPropertyChangeListener(
    final PropertyChangeListener listener)
  {
    change.addPropertyChangeListener(listener);
  }

  /**
   * This overloaded ImageObserver method ignores image updates 
   * until a full frame is available.  Then refresh is called to
   * display the new pixels.
   *
   * @param image Updated image 
   * @param infoflags Flags indicating the new data available.
   * @param x Coordinate of left edge. (-1 if unknown)
   * @param y Coordinate of top edge. (-1 if unknown)
   * @param width Image width. (-1 if unknown)
   * @param height Image height. (-1 if unknown)
   *
   * @return true if we should continue to listen for updates.
   */
  public boolean imageUpdate(
    final Image image,
    final int   infoflags,
    final int   x,
    final int   y,
    final int   width,
    final int   height)
  {
    //DjVuOptions.err.println("imageUpdate "+infoflags+","+width);
    if((infoflags & (ALLBITS | FRAMEBITS)) != 0)
    {
      repaint(20L);

      return true;
    }
    else if((infoflags & SOMEBITS) != 0)
    {
      return true;
    }

    return super.imageUpdate(image, infoflags, x, y, width, height);
  }

  /**
   * Called when a property has been changed.  This is used  to monitor
   * progressive decodes for text.
   *
   * @param event indicates the changed property.
   */
  public void propertyChange(PropertyChangeEvent event)
  {
//    DjVuOptions.err.println("propertyChange "+this.image+" "+event);
    try
    {
      final String name=(event != null)?event.getPropertyName():null;
      if("status".equalsIgnoreCase(name))
      {
        final Object value=event.getNewValue();
        setStatus((value != null)?value.toString():NILL);          
      }
      else if(DjVuImage.isDoneDecodingEvent(this.image, this, event))
      {
        //repaint(50L);
        final String text    = getText();
        final String oldText = textArea.getText();

        if((oldText != text) && ((oldText == null) || !oldText.equals(text)))
        {
          textArea.setText((text != null)
            ? text
            : NILL);
          textArea.repaint();
          change.firePropertyChange("text", oldText, text);
        }

        final DjVuImage image=this.image;
        if((image != null)&&!image.isDecoding())
        {
          final int pageNo = getPage();

          if(stringToBoolean(properties.getProperty("cache"),DjVuObject.hasReferences))
          {
            try
            {
              nextPage = getDocument().getPage(pageNo+getVisiblePageCount()-1, DjVuPage.MIN_PRIORITY, false);
            }
            catch(final Throwable ignored) {}

            try
            {
              prevPage = getDocument().getPage(pageNo-getVisiblePageCount()-1, DjVuPage.MIN_PRIORITY, false);
            }
            catch(final Throwable ignored) {}
          }
        }
      }
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
    }
  }

  /**
   * Convert a string to a boolean using a case insensative comparison. "yes"
   * and "true" will be recognized as true. "no" and "false" will be
   * recognized as false. Anything else will be return the default value.
   *
   * @param value string to convert.
   * @param retval default value to return.
   *
   * @return the converted boolean.
   */
  public static boolean stringToBoolean(
    final String  value,
    final boolean retval)
  {
    try
    {
      if("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value))
      {
        return true;
      }

      if("no".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value))
      {
        return false;
      }
    }
    catch(final Throwable ignored) {}

    return retval;
  }

  /**
   * This is used to create additional objects, like the TextFinder, toolbars
   * and outline navigation.  In general an add-on may be any class with a
   * constructor which accepts the single argument of this DjVuBean.
   *
   * @param name class name of the add-on.
   *
   * @return the newly created add-on, or null if no add-on was created.
   */
  public Object createAddOn(final String name)
  {
    try
    {
      final Class       addOnClass       = Class.forName(name);
      final Class[]     properties       = {DjVuBean.class};
      final Constructor addOnConstructor =
        addOnClass.getConstructor(properties);
      final Object[]    args = new Object[1];
      args[0] = this;

      return addOnConstructor.newInstance(args);
    }
    catch(final Throwable ignored) {}

    return null;
  }

  /**
   * Called to draw the DjVuPage
   *
   * @param g Graphics device to draw to.
   */
  public void paint(Graphics g)
  {
    if(NEED_PAINT_STATUS)
    {
      setStatus("paint called");
    }
    if(isShowing())
    {
      paintComponent(g);
    }
  }
  
  public DjVuImage getImage()
  {
    DjVuImage retval=this.image;
    if((retval == null)&&(getImageThread == null)&&!firstTime)
    {
      final Thread t=new Thread(this);
      getImageThread=t;
      t.start();
    }
    return retval;
  }

  /**
   * Called to draw the DjVuPage
   *
   * @param g Graphics device to draw to.
   */
  public void paintComponent(Graphics g)
  {
    if(isShowing())
    {
      final Dimension size = getViewportSize();

      if((size.width != oViewportWidth) || (size.height != oViewportHeight))
      {
        //DjVuOptions.out.println("scale="+size+",zoom="+zoom);
        final int scale = zoom;

        if(scale < 0)
        {
          zoom = 0;
          zoom(scale);
          //return;
        }
      }
      if(firstTime)
      {
        firstTime = false;
        setStatus("requesting focus");
        requestFocus();
        createAddOn("com.lizardtech.djvubean.menu.DjVuMenu");
        createAddOn("com.lizardtech.djvubean.keys.DjVuKeys");
        annoManager=createAddOn("com.lizardtech.djvubean.anno.AnnoManager");
        try
        {
          drawMapAreaMethod=annoManager.getClass().getMethod("draw",new Class[]{Graphics.class});
        }
        catch(final Throwable ignored) {}
        repaint(30L);
        return;
      }
      try
      {
        final Rectangle scrollPaneBounds =
          new Rectangle(
            getScrollPosition(),
            getViewportSize());
        final DjVuImage image = getImage();
        if(image == null)
        {
          //DjVuOptions.err.println("image="+image);
          try {Thread.sleep(20L); } catch(final Throwable exp) {}
          repaint(30L);
          return;
        }
        image.setBufferBounds(this, scrollPaneBounds);
//        g = g.create();
//        g.clipRect(
//          scrollPaneBounds.x,
//          scrollPaneBounds.y,
//          scrollPaneBounds.width,
//          scrollPaneBounds.height);

        boolean useXOR = false;

        Throwable expsaved=null;
        try
        {
          image.draw(this, g, this);
          useXOR = true;
        }
        catch(final Throwable exp)
        {
          expsaved=exp;
        }
        if(!isDecoding())
        {
          try
          {
            if(drawMapAreaMethod != null)
            {
              drawMapAreaMethod.invoke(annoManager,new Object[]{g});
            }
          } 
          catch(final Throwable exp) {}
          image.drawHighlight(g, useXOR);
        }

        final Rectangle select = getSelect();

        if((select.width > 1) && (select.height > 1))
        {
          if(useXOR)
          {
            g.setXORMode(DjVuImage.WHITE);
            g.drawRect(select.x, select.y, select.width, select.height);

            if((select.width > 3) && (select.height > 3))
            {
              g.drawRect(
                select.x + 1,
                select.y + 1,
                select.width - 2,
                select.height - 2);
            }
          }

          g.setPaintMode();
          g.setColor(DjVuImage.BLUE);
          g.drawRect(
            select.x - 1,
            select.y - 1,
            select.width + 2,
            select.height + 2);
        }
        if(expsaved != null)
        {
          throw expsaved;
        }
      }
      catch(final Throwable exp)
      {
        setStatus("error "+exp.toString());
        exp.printStackTrace(DjVuOptions.err);
      }
      if(NEED_PAINT_STATUS)
      {
        setStatus("screen updated");
      }
    }
  }

  public void run()
  {
    if(getImageThread == Thread.currentThread())
    {
      getImageWait();
      getImageThread=null;
    }
  }

  /**
   * Called to recursively validate each parent.
   */
  public void recursiveRevalidate()
  {
    invalidate();

    try
    {
      for(
        Component component = this;
        (component != null) && (component.isShowing());
        component = component.getParent())
      {
        component.validate();
      }
    }
    catch(final Throwable ignored) {}
  }

  /**
   * Remove a listener for PropertyChangeEvent.
   *
   * @param listener to remove
   */
  public void removePropertyChangeListener(
    final PropertyChangeListener listener)
  {
    change.removePropertyChangeListener(listener);
  }

  /**
   * Used to specify a rectangle in document coordinates to repaint. This is
   * usually used for highlighting search results.
   *
   * @param bounds Rectangle to highlight.
   */
  public void repaintImageCoordinates(final Rectangle bounds)
  {
    if((bounds != null) && (!bounds.isEmpty()) && isShowing())
    {
      try
      {
        final Rectangle scrollPaneBounds =
          new Rectangle(
            getScrollPosition(),
            getViewportSize());

        if(scrollPaneBounds.intersects(bounds))
        {
          repaint(20L, 
            bounds.x - scrollPaneBounds.x,
            bounds.y - scrollPaneBounds.y,
            bounds.width,
            bounds.height);
        }
      }
      catch(final Throwable exp)
      {
        exp.printStackTrace(DjVuOptions.err);
        System.gc();
      }
    }
  }

  /**
   * Called to adjust to adjust the scroll position after scaling the image.
   *
   * @param owidth old width.
   * @param oheight old height.
   * @param nwidth new width.
   * @param nheight new height.
   */
  public void scaleScrollPosition(
    final int owidth,
    final int oheight,
    final int nwidth,
    final int nheight)
  {
    final Point     scrollPosition = getScrollPosition();
    final Dimension viewportSize = getViewportSize();
    final long      xmid2        =
      (2L * scrollPosition.x) + viewportSize.width;
    final long      x2    = ((xmid2 * nwidth) / owidth) - viewportSize.width;
    final long      ymid2 = (2L * scrollPosition.y) + viewportSize.height;
    final long      y2    =
      ((ymid2 * nheight) / oheight) - viewportSize.height;
    setScrollPosition((int)(x2 / 2L), (int)(y2 / 2L));
  }

  /**
   * Called to draw the DjVuPage
   *
   * @param g Graphics device to draw to.
   */
  public void update(Graphics g)
  {
    if(isShowing())
    {
      if(NEED_PAINT_STATUS)
      {
        setStatus("update called");
      }

      //DjVuOptions.err.println("clearPage="+clearPage+" defereRepaint="+defereRepaint);
      if(clearPage-- == 0)
      {
        g.clearRect(0,0, 65535, 65535);
      }
      if(defereRepaint)
      {
        try { Thread.sleep(20L); } catch(final Throwable ignored) {}
        repaint(30L);
      }
      else
      {
        paintComponent(g);
      }
    }
  }

  /**
   * Called to recursively validate.
   */
  public void validate()
  {
    if(!isValid())
    {
      super.validate();
      repaint(50L);
    }
  }

  /**
   * Called to zoom to the current selected rectangle set with setSelect().
   */
  public void zoomSelect()
  {
    final Rectangle select = getSelect();

    if((select.width > 0) && (select.height > 0))
    {
      Dimension scrollPaneSize = getViewportSize();
      final double scale = Math.min(getXScale(), getYScale());      
      Dimension    borderNW = getImageWait().getBorderNW();
      final double x0     = (double)(2 * (select.x - borderNW.width));
      final double y0     = (double)(2 * (select.y - borderNW.height));
      final double width  = (double)select.width;
      final double height = (double)select.height;

      setImage(null);
      zoom((int)(Math.min((double)scrollPaneSize.width / width, (double)scrollPaneSize.height / height) * scale));
      scrollPaneSize   = getViewportSize();
      this.image       = getImageWait();
      borderNW         = getImageWait().getBorderNW();
      double s = Math.min(getXScale(),getYScale())/scale;
      setScrollPosition(
        borderNW.width
        + (((int)(((x0 + width) * s) - (double)scrollPaneSize.width)) / 2),
        borderNW.height
        + (((int)(((y0 + height) * s) - (double)scrollPaneSize.height)) / 2));
    }

    setSelect(null);
  }

  /**
   * Set the image to display. This method fires a "image"
   * PropertyChangeEvent.
   *
   * @param image to display.
   */
  protected void setImage(final DjVuImage image)
  {
    final DjVuImage old=this.image;
    if(old != null)
    {
      final MouseListener mlistener = this.mlistener;
      if(mlistener != null)
      {
        removeMouseListener(mlistener);
      }
      final int jmax=old.getIndexMax();
      for(int j=0;j<jmax;j++)
      {
        final DjVuPage page=(DjVuPage)old.getDjVuPage(j);
        if(page != null)
        {
          page.removePropertyChangeListener(this);
        }
      }
      final String oldText = textArea.getText();
      if((oldText != NILL) && ((oldText == null) || !oldText.equals(NILL)))
      {
        textArea.setText(NILL);
        textArea.repaint();
        change.firePropertyChange("text", oldText, text);
      }
      this.image = null;
    }
    if(image != null)
    {
//      final long lockTime=System.currentTimeMillis();
      String status=null;
      synchronized(this)
      {
        this.image = image;
        if(mlistener != null)
        {
          addMouseListener(mlistener);
        }
//        DjVuObject.checkLockTime(lockTime,10000);
        final int jmax=image.getIndexMax();
        for(int j=0;j<jmax;j++)
        {
          final DjVuPage page=image.getDjVuPage(j);
          if(page != null)
          {
            page.addPropertyChangeListener(this);
            final String s=page.getStatus();
            if(s != null)
            {
              if(status == null)
              {
                status=s;
              }
              else
              {
                status+=":"+s;
              }
            }
          }
        }
      }
      if(status != null)
      {
        setStatus(status);
      }
    }
    change.firePropertyChange("image", old, image);
  }

  /**
   * Query the Maximum Panel size.
   *
   * @return the maximum panel size.
   */
  protected Dimension getMaximumPanelSize()
  {
    return super.getMaximumSize();
  }

  /**
   * Adjust the panel size.
   *
   * @param size The new panel size.
   */
  protected final void setPanelSize(final Dimension size)
  {
    setPanelSize(size.width, size.height);
  }

  /**
   * Adjust the panel size.
   *
   * @param width The new panel width.
   * @param height The new panel height.
   */
  protected void setPanelSize(
    int width,
    int height)
  {
    Dimension size = getSize();

    if((size.width != width) || (size.height != height))
    {
      super.setSize(width, height);
    }
  }

  /**
   * Query the Preferred Panel size.
   *
   * @return the preferred panel size.
   */
  protected Dimension getPreferredPanelSize()
  {
    return super.getPreferredSize();
  }

  /**
   * Return the zoom factor of the X dimension.
   *
   * @return x-zoom factor.
   */
  protected int getXScale()
  {
    final DjVuImage image  = getImageWait();
//    final long      width  = image.getDjVuInfoArray()[0].width;
//    final long      dpi    = image.getDjVuInfoArray()[0].dpi;
//    final long      cwidth = image.getSize().width;
//    final long      xscale = (((dpi * cwidth) + width) - 1) / width;

//    return (int)xscale;
    return (int)Math.ceil(image.getHorizontalScale());
  }

  /**
   * Return the zoom factor of the Y dimension.
   *
   * @return y-zoom factor.
   */
  protected int getYScale()
  {
    final DjVuImage image   = getImageWait();
//    final long      height  = image.getDjVuInfoArray()[0].height;
//    final long      dpi     = image.getDjVuInfoArray()[0].dpi;
//    final long      cheight = image.getSize().height;
//    final long      yscale  = (((dpi * cheight) + height) - 1) / height;
//
//    return (int)yscale;
    return (int)Math.ceil(image.getVerticalScale());
  }

  /**
   * Called to invoke a method of the container class.
   *
   * @param component to invoke method in.
   * @param methodName name of method to invoke.
   * @param parms parameter types of the method.
   * @param args arguments to the method.
   *
   * @return retval of the method.
   *
   * @throws NoSuchMethodException if the mothod does not exist.
   * @throws IllegalAccessException if access to the method is denied.
   * @throws InvocationTargetException if invocation fails.
   */
  protected Object invokeMethod(
    final Object   component,
    final String[] methodName,
    final Class[]  parms,
    final Object[] args)
    throws NoSuchMethodException, IllegalAccessException, 
      InvocationTargetException
  {
    Object retval = null;

    for(int i = 0; i < methodName.length;)
    {
      try
      {
        Method method =
          component.getClass().getMethod(methodName[i++], parms);
        retval = method.invoke(component, args);

        break;
      }
      catch(NoSuchMethodException exp)
      {
        if(i == methodName.length)
        {
          throw exp;
        }
      }
      catch(InvocationTargetException exp)
      {
        if(i == methodName.length)
        {
          throw exp;
        }
      }
      catch(IllegalAccessException exp)
      {
        if(i == methodName.length)
        {
          throw exp;
        }
      }
    }

    return retval;
  }

  /**
   * Called to invoke a method of the parent classes.
   *
   * @param methodName name of method to invoke.
   * @param parms parameter types of the method.
   * @param args arguments to the method.
   * @param maxDepth maximum number of parents to try.
   *
   * @return retval of the method or null.
   */
  protected Object invokeParentsMethod(
    final String[] methodName,
    final Class[]  parms,
    final Object[] args,
    int            maxDepth)
  {
    Object retval = null;

    for(
      Component parent = getParent();
      (parent != null) && (maxDepth-- > 0);
      parent = parent.getParent())
    {
      try
      {
        retval = invokeMethod(parent, methodName, parms, args);

        break;
      }
      catch(final Throwable ignored) {}
    }

    return retval;
  }

  /**
   * Change the current zoom value. This method fires a "zoom"
   * PropertyChangeEvent.
   *
   * @param zoom to scale to.
   */
  private void zoom(int zoom)
  {
    final boolean notRecursive = (oldZoom == null);

    zoom=(zoom > 0)?Math.min(Math.max(25,zoom),1200):zoom;
    if(notRecursive)
    {
      oldZoom = getZoom();
      defereRepaint=true;
    }

    try
    {
      //try { throw new Exception( "zoom="+zoom+",this.zoom="+this.zoom); } catch(final Throwable exp) {exp.printStackTrace(DjVuOptions.err);}
      if(this.zoom != zoom)
      {
//        final long lockTime=System.currentTimeMillis();
        if(this.zoom != zoom)
        {
          final Dimension size     = getViewportSize();
          final DjVuImage image    = getImage();

          if(image != null)
          {
            final Rectangle bounds=image.computeScaledBounds(zoom,size);
            setImageSize(bounds.width, bounds.height);
          }
          this.zoom = zoom;
        }
      }
    }
    finally
    {
      if(notRecursive)
      {
        change.firePropertyChange(
          "zoom",
          oldZoom,
          getZoom());
        oldZoom = null;
        defereRepaint=false;
      }
    }
  }
}
