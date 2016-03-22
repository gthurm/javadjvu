//C- -------------------------------------------------------------------
//C- Java DjVu (r) (v. 0.8)
//C- Copyright (c) 2005 Internet Archive Inc.  All Rights Reserved.
//C-
//C- This software is subject to, and may be distributed under, the
//C- GNU General Public License, Version 2. The license should have
//C- accompanied the software or you may obtain a copy of the license
//C- from the Free Software Foundation at http://www.fsf.org .
//C-
//C- The computer code originally released by Internet Archive under this
//C- license and unmodified by other parties is deemed "the INTERNET ARCHIVE
//C- ORIGINAL CODE."  Subject to any third party intellectual property
//C- claims, Internet Archive grants recipient a worldwide, royalty-free,
//C- non-exclusive license to make, use, sell, or otherwise dispose of
//C- the INTERNET ARCHIVE ORIGINAL CODE or of programs derived from the
//C- INTERNET ARCHIVE ORIGINAL CODE in compliance with the terms of the GNU
//C- General Public License.   This grant only confers the right to
//C- infringe patent claims underlying the INTERNET ARCHIVE ORIGINAL CODE to
//C- the extent such infringement is reasonably necessary to enable
//C- recipient to make, have made, practice, sell, or otherwise dispose
//C- of the INTERNET ARCHIVE ORIGINAL CODE (or portions thereof) and not to
//C- any greater extent that may be necessary to utilize further
//C- modifications or combinations.
//C-
//C- The INTERNET ARCHIVE ORIGINAL CODE is provided "AS IS" WITHOUT WARRANTY
//C- OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
//C- TO ANY WARRANTY OF NON-INFRINGEMENT, OR ANY IMPLIED WARRANTY OF
//C- MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
//C-
//C- In addition, as a special exception, Internet Archive gives permission
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
import java.util.*;


/**
 * DjVuFilter objects are used to present a segment of a DjVu image at an
 * arbitrary resolution.  In order to keep the code simple, this filter is
 * used on a 1 pixel memory source to wrap into an ImageProducer.  This
 * avoids the need to implement low level methods to deal directly with
 * ImageConsumer objects.  The animated flag may be used for progressive
 * rendering and scrolling.  When animated, up to two additional threads may be
 * used to offload work.  The first thread updates the buffered pixels after
 * each progressive update and after move() method calls.  The second thread
 * passes the pixels to the ImageConsumer.
 *
 * @author Bill C. Riemers, Foxtrot Technologies Inc.
 */
