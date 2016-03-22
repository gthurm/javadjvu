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
import java.awt.*;
import java.awt.image.*;
import java.beans.*;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.*;


/**
 * This class is used to represent a DjVuPage object as a Vector of buffered
 * images, for displaying in a viewport.
 */
public class DjVuImage
{
  //~ Static fields/initializers ---------------------------------------------

  public static boolean BROKEN_XOR=System.getProperty("os.name").equalsIgnoreCase("Mac OS X");
  
  /** A constant indicating the color white. */
  public static Color WHITE = new Color(255, 255, 255);

  /** A constant indicating the color blue. */
  public static Color BLUE = new Color(0, 0, 255);

  /** A constant indicating the color blue. */
  public static Color BLACK = new Color(0, 0, 0);

  /** Magic scale value used to zoom to fit width. */
  public static final int FIT_WIDTH = -1;

  /** Magic scale value used to zoom to fit page. */
  public static final int FIT_PAGE = -2;
  
  /** Test to determine java version. */
  public static final Constructor colorConstructor;

  /** Special value for XOR HIGHLIGHTING */
  public static final int XOR_HILITE=0xFF000000;
  
  //~ Instance fields --------------------------------------------------------

  static
  {
    Constructor c=null;
    try
    {
      c=Color.class.getConstructor(new Class[]{Integer.TYPE,Boolean.TYPE});
    }
    catch(final Throwable ignored) {}
    colorConstructor=c;
  }
    
  // north and west borders respectively.
  private final Dimension borderNW = new Dimension();

  // south and east borders respectively.
  private final Dimension borderSE = new Dimension();
  
  // The DjVuPage rendered by this class.
  private final DjVuPage [] djvuPageArray;

  // The page information
  private final DjVuInfo [] infoArray;

  // Array of page bounds
  private final Rectangle [] boundArray;
  
  // Bounding box for filters
  private final Rectangle filterBounds=new Rectangle();

  // Array of DjVuFilter objects
  private final DjVuFilter [] filterArray;
  
  // True if fit page and fit width should be rounded down for faster viewing
  private final boolean favorFast;
  
  // Rectangle specifying the target area to draw.
  private Rectangle targetBounds  = null;
  
  
  // Normalized width
  private final double sWidth;
  // Normalized height
  private final double sHeight;
  // Highest DPI
  private final int dpi;
  
  private Rectangle highlightBounds    = new Rectangle();
  //private Rectangle rawHighlightBounds = new Rectangle();
  private double highlightXmin = 0;
  private double highlightYmin = 0;
  private double highlightXmax = 0;
  private double highlightYmax = 0;
  
  private Vector    highlightList = new Vector();

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new DjVuImage object.
   *
   * @param djvuPages the array of pages to draw at full resolution.
   * @param favorFast true if fast magnifications should be favored.
   */
  public DjVuImage(
          final DjVuPage [] djvuPages,
          final boolean favorFast)
  {
    this.djvuPageArray = djvuPages;
    infoArray=new DjVuInfo[djvuPages.length];
    boundArray=new Rectangle[djvuPages.length];
    filterArray=new DjVuFilter[djvuPages.length];
    
    double sWidth=0;
    double sHeight=0;
    int dpi=25;
    for(int i=0;i<djvuPages.length;i++)
    {
      infoArray[i]=null;
      boundArray[i]=null;
      filterArray[i]=null;
      final DjVuPage page=djvuPages[i];
      if(page != null)
      {
        final DjVuInfo info=page.getInfoWait();
        this.infoArray[i]=info;
        sWidth+=(double)info.width/(double)info.dpi;
        double h=(double)info.height/(double)info.dpi;
        if(h > sHeight)
        {
          sHeight=h;
        }
        if(info.dpi > dpi)
        {
          dpi=info.dpi;
        }
      }
    }
    this.sWidth=sWidth;
    this.sHeight=sHeight;
    this.dpi=dpi;
    this.favorFast=favorFast;
  }