public final class DjVuFilter
  extends ImageFilter
  implements Runnable
{
  //~ Static fields/initializers ---------------------------------------------

  // Index for the rectangle of renderable pixels.
  private static final int INTEREST_RECT = 0;

  // Index for the rectangle of renderable pixels and border.
  private static final int FULL_RECT = 1;

  // Index for the rectangle of renderable pixels and 
  // pixels cached for quicker scrolling.
  private static final int BUFFER_RECT = 2;

  // Index of the subsampled page.
  private static final int PAGE_RECT = 3;

  // Index of the rectangle of the last rendered rectangle.
  // (Does not work.)
  private static final int LAST_RENDERED_RECT = 4;

  // Index of the thread handling progressive decoding.
  private static final int PROGRESSIVE_THREAD = 0;

  // Index of the thread handling scroll buffering.
  private static final int SCROLL_THREAD = 0;

  // Index of the thread copying pixels to the ImageConsumer.
  private static final int NEWPIXEL_THREAD = 1;

  // Index of the flag indicating progressive decoding is 
  // in progress.
  private static final int PROGRESSIVE_FLAG = 0;

  // Index of the flag indicating that pixels are ready
  // to copy.
  private static final int NEWPIXELS_FLAG = 1;

  /** RGB Color model. */
  public static final ColorModel RGB_MODEL = ColorModel.getRGBdefault();

  /** Gray Color model. */
  public static final ColorModel GRAY_MODEL =
    new DirectColorModel(8, 0xff, 0xff, 0xff);

  //~ Instance fields --------------------------------------------------------

  /** The DjVuInfo Codec */
  public final DjVuInfo info;

  /** The DjVuPage being displayed. */
  public final DjVuPage page;

  /** True if separate threads may be used. */
  public final boolean animated;

  /** The new position of the upper left corner. */
  protected final Point position = new Point();

  /** The full height of the subsampled segment. */
  protected final int fullHeight;

  /** The full width of the subsampled segment. */
  protected final int fullWidth;

  /** The current subsample rate. */
  protected final int subsample;
  
  /** Horizontal scale factor. */
  protected final double sx;
  
  /** Vertical scale factor. */
  protected final double sy;


  /** The size of the page at the target resolution. */
  private final Dimension pageSize;

  /** Table of Image objects referenced by parent. */
  private final Hashtable imageTable = new Hashtable();

  /** The one pixel MemoryImageSource object. */
  private final MemoryImageSource source;
  
  /** The bounding rectangle of the segment being displayed. */
  private final Rectangle bounds;

  /** An array of boolean flags. */
  private final boolean[] flags = {false, false};

  /** An array of cached pixels as GMap objects. */
  private final GMap[] mapArray = {null,null};

  /** An array of ImageProducer objects using this filter. */
  private final ImageProducer[] producer   = {null};
  private final Object[]        reuseArray = {null};

  /** An array of rectangle computed for the segmentation. */
  private final GRect[] segmentArray = {null, null, null, null, null};

  /** An array of threads used by this filter. */
  private final Thread[] threadArray = {null, null};

  /** The height of the buffered pixel map. */
  private final int bufferHeight;

  /** The width of the buffered pixel map. */
  private final int bufferWidth;

  /** The size factor to increase the fullWidth for buffering. */
  private final int deltaX;

  /** The size factor to increase the fullHeight for buffering. */
  private final int deltaY;

  /** The fill color used for borders. */
  private int fillColor = 0xff808080;

  private boolean firstTime=true;
  
  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new instance of DjVuFilter
   * 
   * @param bounds The bounding rectangle of the desired segment.
   * @param pageSize The stretched page size.
   * @param page The DjVuPage to render.
   * @param animated True if multiple threads should be used to allow 
   *                 progressive rendering and move method calls.
   */
  public DjVuFilter(
    final Rectangle bounds,
    final Dimension pageSize,
    final DjVuPage  page,
    final boolean   animated)
  {
    info              = page.getInfoWait();
    this.bounds       = new Rectangle(bounds.x,bounds.y,bounds.width,bounds.height);
    this.page         = page;
    this.pageSize     = pageSize;
    this.animated     = animated;
    source            = new MemoryImageSource(
        1,
        1,
        GRAY_MODEL,
        new byte[1],
        0,
        1);
    source.setAnimated(animated);
    source.setFullBufferUpdates(animated);
    flags[PROGRESSIVE_FLAG]   = animated;
    position.x                = bounds.x;
    position.y                = bounds.y;

    int subsample             = 12;

    GRect p = null;
    for(;; subsample--)
    {
      p =
        segmentArray[PAGE_RECT] =
          new GRect(
            0,
            0,
            ((info.width + subsample) - 1) / subsample,
            ((info.height + subsample) - 1) / subsample);

      if(
        (subsample == 1)
        || ((p.xmax >= pageSize.width) && (p.ymax >= pageSize.height)))
      {
        break;
      }
    }

    this.subsample   = subsample;
    sx=((double)info.width)/((double)pageSize.width*(double)subsample);
    sy=((double)info.height)/((double)pageSize.height*(double)subsample);
    fullWidth = (int)Math.ceil(sx*(double)bounds.width);
    fullHeight = (int)Math.ceil(sy*(double)bounds.height);

    if(animated)
    {
      bufferWidth = Math.min((int)Math.ceil(sx*(double)bounds.width*8D/7D),p.width());
      bufferHeight =Math.min((int)Math.ceil(sy*(double)bounds.height*8D/7D),p.height());
      deltaX=Math.max((1+bufferWidth-fullWidth)/2,0);
      deltaY=Math.max((1+bufferHeight-fullHeight)/2,0);

      if(page.isDecoding())
      {
        threadArray[PROGRESSIVE_THREAD] = new Thread(this);
        threadArray[PROGRESSIVE_THREAD].start();
      }
      else
      {
        final GRect bufferRect = segment();
        mapArray[0] = mapArray[1] = page.getMap(
          bufferRect,
          subsample,
          null);
        segmentArray[BUFFER_RECT]          = bufferRect;
      }
    }
    else
    {
      deltaX             = 0;
      deltaY             = 0;
      bufferWidth        = fullWidth;
      bufferHeight       = fullHeight;
    }
//    if(animated) DjVuOptions.err.println("bcr this() done");
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Return the bounding rectangle of the segment.
   *
   * @return The rectangle bounding the segment.
   */
  public Rectangle getBounds()
  {
    return bounds;
  }

  /**
   * Called by the image producer to set the image size.  The actual values
   * are ignored, since the producer is always a 1 pixel source.
   *
   * @param w ignored
   * @param h ignored
   */
  public void setDimensions(
    final int w,
    final int h)
  {
//    if(animated) DjVuOptions.err.println("bcr setDimensions");
    super.setDimensions(fullWidth, fullHeight);

//    if(animated) DjVuOptions.err.println("bcr setDimensions done");
  }

  /**
   * Call to lookup, or create an image with the specified parent.
   *
   * @param parent The component to call createImage on.
   *
   * @return An Image representation of this object.
   */
  public Image getImage(final Component parent)
  {
//    if(animated) DjVuOptions.err.println("bcr getImage");
    Image retval = (Image)DjVuObject.getFromReference(imageTable.get(parent));

    if(retval == null)
    {
      retval = parent.createImage(getImageProducer());
      imageTable.put(
        parent,
        DjVuObject.createSoftReference(retval, retval));
    }

//    if(animated) DjVuOptions.err.println("bcr getImage done");
    return retval;
  }

  /**
   * Called to create an ImageProducer which uses this filter.
   *
   * @return The ImageProducer for this filter.
   */
  public ImageProducer getImageProducer()
  {
//    if(animated) DjVuOptions.err.println("bcr getImageProducer");
    if(producer[0] == null)
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(mapArray)
      {
        if(producer[0] == null)
        {
          //DjVuOptions.err.println("bcr getImageProducer "+this);
          final ImageProducer p = new FilteredImageSource(source, this);

          if((fullWidth != bounds.width) || (fullHeight != bounds.height))
          {
            producer[0] =
              new FilteredImageSource(
                p,
                (fullWidth < bounds.width)
                ? new ReplicateScaleFilter(bounds.width, bounds.height)
                : new AreaAveragingScaleFilter(bounds.width, bounds.height));
          }
          else
          {
            producer[0] = p;
          }
        }
//        DjVuObject.checkLockTime(lockTime,10000);
      }
    }

//    if(animated) DjVuOptions.err.println("bcr getImageProducer done");
    return producer[0];
  }

  /**
   * Sets the array of pixels from the available GPixmap and fillColor for
   * borders.
   *
   * @param x (ignored)
   * @param y (ignored)
   * @param w (ignored)
   * @param h (ignored)
   * @param model (ignored)
   * @param pixels (ignored)
   * @param off (ignored)
   * @param scansize (ignored)
   */
  public void setPixels(
    int        x,
    int        y,
    int        w,
    int        h,
    ColorModel model,
    byte[]     pixels,
    int        off,
    int        scansize)
  {
    //if(animated) DjVuOptions.err.println("bcr setPixels "+this);
    GMap  map        = null;
    GRect bufferRect = null; // This corresponds to the position of the map pixels.
    GRect mapRect    = null; // This corresponds to the position of the map pixels.
    GRect fullRect   = null; // This includes all map pixels and border pixels.

    if((!animated) && (mapArray[0] == null))
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(mapArray)
      {
        if(mapArray[0] == null)
        {
          try
          {
            page.waitForCodec(page.doneLock, 0L);
          }
          catch(final Throwable ignored) {}

          bufferRect                         = segment();
          map                                = page.getMap(
              bufferRect,
              subsample,
              null);
          segmentArray[BUFFER_RECT]          = bufferRect;
          segmentArray[LAST_RENDERED_RECT]   = null;
          mapArray[0]                        = map;
          mapArray[1]                        = null;
          mapRect                            = segmentArray[INTEREST_RECT];
          bufferRect                         = segmentArray[BUFFER_RECT];
          fullRect                           = segmentArray[FULL_RECT];
        }
//        DjVuObject.checkLockTime(lockTime,10000);
      }
    }

    if(map == null)
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(mapArray)
      {
        map          = mapArray[0];
        mapRect      = segmentArray[INTEREST_RECT];
        bufferRect   = segmentArray[BUFFER_RECT];
        fullRect     = segmentArray[FULL_RECT];

        if(
          (map == null)
          || (fullRect == null)
          || (bufferRect == null)
          || fullRect.isEmpty()
          || !bufferRect.contains(mapRect))
        {
//          if(firstTime&&!page.isDecoding())
          if(firstTime)
          {
              mapRect=new GRect();
          }
          else
          {
            return;
          }
        }
        else
        {
          segmentArray[LAST_RENDERED_RECT] = fullRect;
        }
        mapArray[1]=null;
//        DjVuObject.checkLockTime(lockTime,10000);
      }
    }
    firstTime=false;
    //if(animated) DjVuOptions.err.println("bcr 1 setPixels "+this);

//    DjVuOptions.out.println("bcr gotit");
    final int[] row = new int[fullWidth];

    if(mapRect.isEmpty())
    {
      synchronized(mapArray)
      {
         mapArray[1]=mapArray[0];
      }
      for(int i = 0; i < fullWidth;)
      {
        row[i++] = fillColor;
      }

      for(int fullY = 0; fullY < fullHeight;)
      {
        super.setPixels(
          0,
          fullY++,
          fullWidth,
          1,
          RGB_MODEL,
          row,
          0,
          fullWidth);
      }
    }
    else
    {
      // This is easy.  mapRect corresponds the the map rectangle
      // and is always contained within fullRect which includes any
      // border pixels.
      final int mapWidth  = mapRect.width();
      final int offset    = mapRect.xmin - fullRect.xmin;
      int       fullY     = 0;
      int       mapHeight = mapRect.height();
      int       hmax      = fullRect.ymax - mapRect.ymax;
      int       hmin      = mapRect.ymin - fullRect.ymin;

      // This may seem backwards, but GRect coordinates are bottom up not top down.
      if(hmax > 0)
      {
        for(int i = 0; i < fullWidth;)
        {
          row[i++] = fillColor;
        }

        do
        {
          super.setPixels(
            0,
            fullY++,
            fullWidth,
            1,
            RGB_MODEL,
            row,
            0,
            fullWidth);
        }
        while(--hmax > 0);
      }

      if(mapHeight > 0)
      {
        for(int i = 0; i < offset;)
        {
          row[i++] = fillColor;
        }

        for(int i = offset + mapWidth; i < fullWidth;)
        {
          row[i++] = fillColor;
        }

        int       mapY = bufferRect.ymax - mapRect.ymax;
        final int mapX = mapRect.xmin - bufferRect.xmin;

        do
        {
          map.fillRGBPixels(
            mapX,
            mapY++,
            mapWidth,
            1,
            row,
            offset,
            fullWidth);
          super.setPixels(
            0,
            fullY++,
            fullWidth,
            1,
            RGB_MODEL,
            row,
            0,
            fullWidth);
        }
        while(--mapHeight > 0);
      }
      synchronized(mapArray)
      {
         mapArray[1]=mapArray[0];
      }

      if(hmin > 0)
      {
        for(int i = 0; i < fullWidth;)
        {
          row[i++] = fillColor;
        }

        do
        {
          super.setPixels(
            0,
            fullY++,
            fullWidth,
            1,
            RGB_MODEL,
            row,
            0,
            fullWidth);
        }
        while(--hmin > 0);
      }
    }
  }

  /**
   * Sets the array of pixels from the available GPixmap and fillColor for
   * borders.
   *
   * @param x the x-coordinate of the upper-left corner of the region of
   *        pixels (ignored)
   * @param y the y-coordinate of the upper-left corner of the region of
   *        pixels (ignored)
   * @param w the width of the region of pixels (ignored)
   * @param h the height of the region of pixels (ignored)
   * @param model the color model of the input data  (ignored)
   * @param pixels the array of pixels (ignored)
   * @param off the offset into the pixel array (ignored)
   * @param scansize the distance from one row of pixels to the next in the
   *        array (ignored)
   */
  public void setPixels(
    int        x,
    int        y,
    int        w,
    int        h,
    ColorModel model,
    int[]      pixels,
    int        off,
    int        scansize)
  {
    setPixels(x, y, w, h, model, (byte[])null, off, scansize);
  }

  /**
   * Return the scaled page size.
   *
   * @return The scaled page size.
   */
  public Dimension getSize()
  {
    return pageSize;
  }

  /**
   * Called to move the upper left hand corner of the image segment. If the
   * useThread option is set, this method will return right away, otherwise
   * the method waits until a new pixels are sent to the ImageConsumer.
   *
   * @param x Left to right x coordinate.
   * @param y Top down y coordinate.
   *
   * @throws IllegalStateException if not animated.
   */
  public void move(
    final int x,
    final int y)
  {
//    if(animated) DjVuOptions.err.println("bcr move");
    boolean     needUpdate = false;
    final Point p = position;

    if((p.x != x) || (p.y != y))
    {
      if(!animated)
      {
        throw new IllegalStateException(
          DjVuFilter.class.getName() + " animated=false");
      }
      
//      final long lockTime=System.currentTimeMillis();
      synchronized(mapArray)
      {
        if((p.x != x) || (p.y != y))
        {
          final GRect fullRect = segmentArray[FULL_RECT];
          p.x   = x;
          p.y   = y;
          segment();

          if(
            (source != null)
            && (fullRect != null)
            && !fullRect.equals(segmentArray[FULL_RECT]))
          {
            needUpdate = true;

//            DjVuOptions.err.println("bcr mapArray.notifyAll");
            mapArray.notifyAll();

            if(threadArray[SCROLL_THREAD] == null)
            {
              threadArray[SCROLL_THREAD] = new Thread(this);
              threadArray[SCROLL_THREAD].start();
            }
          }
//          DjVuObject.checkLockTime(lockTime,10000);
        }
      }

      if(needUpdate)
      {
        newPixels();
      }
    }
  }

  /**
   * This is the run method used for all threads.
   */
  public void run()
  {
    try
    {
      final Thread current = Thread.currentThread();

      if(current == threadArray[NEWPIXEL_THREAD])
      {
        newPixelRun();
      }

      if(
        (current == threadArray[PROGRESSIVE_THREAD])
        && flags[PROGRESSIVE_FLAG])
      {
        progressiveRun();
      }

      if(threadArray[SCROLL_THREAD] == current)
      {
        scrollRun();
      }
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
    }
  }

  /**
   * Called to indicate newPixels are available.  If multiple threads are
   * used, the NEWPIXELS_FLAG is set and then this method returns.
   * Otherwise, this method will not return until the pixels are sent to the
   * ImageConsumer.
   */
  protected void newPixels()
  {
//    if(animated) DjVuOptions.err.println("bcr newPixels");
    flags[NEWPIXELS_FLAG] = true;

    if(source != null)
    {
      if(animated)
      {
//        final long lockTime=System.currentTimeMillis();
        synchronized(source)
        {
          source.notifyAll();

          if(threadArray[NEWPIXEL_THREAD] == null)
          {
            threadArray[NEWPIXEL_THREAD] = new Thread(this);
            threadArray[NEWPIXEL_THREAD].start();
          }
//          DjVuObject.checkLockTime(lockTime,10000);
        }
      }
      else
      {
        newPixels();
      }
    }
  }

  /**
   * This is the run method for the newPixel thread.
   */
  private void newPixelRun()
  {
    final Thread current=Thread.currentThread();
    //if(animated) DjVuOptions.err.println("bcr newPixelRun "+this);
    if(source != null)
    {
      for(int i = 0;;)
      {
        if(threadArray[NEWPIXEL_THREAD] != current)
        {
          break;
        }

        if(flags[NEWPIXELS_FLAG])
        {
          i                       = 0;
          flags[NEWPIXELS_FLAG]   = false;

//          if(animated) DjVuOptions.err.println("bcr call source.newPixels");
          source.newPixels();

          try
          {
            Thread.sleep(20L);
          }
          catch(final Throwable ignored) {}
        }
        else
        {
//          final long lockTime=System.currentTimeMillis();
          synchronized(mapArray)
          {
            if(flags[NEWPIXELS_FLAG])
            {
              continue;
            }

            if(i++ > 0)
            {
              threadArray[NEWPIXEL_THREAD] = null;

              break;
            }

            try
            {
              source.wait(200L);
            }
            catch(final Throwable ignored) {}
//            DjVuObject.checkLockTime(lockTime,10000);
          }
        }
      }
    }
  }

  /**
   * This is the run method for the progressive thread.
   */
  private void progressiveRun()
  {
    final Thread current=Thread.currentThread();
    //if(animated) DjVuOptions.err.println("bcr progressiveRun "+this);
    GRect   lastFullRect = null;
    boolean repeat;

    do
    {
      final boolean decoding = page.isDecoding();
      repeat = decoding;

      GMap  map;
      GRect fullRect;
      GRect bufferRect;
      {
//        final long lockTime=System.currentTimeMillis();
        synchronized(mapArray)
        {
          map          = mapArray[0];
          bufferRect   = segment();
          fullRect     = segmentArray[FULL_RECT];
//          DjVuObject.checkLockTime(lockTime,10000);
        }
      }
      
      GMap newMap = page.updateMap(map, bufferRect, subsample);

      if((newMap != null) && (map != newMap))
      {
//        final long lockTime=System.currentTimeMillis();
        synchronized(mapArray)
        {
          reuseArray[0] = DjVuObject.createSoftReference(mapArray[1], mapArray[1]);
          mapArray[0] = mapArray[1] = newMap;
          segmentArray[BUFFER_RECT]          = bufferRect;
          segmentArray[LAST_RENDERED_RECT]   = lastFullRect = null;
//          DjVuObject.checkLockTime(lockTime,10000);
        }

        newPixels();
        lastFullRect = fullRect;

        if(DjVuOptions.COLLECT_GARBAGE)
        {
          System.gc();
        }

        repeat = true;
      }
      else if(!fullRect.equals(lastFullRect))
      {
        newPixels();
        lastFullRect = fullRect;
      }

      if(decoding)
      {
        page.waitForCodec(page.progressiveLock, 0L);

        if(newMap != null)
        {
//          final long lockTime=System.currentTimeMillis();
          synchronized(mapArray)
          {
            bufferRect   = segment();
            fullRect     = segmentArray[FULL_RECT];
//            DjVuObject.checkLockTime(lockTime,10000);
          }

          if(fullRect.equals(lastFullRect))
          {
            // Wait at least half a second before updating 
            // a page which is still decoding.
            try
            {
              page.waitForCodec(page.doneLock, 500L);
            }
            catch(final Throwable ignored) {}
          }
        }
      }
    }
    while(repeat);

    flags[PROGRESSIVE_FLAG] = false;
  }

  /**
   * This is the run method for the scroll thread.
   */
  private void scrollRun()
  {
    final Thread current=Thread.currentThread();
//    if(animated) DjVuOptions.err.println("bcr + setScrollRun "+current);
    final GRect hor = new GRect();
    final GRect ver = new GRect();

    for(int i = 0;;)
    {
      GMap  map;
      GRect bufferRect;
      GRect oldRect;
      {
//        final long lockTime=System.currentTimeMillis();
        synchronized(mapArray)
        {
          map          = mapArray[0];
          bufferRect   = segment();
          oldRect      = segmentArray[BUFFER_RECT];

          if(oldRect == null)
          {
            oldRect=bufferRect;
          }
          if(oldRect.contains(bufferRect))
          {
            map = null;

            if(threadArray[SCROLL_THREAD] != current)
            {
              break;
            }

            if(i++ > 1)
            {
              threadArray[SCROLL_THREAD] = null;

              break;
            }

            try
            {
              mapArray.wait(200L);
            }
            catch(final Throwable ignored) {}

//          DjVuOptions.err.println("bcr mapArray.wait() return in "+current);
            continue;
          }
//          DjVuObject.checkLockTime(lockTime,10000);
        }
      }

      i = 0;

      try
      {
        ver.intersect(oldRect,bufferRect);
        if(ver.xmin == bufferRect.xmin)
        {
          ver.xmin   = oldRect.xmax;
          ver.xmax   = bufferRect.xmax;
        }
        else
        {
          ver.xmin   = bufferRect.xmin;
          ver.xmax   = oldRect.xmin;
        }

        hor.xmin   = bufferRect.xmin;
        hor.xmax   = bufferRect.xmax;

        if(ver.ymin == bufferRect.ymin)
        {
          hor.ymin   = oldRect.ymax;
          hor.ymax   = bufferRect.ymax;
        }
        else
        {
          hor.ymin   = bufferRect.ymin;
          hor.ymax   = oldRect.ymin;
        }

        // It may be less time consuming to fill in the extra pixels.  While this
        // removes the overhead of decoding some of the pixels, it adds the overhead
        // of copying all of the pixels.  It also takes longer to decode two small
        // rectangles than one big one.
        // To decide which is optimal, we assume copying all the pixels from a completely
        // new rectangle would take 10% longer.  Next we assume the time taken to decode
        // a segment varies with the sqrt of the area, with 10% of the time fixed.  
        // So we have:
        //    time to insert = C*sqrt(hor.area())+C*sqrt(ver.area())+0.1*C*sqrt(total.area)
        //    time to decode the whole thing = C*sqrt(total.area)
        // Of course, this is just a wild guess...
        final double areaH    = Math.sqrt(hor.area());
        final double areaV    = Math.sqrt(ver.area());
        final double areaT    = Math.sqrt(bufferRect.area());
        final double overhead = (hor.isEmpty() || ver.isEmpty())
          ? 0.9
          : 0.8;
        final GMap savedMap = (GMap)DjVuObject.getFromReference(reuseArray[0]);
        reuseArray[0] = null;

        if((map != null)&&((areaH + areaV) < (overhead * areaT)))
        {
//          DjVuOptions.out.println("bcr fill pixels");
          map =
            map.translate(
              bufferRect.xmin - oldRect.xmin,
              bufferRect.ymin - oldRect.ymin,
              savedMap);

          if(!hor.isEmpty())
          {
            final GMap m = page.getMap(hor, subsample, null);
            map.fill(
              m,
              hor.xmin - bufferRect.xmin,
              hor.ymin - bufferRect.ymin);
          }

          if(!ver.isEmpty())
          {
            final GMap m = page.getMap(ver, subsample, null);
            map.fill(
              m,
              ver.xmin - bufferRect.xmin,
              ver.ymin - bufferRect.ymin);
          }
        }
        else
        {
//          DjVuOptions.out.println("bcr copy pixels");
          map = page.getMap(bufferRect, subsample, savedMap);
        }
        if(map != null)
        {
//          final long lockTime=System.currentTimeMillis();
          synchronized(mapArray)
          {
            reuseArray[0] = DjVuObject.createSoftReference(mapArray[1],mapArray[1]);
            mapArray[0] = mapArray[1] = map;
            segmentArray[BUFFER_RECT]   = bufferRect;
//            DjVuObject.checkLockTime(lockTime,10000);
          }
        }

        newPixels();
      }
      catch(final Throwable exp)
      {
        exp.printStackTrace(DjVuOptions.err);
      }
      // System.gc();
    }

//    if(animated) DjVuOptions.err.println("bcr - setScrollRun "+current);
  }

  /**
   * Compute the segmented rectangles from the current bounds.
   * 
   * @return The newly created GRect which includes buffered pixels.
   */
  private GRect segment()
  {
//    if(animated) DjVuOptions.err.println("bcr segment");
    GRect retval = null;

//    final long lockTime=System.currentTimeMillis();
    synchronized(mapArray)
    {
      final int xmin = bounds.x = position.x;
      bounds.y = position.y;

      final int ymin = pageSize.height - bounds.y - bounds.height;

      if(subsample != 0)
      {
        final GRect p = segmentArray[PAGE_RECT];
        final int x0  = (int)Math.floor(sx*(double)xmin);
        final int y0  = (int)Math.floor(sy*(double)ymin);
        final GRect f = new GRect(x0, y0, fullWidth, fullHeight);

        segmentArray[FULL_RECT] = f;

        final GRect i = new GRect();
        i.intersect(p, f);
        segmentArray[INTEREST_RECT] = i;

        if(animated)
        {
          final int x0B  = Math.max(0,Math.min(p.xmax-bufferWidth,Math.min(
                  Math.max(i.xmax-bufferWidth,x0-deltaX),
                  i.xmin)));
          final int y0B  = Math.max(0,Math.min(p.ymax-bufferHeight,Math.min(
                  Math.max(i.ymax-bufferHeight,y0-deltaY),
                  i.ymin)));
          retval = new GRect(x0B, y0B, bufferWidth, bufferHeight);
        }
        else
        {
          retval = i;
        }
      }
//      DjVuObject.checkLockTime(lockTime,10000);
    }

    return retval;
  }
}