  /**
   * Creates a new DjVuImage object.
   * 
   * @param djvuPages the array of pages to draw at full resolution.
   * @param favorFast true if fast magnifications should be favored.
   * @param zoom factor to scale by as a percentage
   * @param size holder for returning the viewing size
   */
  public DjVuImage(
          final DjVuPage [] djvuPages,
          final boolean favorFast,
          final int zoom,
          final Dimension size)
  {
    this(djvuPages,favorFast);
    setTargetBounds(computeScaledBounds(zoom, size));
  }

  /**
   * Creates a new DjVuImage object.
   * 
   * @param djvuPages the array of pages to draw at full resolution.
   * @param favorFast true if fast magnifications should be favored.
   * @param width The width to scale the page to.
   * @param height The height to scale the page to.
   */
  public DjVuImage(
    final DjVuPage [] djvuPages,
    final boolean favorFast,
    final int      width,
    final int      height)
  {
    this(djvuPages,favorFast);
    setTargetBounds(new Rectangle(0, 0, width, height));
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Equivalent to new Color(rgb,hasAlpha).
   *
   * @param rgb color as an integer
   * @param alpha true if there is an alpha channel
   *
   * @return the scaled size
   */
  public static Color createColor(int rgb,final int alpha)
  {
    Color retval=null;
    if(alpha > 254)
    {
      retval=new Color(rgb);        
    }
    else if(colorConstructor != null)
    {
      try
      {
        final Object[] args={new Integer((rgb&0xffffff)|((alpha<<24)&0xff000000)),Boolean.TRUE};
        retval=(Color)colorConstructor.newInstance(args);
      }
      catch(final Throwable ignored) {}
    }
    return retval;
  }
  
  // Sets the target size for this image
  private void setTargetBounds(final Rectangle bounds)
  {
    targetBounds=bounds;
    int x0=0;
    int y0=0;
    final double sh=bounds.width/sWidth;
    final double sv=bounds.height/sHeight;
    for(int j=0;j<infoArray.length;j++)
    {
      final DjVuInfo info=infoArray[j];
      if(info != null)
      {
        final int w=(int)Math.ceil(((sh*(double)info.width)/(double)info.dpi)-0.00001D);
        final int h=(int)Math.ceil(((sv*(double)info.height)/(double)info.dpi)-0.00001D);
        boundArray[j]=new Rectangle(x0,y0, w, h);
        x0+=w;
      }
    }
  }
  
  /**
   * Query the vertical scale
   */
  public double getVerticalScale()
  {
    return getBounds().height/sHeight;
  }
  
  /**
   * Query the horizontal scale
   */
  public double getHorizontalScale()
  {
    return getBounds().width/sWidth;
  }

  /**
   * Query the maximum DPI of the pages being displayed.
   */
  public int getMaxDPI()
  {
      return dpi;
  }
  
  /**
   * Compute the page size scaled at the appropriate zoom.
   *
   * @param zoom scale factor
   * @param size target window size
   *
   * @return the scaled size
   */
  public Rectangle computeScaledBounds(final int zoom, final Dimension size)
  {
    final double sDPI=dpi;
    double width=sWidth*sDPI;
    double height=sHeight*sDPI;
    switch(zoom)
    {
      case FIT_WIDTH : // FIT_WIDTH
      {
        double sw=((double)size.width)/width;
        if(favorFast && (sw < 1.0D))
        {
          sw=1.0D/Math.ceil(1.0D/sw);
          width*=sw;
        }
        else
        {
          width=size.width;
        }
        height*=sw;
        break;
      }
      case FIT_PAGE : // FIT_PAGE
      {
        final double sw=(double)size.width/(double)width;
        final double sh=(double)size.height/(double)height;
        double s=(sw<sh)?sw:sh;
        if(favorFast && (s < 1.0D))
        {
          s=1.0D/Math.ceil(1.0D/s);
        }
        width*=s;
        height*=s;
        break;
      }
      case 0:
      {
        break;
      }
      default : // regular scaling
      {
        final double s=((double)((zoom < 1)?1:zoom))/sDPI;
        width*=s;
        height*=s;
        break;
      }
    }
    return new Rectangle(0,0, (int)Math.ceil(width), (int)Math.ceil(height));
  }

  /**
   * Query the north and west border width and height respectively.
   *
   * @return The NW corner border.
   */
  public Dimension getBorderNW()
  {
    return borderNW;
  }

  /**
   * Query the south and east border width and height respectively.
   *
   * @return The SE corner border.
   */
  public Dimension getBorderSE()
  {
    return borderSE;
  }

  /**
   * Query the bounds for a particular page.
   *
   * @param index page to query
   *
   * @return bounding rectangle
   */
  public Rectangle getPageBounds(final int index)
  {
    if(targetBounds == null)
    {
      getBounds();
    }
    return boundArray[index];
  }

  /**
   * Query if decoding is still in progress.
   *
   * @return True if still decoding.
   */
  public boolean isDecoding()
  {
    for(int i=0;i<djvuPageArray.length;i++)
    {
        final DjVuPage page=djvuPageArray[i];
        if((page != null)&&page.isDecoding())
        {
            return true;
        }
    }
    return false;
  }

  /**
   * Query a DjVuPage being rendered.
   *
   * @param index page to query
   *
   * @return The DjVuPages being rendered.
   */
  public DjVuPage getDjVuPage(final int index)
  {
    return djvuPageArray[index];
  }

  /**
   * Check if this is a done decoding event.  If done or the event is not for
   * this page remove the listener.
   *
   * @param listener the listener to check
   * @param event the event to check
   *
   * @return true if a done decoding event
   */
  public boolean isDoneDecodingEvent(
    final PropertyChangeListener listener,
    final PropertyChangeEvent    event)
  {
    if(event == null)
    {
        return !isDecoding();
    }
    for(int i=0;i<djvuPageArray.length;i++)
    {
      final DjVuPage page=djvuPageArray[i];
      if((page != null)&&page.equals(event.getSource()))
      {
          return event.getPropertyName().equals(page.doneLock);
      }
    }
    return false;
  }

  /**
   * Check if this is a done decoding event.  If done or the event is not for
   * this page remove the listener.
   *
   * @param image the image to check
   * @param listener the listener to check
   * @param event the event to check
   *
   * @return true if a done decoding event
   */
  public static boolean isDoneDecodingEvent(
    final DjVuImage              image,
    final PropertyChangeListener listener,
    final PropertyChangeEvent    event)
  {
    return (image != null) && image.isDoneDecodingEvent(listener, event);
  }

  /**
   * Computes the size of the border.
   *
   * @param parentSize The size of the parent window.
   * @param imageBounds The scaled size of the DjVuPage.
   */
  public void setBorder(
    final Dimension parentSize,
    final Rectangle imageBounds)
  {
    final int width  = Math.max(parentSize.width - imageBounds.width, 0);
    final int height = Math.max(parentSize.height - imageBounds.height, 0);
    borderNW.width    = width / 2;
    borderNW.height   = height / 2;
    borderSE.width    = width - borderNW.width;
    borderSE.height   = height - borderNW.height;
  }

  /**
   * Query the scaled size of this image, with an origin at (0,0).
   *
   * @return The segmented rectangle.
   */
  public Rectangle getBounds()
  {
    Rectangle retval = targetBounds;
    if(retval == null)
    {
      retval = new Rectangle(0, 0, (int)Math.ceil(sWidth*dpi), (int)Math.ceil(sHeight*dpi));
      setTargetBounds(retval);
    }
    return retval;
  }

  /**
   * Sets the bounds for buffering.  Any image outside these bounds will be
   * removed from the buffer.  If full buffering is used, the image buffer
   * will be filled at this time.
   *
   * @param parent The containing component.
   * @param bounds The desired boundrary.
   */
  public void setBufferBounds(
    final Component parent,
    Rectangle       bounds)
  {
    setBorder(
      parent.getSize(),
      getBounds());
    setBufferBounds(transformBounds(bounds));
  }

  /**
   * Sets the bounds for buffering.  Any image outside these bounds will be
   * removed from the buffer.
   *
   * @param bounds Desired bounds.
   */
  public void setBufferBounds(final Rectangle bounds)
  {
    if(bounds.isEmpty())
    {
      filterBounds.setBounds(0,0,0,0);
      for(int j=0;j<filterArray.length;j++)
      {
        filterArray[j]=null;
      }
    }
    else if((filterBounds.width != bounds.width)
        || (filterBounds.height != bounds.height))
    {
      final Rectangle bb=new Rectangle();
      for(int j=0;j<boundArray.length;j++)
      {
        final Rectangle b=boundArray[j];
        if(b != null)
        {
          bb.setBounds(
                  bounds.x-b.x,
                  bounds.y-b.y,
                  bounds.width,
                  bounds.height);
          filterArray[j] = new DjVuFilter(
            bb,
            b.getSize(),
            djvuPageArray[j],
            true);
        }
        else
        {
          filterArray[j]=null;
        }
      }
    }
    else if(
      (filterBounds.x != bounds.x)
        || (filterBounds.y != bounds.y))
    {
      for(int j=0;j<boundArray.length;j++)
      {
        final DjVuFilter filter=filterArray[j];
        if(filter != null)
        {
          final Rectangle b=boundArray[j];
          filter.move(bounds.x-b.x, bounds.y-b.y);
        }
      }
    }
    filterBounds.setBounds(bounds.x,bounds.y,bounds.width,bounds.height);
  }

  /**
   * Get a segmented image filter.
   *
   * @param parent Parent component to create the Image in.
   * @param bounds Segmentation bounds.
   * @param index filter to create
   *
   * @return The newly created filter.
   */
  public DjVuFilter getFilter(
    final Component parent,
    final Rectangle bounds,
    final int index)
  {
    return new DjVuFilter(
      bounds,
      getSize(),
      djvuPageArray[index],
      true);
  }

  /**
   * Query the bounding rectangle of the area to highlight.
   *
   * @return bounding rectangle of the area to highlight.
   */
  public Rectangle getHighlightBounds()
  {
    final double sx=getHorizontalScale();
    final double sy=getVerticalScale();
    return new Rectangle(
      borderNW.width+(int)Math.floor(sx*highlightXmin),
      borderNW.height+(int)Math.floor(sy*highlightYmin),
      (int)Math.ceil(sx*(highlightXmax-highlightXmin)),
      (int)Math.ceil(sy*(highlightYmax-highlightYmin)));
  }

  /**
   * Take the list of highlighting rectangles and transform it to a list of
   * non-overlapping rectangles covering the same regions. (Since we use XOR
   * highlighting, overlapping rectangles "cancel" each other out.)
   *
   * @param index of visible image to highlight
   * @param list of bounds to highlight.
   */
  public void setHighlightList(final int index, final Vector list)
  {
    highlightList.setSize(0);
    highlightXmin=highlightYmin=highlightXmax=highlightYmax=0;

    if((list != null)&&(index>=0)&&(index<boundArray.length))
    {
      final Rectangle bounds=boundArray[index];
      if(bounds != null)
      {
        final DjVuInfo info=infoArray[index];
        final double s=1D/(double)info.dpi;
        final double sx=1D/getHorizontalScale();
        final double sy=1D/getVerticalScale();
        for(int pos = 0; pos < list.size();)
        {
          Object current = list.elementAt(pos++);
          double [] ncurrent;
          if(current instanceof GRect)
          {
            final GRect gcurrent = (GRect)current;
            ncurrent=new double[4];
            ncurrent[0]=(sx*bounds.x)+(s*(double)gcurrent.xmin);
            ncurrent[1]=(sy*bounds.y)+(s*(double)(info.height-gcurrent.ymax));
            ncurrent[2]=(sx*bounds.x)+(s*(double)gcurrent.xmax);
            ncurrent[3]=(sy*bounds.y)+(s*(double)(info.height-gcurrent.ymin));
            current = ncurrent;
          }
          else
          {
            ncurrent=(double[])current;
          }
          addHighlightArea(ncurrent,0);
        }
      }
    }
  }

  /**
   * Get a segmented Image.
   *
   * @param parent Parent component to create the Image in.
   * @param bounds Segmentation bounds.
   *
   * @return The newly created Image.
   */
  public Image[] getImage(
    final Component parent,
    final Rectangle bounds)
  {
    final Image[] retval = {null};
    retval[0] =
      parent.createImage(
        new DjVuFilter(
          bounds,
          getSize(),
          djvuPageArray[0],
          false).getImageProducer());

    return retval;
  }

  /**
   * Query the size of the DjVuPage array.
   */
  public final int getIndexMax()
  {
      return djvuPageArray.length;
  }
  
  /**
   * Query the size and other page information as indicated in the INFO chunk.
   *
   * @param index of the page to query
   *
   * @return the page information
   */
  public final DjVuInfo getDjVuInfo(final int index)
  {
      return infoArray[index];
  }
  
  /**
   * Create a new instance of this class, scaled to the specified size.
   *
   * @param width Scaled width.
   * @param height Scaled height.
   *
   * @return a new instance of this class.
   */
  public DjVuImage getScaledInstance(
    int width,
    int height)
  {
    if(height <= 0)
    {
      if(width <= 0)
      {
        return null;
      }

      height =
        (int)(((long)infoArray[0].height * (long)width) / (long)infoArray[0].width);
    }
    else if(width <= 0)
    {
      width =
        (int)(((long)infoArray[0].width * (long)height) / (long)infoArray[0].height);
    }

    final DjVuImage retval = new DjVuImage(djvuPageArray, favorFast,width, height);

    return retval;
  }

  /**
   * Create a new instance of this class, scaled to the specified size.
   *
   * @param scale Scaling, relative to DPI.
   *
   * @return a new instance of this class.
   */
  public DjVuImage getScaledInstance(int scale)
  {
    return new DjVuImage(
      djvuPageArray,
      favorFast,
      (((infoArray[0].width * scale) + infoArray[0].dpi) - 1) / infoArray[0].dpi,
      (((infoArray[0].height * scale) + infoArray[0].dpi) - 1) / infoArray[0].dpi);
  }

  /**
   * Query the scaled size of the DjVuPage.
   *
   * @return the scaled size of the DjVuPage.
   */
  public Dimension getSize()
  {
    return getBounds().getSize();
  }

  /**
   * Query the DjVuText codec for this page. Use a maxWait value of 0L to
   * wait until the codec is available, or the document is decoded.
   *
   * @param maxWait The maximum amount of time to wait.  (In milliseconds.)
   *
   * @return the DjVuText codec.
   */
  public Codec getTextCodec(final int index,final long maxWait)
  {
    if((index >= 0)&&(index<djvuPageArray.length))
    {
      final DjVuPage page=djvuPageArray[index];
      if(page != null)
      {
        return page.waitForCodec(page.textLock,maxWait);
      }
    }
    return null;
  }

  /**
   * Called to create one or more images to draw to.
   *
   * @param parent Component being drawn to.
   * @param g Graphics device to draw to.
   * @param observer The ImageObserver monitoring progressive rendering.
   */
  public void draw(
    final Component     parent,
    final Graphics      g,
    final ImageObserver observer)
  {
    //DjVuOptions.err.println("bcr paintComponent filter="+filter);
    clearBorder(
      parent,
      getBounds(),
      g);

    final Shape clip   = g.getClip();
    if(filterBounds.isEmpty())
    {
      setBufferBounds(transformBounds(clip));
    }

    for(int j=0;j<filterArray.length;j++)
    {
      final DjVuFilter filter=filterArray[j];
      if(filter != null)
      {
        final Rectangle bounds=filter.getBounds();
        final Rectangle b=boundArray[j];
        final int x0=borderNW.width+b.x;
        final int y0=borderNW.height+b.y;
        g.clipRect(x0,y0,b.width,bounds.y+bounds.height);
//        if(g.hitClip(x0+bounds.x,y0+bounds.y,bounds.width,bounds.height))
//        {
          g.drawImage(
            filter.getImage(parent),
            x0+bounds.x,
            y0+bounds.y,
            observer);
//        }
        g.setClip(clip);
      }
    }
  }

  /**
   * Transform the specified coordinates.
   *
   * @param index visible image index
   * @param rect rectangle to transform.
   * @param scaled the results of the transform.
   */
  public void transformRectangle(
    final int index,
    final Rectangle rect,
    final Rectangle scaled)
  {
    if(rect.isEmpty() || (index < 0)||(index > boundArray.length)||(boundArray[index] == null))
    {
      scaled.setBounds(rect.x, rect.y, 0, 0);
    }
    else
    {
      final Rectangle bound=boundArray[index];
      final DjVuInfo info=infoArray[index];
      final double sx=(double)bound.width/(double)info.width;
      final double sy=(double)bound.height/(double)info.height;
      final double xmin = sx*(double)rect.x;
      final double ymin = sx*(double)rect.y;
      final double xmax=sx*(double)(rect.x+rect.width);
      final double ymax=sy*(double)(rect.y+rect.height);
      scaled.setBounds(
        borderNW.width + boundArray[index].x + (int)Math.floor(xmin+0.000001),
        borderNW.height + boundArray[index].y +  (int)Math.floor(ymin+0.000001),
        (int)Math.ceil(0.999999+xmax - xmin),
        (int)Math.ceil(0.999999+ymax - ymin));
    }
  }

  /**
   * Transform the specified coordinates.
   *
   * @param index visible image to transform from
   * @param rect rectangle to transform.
   * @param scaled the results of the transform.
   */
  public void transformRectangle(
    final int index,
    final GRect     rect,
    final Rectangle scaled)
  {
    scaled.setBounds(
      rect.xmin,
      infoArray[index].height - rect.ymax,
      rect.width(),
      rect.height());
    transformRectangle(index,scaled, scaled);
  }

  // Query the vector of areas to highlight.
  //
  // @return vector of areas to highlight.
  //
  private Vector getHighlightList()
  {
    return highlightList;
  }

  private void addHighlightArea(
    final double [] area,
    int              pos)
  {
    final Vector highlightList = getHighlightList();
    synchronized(highlightList)
    {
      if(
       ((area[0]<=highlightXmin)?(area[2]> highlightXmin):(area[0]<highlightXmax))
       &&((area[1]<=highlightYmin)?(area[3]> highlightYmin):(area[1]<highlightYmax)))
      {
        while(pos < highlightList.size())
        {
          final double [] current =
            (double[])highlightList.elementAt(pos++);
 
          if(
           ((area[0]<=current[0])?(area[2]>current[0]):(area[0]<current[2]))
           &&((area[1]<=current[1])?(area[3]>current[1]):(area[1]<current[2])))
          {
            if(
              (current[0]<=area[0])
              &&(current[1]<=area[1])
              &&(current[2]>=area[2])
              &&(current[3]>=area[3]))
            {
              return;
            }

            if(
              (current[0]>=area[0])
              &&(current[1]>=area[1])
              &&(current[2]<=area[2])
              &&(current[3]<=area[3]))
            {
              highlightList.removeElementAt(--pos);
            }
            else if(
              (current[0] == area[0])
              && (current[2] == area[2]))
            {
              area[1]=Math.min(current[1], area[1]);
              area[3]=Math.max(current[3], area[3]);
              highlightList.removeElementAt(--pos);
            }
            else if(
              (current[1] == area[1])
              && (current[3] == area[3]))
            {
              area[0]=Math.min(current[0], area[0]);
              area[2]=Math.max(current[2], area[2]);
              highlightList.removeElementAt(--pos);
            }
            else
            {
              if(area[0]<current[0])
              {
                if((area[2]<=current[2])&&(area[1]>=current[1])&&(area[3]<=current[3]))
                {
                  area[3]=current[3];
                  continue;
                }
                final double [] left={area[0],area[1],current[0],area[3]};
                addHighlightArea(left,pos);
                area[0]=current[0];
              }
              if(area[2]>current[2])
              {
                if((area[1]>=current[1])&&(area[3]<=current[3]))
                {
                  area[0]=current[2];
                  continue;
                }
                final double [] right={current[2],area[1],area[2],area[3]};
                addHighlightArea(right,pos);
                area[2]=current[2];
              }
              if(
                (current[0] == area[0])
                && (current[2] == area[2]))
              {
                area[1]=Math.min(current[1], area[1]);
                area[3]=Math.max(current[3], area[3]);
                highlightList.removeElementAt(--pos);
              }
              else if(area[1]<current[1])
              {
                if(area[3] > current[3])
                {
                  final double [] top={area[0],area[1],area[2],current[1]};
                  addHighlightArea(top,pos);
                  area[1]=current[3];
                }
                else
                {
                  area[3]=current[1];
                }
              }
              else if(area[3]>current[3])
              {
                area[1]=current[3];
              }
              else
              {
                return;
              }
            }
          }
        }
      }

      highlightList.addElement(area);

      if(highlightList.size() == 1)
      {
        highlightXmin=area[0];
        highlightYmin=area[1];
        highlightXmax=area[2];
        highlightYmax=area[3];
      }
      else
      {
        if(highlightXmin > area[0] ) 
            highlightXmin=area[0];
        if(highlightYmin > area[1] ) 
            highlightYmin=area[1];
        if(highlightXmax < area[2] ) 
            highlightXmax=area[2];
        if(highlightYmax < area[3] ) 
            highlightYmax=area[3];
      }
    }
  }

  // Called to clear the area around the image.
  //
  // @param parent The component window being drawn to.
  // @param imageBounds The scaled image size.
  // @param g Graphics device to draw to.
  //
  private void clearBorder(
    final Component parent,
    final Rectangle imageBounds,
    final Graphics  g)
  {
    final Dimension parentSize = parent.getSize();
    setBorder(parentSize, imageBounds);

    if(borderNW.height > 0)
    {
      g.clearRect(
        0,
        0,
        imageBounds.width + borderSE.width + borderNW.width,
        borderNW.height);
    }

    if(borderNW.width > 0)
    {
      g.clearRect(0, borderNW.height, borderNW.width, imageBounds.height);
    }

    if(borderSE.width > 0)
    {
      g.clearRect(
        imageBounds.width + borderNW.width,
        borderNW.height,
        borderSE.width,
        imageBounds.height);
    }

    if(borderSE.height > 0)
    {
      g.clearRect(
        0,
        imageBounds.height + borderNW.height,
        imageBounds.width + borderSE.width + borderNW.width,
        borderSE.height);
    }
  }

  /** Called to paint the highlight areas onto the image.
   *
   * @param g graphics object to draw.
   * @param useXOR true if XOR highlighting should be used.
   */
  public void drawHighlight(
    final Graphics g,
    final boolean  useXOR)
  {      
    final Vector    highlightList = getHighlightList();
    synchronized(highlightList)
    {
//      final Rectangle scaled = new Rectangle();

      final double sx=getHorizontalScale();
      final double sy=getVerticalScale();
      for(int pos = 0; pos < highlightList.size();)
      {
        final double [] rect=(double[])highlightList.elementAt(pos++);
        final int xmin=borderNW.width+(int)Math.floor(sx*rect[0]);
        final int ymin=borderNW.height+(int)Math.floor(sy*rect[1]);
        final int xmax=borderNW.width+(int)Math.ceil(sx*rect[2]);
        final int ymax=borderNW.height+(int)Math.ceil(sy*rect[3]);
//        final Rectangle scaled=((SimpleArea)highlightList.elementAt(pos++)).getBounds();
//        transformRectangle(
//          ((SimpleArea)highlightList.elementAt(pos++)).getBounds(),
//          scaled);

        if(useXOR)
        {
          fillRect(g,XOR_HILITE,50,xmin, ymin, xmax, ymax);
        }

        g.setPaintMode();
        g.setColor(BLUE);
        g.drawRect(xmin, ymin, xmax-xmin, ymax-ymin);
      }
    }
  }

  // Complete the bounding rectangle of the clip after removing the border.
  //
  // @param clip The clip to transform.
  //
  // @return a bounding rectangle.
  //
  private Rectangle transformBounds(final Shape clip)
  {
    Rectangle retval = clip.getBounds();
    retval.x -= borderNW.width;

    if(retval.x < 0)
    {
      retval.width += retval.x;
    }

    retval.y -= borderNW.height;

    if(retval.y < 0)
    {
      retval.height += retval.y;
    }

    return getBounds().intersection(retval);
  }

  // Confirm the specified Rectangle is contained in the image.
  //
  // @param bounds rectangle to verify.
  // 
  private void validateBounds(final Rectangle bounds)
  {
    validateBounds(bounds.x, bounds.y, bounds.width, bounds.height);
  }

  // Confirm the specified Rectangle is contained in the image.
  //
  // @param x coordinate of the rectangle to verify.
  // @param y coordinate of the rectangle to verify.
  // @param width of the rectangle to verify.
  // @param height of the rectangle to verify.
  //
  // @throws IllegalArgumentException If the rectangle is not contained
  //         within the image.
  //
  private void validateBounds(
    int x,
    int y,
    int width,
    int height)
  {
    final Rectangle bounds = getBounds();

    if(
      (x >= bounds.x)
      && (y >= bounds.y)
      && ((x + width) <= (bounds.x + bounds.width))
      && ((y + height) <= (bounds.y + bounds.height)))
    {
      throw new IllegalArgumentException(
        "Invalid target window (" + x + "," + y + "," + width + "," + height
        + "),getBounds().width=" + getBounds().width + ",getBounds().height="
        + getBounds().height + ")");
    }
  }

  /** 
   * Set the color as specified and then call g.fillRect.
   *
   * @param g Graphics item to use.
   * @param color RGB color to use
   * @param opacity Level of opacity from 0 to 100.
   * @param xmin left edge
   * @param ymin top edge
   * @param xmax right edge
   * @param ymax bottom edge
   *
   */
  public void fillRect(
          final Graphics g,
          int color,
          int opacity,
          final int xmin,
          final int ymin,
          final int xmax,
          final int ymax)
  {
    if(g!= null)
    {
      switch(color)
      {
        case 0xFFFFFFFF:
        {
          break;
        }
        case XOR_HILITE:
        {
          if(!BROKEN_XOR)
          {
            g.setColor(DjVuImage.BLACK);
            g.setXORMode(DjVuImage.WHITE);
            g.fillRect(xmin,ymin,xmax-xmin,ymax-ymin);
            break;
          }
          color=0xffff00;
          opacity=50;
        }
        default:
        {
          //final Integer pixel=new Integer((((opacity*255)/100)<<24)|(color&0xffffff));
          Color c=createColor(color, (opacity*255)/100);
          if(c!=null)
          {
            g.setPaintMode();          
            g.setColor(c);
          }
          else
          {
            c=new Color((color^0xffffff)&0xffffff);
            g.setColor(DjVuImage.BLACK);
            g.setXORMode(c);
          }
          g.fillRect(xmin,ymin,xmax-xmin,ymax-ymin);
//          Image x=(Image)imageMap.get(pixel);
//          if(x == null)
//          {
//            final int [] p={pixel.intValue()};
//            x=djvuBean.createImage(new java.awt.image.MemoryImageSource(1,1,DjVuFilter.RGB_MODEL,p,0,1));
//            imageMap.put(pixel,x);
//          }
//          g.setPaintMode();
//          g.setColor(DjVuImage.BLACK);
//          g.drawImage(x,xmin,ymin,xmax-xmin,ymax-ymin,djvuBean);
          break;
        }
      }
    }
  }
  

}
