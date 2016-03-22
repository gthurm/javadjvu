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
package com.lizardtech.djvubean.anno;

import com.lizardtech.djvubean.*;
import com.lizardtech.djvu.*;
import com.lizardtech.djvu.anno.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 *
 * @author docbill
 */
public class AnnoManager 
  extends MouseAdapter
  implements MouseMotionListener
{
  //~ Static fields/initializers ---------------------------------------------

  //~ Instance fields --------------------------------------------------------

  // position used for text searches

  /** DjVuBean to use. */
  protected final DjVuBean djvuBean;

  private Vector [] map_area = null;
  
  private DjVuImage image = null;
  
  private Rect mapRect = null;
  private int mapRectIndex=-1;
  
  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new AnnoManager object.
   * 
   * @param bean DjVuBean to use.
   */
  public AnnoManager(final DjVuBean bean)
  {
    djvuBean = bean;
    bean.properties.put(
      "addOn.anno",
      AnnoManager.class.getName());
    djvuBean.addMouseListener(this);
    djvuBean.addMouseMotionListener(this);
  }
 
  //~ Methods ----------------------------------------------------------------
 
  /**
   * Set the MapArea which contains the specified coordinates.
   *
   * @param x position along the X axis.
   * @param y position along the Y axis.
   */
  private void setMapRect(
    int x,
    int y)
  {
    Vector map_area=getMapArea(0);
    final DjVuImage image=this.image;
    if(image != null)
    {
      final Dimension borderNW=image.getBorderNW();
      final int jmax=image.getIndexMax();
      final double sx=1D/image.getHorizontalScale();
      final double sy=1D/image.getVerticalScale();
      for(int j=0;;)
      {
        final Rectangle bounds=image.getPageBounds(j);
        if((bounds != null)&&(map_area != null) && (map_area.size() > 0))
        {
          final int x0=borderNW.width+bounds.x;
          final int y0=borderNW.height+bounds.y;
          final int x1=x0+bounds.width;
          final int y1=y0+bounds.height;
          if((x >= x0)&&(x < x1)&&(y >= y0)&&(y < y1))
          {
            final DjVuInfo info=image.getDjVuInfo(j);
            x   = (int)(((double)(x - x0) * (double)info.dpi)*sx);
            y   = info.height
              - (int)(((double)(y - y0) * (double)info.dpi)*sy);

            for(int i = map_area.size(); i-- > 0;)
            {
              final Rect rect = (Rect)map_area.elementAt(i);
              if(rect.contains(x, y))
              {
                setMapRect(j, rect);
                return;
              }
            }
          }
        }
        if(++j >= jmax)
        {
          break;
        }
        map_area = getMapArea(j);           
      }
    }

    setMapRect(-1, null);
  }

  /**
   * Query the vector of GRectMap objects.
   *
   * @return vector of GRectMap objects.
   */
  public Vector getMapArea(final int index)
  {
    DjVuImage image=djvuBean.getImage();
    if(image != this.image)
    {
      this.image=image;
      map_area=null;
      mapRect=null;
    }
    Vector [] retval = map_area;
    if((retval == null)&&(image != null))
    {
      final int jmax=image.getIndexMax();
      retval=new Vector[jmax];
      for(int j=0;j<jmax;j++)
      {
        retval[j]=null;
        final Rectangle bounds=image.getPageBounds(j);
        if(bounds != null)
        {
          final DjVuInfo info=image.getDjVuInfo(j);
          final DjVuPage page=image.getDjVuPage(j);
          final DjVuAnno anno = (DjVuAnno)page.waitForCodec(page.annoLock, 0L);
          if(anno != null)
          {
            final Vector v=anno.getMapArea();
            for(int i = v.size(); i-- > 0;)
            {
              final Rect rect = (Rect)v.elementAt(i);
              rect.setPageSize(
                info.width,
                info.height,
                bounds.width,
                bounds.height);
            }
            retval[j]=v;
          }
        }
        map_area = retval;
      }
    }
    return retval[index];
  }

  // This routine switches the current color to the specified value.  Returns true if
  // successfull.
  private boolean switchColor(Graphics g,int color)
  {
    boolean retval=false;
    if(g!=null)
    {
        switch(color)
        {
          case Rect.NO_HILITE:
          {
            g.setColor(DjVuImage.BLACK);
            g.setPaintMode();
            break;
          }
          case Rect.XOR_HILITE:
          {
            g.setColor(DjVuImage.BLACK);
            if(DjVuImage.BROKEN_XOR)
            {
              g.setPaintMode();
            }
            else
            {
              g.setXORMode(DjVuImage.WHITE);
            }
            retval=true;
            break;
          }
          default:
          {
            g.setColor(new Color(color));
            g.setPaintMode();
            retval=true;
            break;
          }
        }
    }
    return retval;
  }
  
  // This routine drows a line with the specified thickness an an optional arrow head.
  //
  private void drawLine(Graphics g,int x0,int y0,int x1,int y1,int weight,boolean arrow)
  {
    double dx=x1-x0;
    double dy=y1-y0;
    final double ds=Math.sqrt(dx*dx+dy*dy);
    if(weight <= 1 || (ds < 4D))
    {
      g.drawLine(x0,y0,x1,y1);
      if(arrow)
      {
        if(ds > 4D)
        {
          dx*=4D/ds;
          dy*=4D/ds;
        }
        g.drawLine(x1, y1, x1-(int)(dx+dy), y1-(int)(dy-dx));
        g.drawLine(x1, y1, x1-(int)(dx-dy), y1-(int)(dy+dx));
      }
    }
    else
    {
      final double r=((double)weight)/(2D*ds);
      final double xx0=x0-r*dy;
      final double yy0=y0+r*dx;
      final double xx1=x1-r*dy;
      final double yy1=y1+r*dx;
      final double xx2=x1+r*dy;
      final double yy2=y1-r*dx;
      final double xx3=x0+r*dy;
      final double yy3=y0-r*dx;
      if(arrow)
      {
        final double xx01=xx1-4D*r*dx;
        final double yy01=yy1-4D*r*dy;
        final double xx02=xx01-4D*(dx+dy)/ds;
        final double yy02=yy01-4D*(dy-dx)/ds;
        double dxx=xx02-xx01;
        double dyy=yy02-yy01;
        double dss=Math.sqrt(dxx*dxx+dyy*dyy);
        double rr=((double)weight)/dss;
        final double xx03=xx02+rr*dyy;
        final double yy03=yy02-rr*dxx;
        final double xx07=xx2-4D*r*dx;
        final double yy07=yy2-4D*r*dy;
        final double xx06=xx07-4D*(dx-dy)/ds;
        final double yy06=yy07-4D*(dy+dx)/ds;
        dxx=xx06-xx07;
        dyy=yy06-yy07;
        dss=Math.sqrt(dxx*dxx+dyy*dyy);
        rr=((double)weight)/dss;
        final double xx05=xx06-rr*dyy;
        final double yy05=yy06+rr*dxx;
        final int xx[]={(int)xx0,(int)xx01,(int)xx02,(int)xx03,x1,(int)xx05,(int)xx06,(int)xx07,(int)xx3};
        final int yy[]={(int)yy0,(int)yy01,(int)yy02,(int)yy03,y1,(int)yy05,(int)yy06,(int)yy07,(int)yy3};
        g.fillPolygon(xx,yy,xx.length);
      }
      else
      {
        final int xx[]={(int)xx0,(int)xx1,(int)xx2,(int)xx3};
        final int yy[]={(int)yy0,(int)yy1,(int)yy2,(int)yy3};
        g.fillPolygon(xx,yy,xx.length);
      }
    }
  }
  
  /**
   * Called to draw the Rects.
   *
   * @param g graphics area to draw with.
   */
  public void draw(Graphics g)
  {
    Vector map_area = getMapArea(0);
    final DjVuImage image=this.image;
    if(image != null)
    {
      final int jmax=image.getIndexMax();
      for(int j=0;;)
      {
        final Rectangle bounds=image.getPageBounds(j);
        if((map_area != null)&&(bounds!= null))
        {
          final Graphics xg = g.create();
          final Dimension borderNW=image.getBorderNW();
          xg.translate(borderNW.width+bounds.x, borderNW.height+bounds.y);
          xg.setColor(DjVuImage.BLUE);

          for(int i = map_area.size(); i-- > 0;)
          {
            final Rect rect = (Rect)map_area.elementAt(i);

            final int[] xx = rect.getXCoordinates();
            final int[] yy = rect.getYCoordinates();
            int xmin=0,ymin=0,xmax=0,ymax=0;
            if(xx.length == 2)
            {
              xmin = (xx[0] < xx[1])?xx[0]:xx[1];
              xmax = (xx[0] > xx[1])?xx[0]:xx[1];
              ymin = (yy[0] < yy[1])?yy[0]:yy[1];
              ymax = (yy[0] > yy[1])?yy[0]:yy[1];
//              drawRect(xg,rect.getHiliteColor(),rect.getOpacity(),xmin, ymin, xmax, ymax);
              image.fillRect(xg,rect.getHiliteColor(),rect.getOpacity(),xmin, ymin, xmax, ymax);
            }
      
            if(rect.isVisible())
            {
              if(xx.length == 2)
              {
//              drawRect(xg, rect.getBgColor(),100,xmin,ymin,xmax,ymax);
                image.fillRect(xg,rect.getBgColor(),100,xmin, ymin, xmax, ymax);
              }
              final int border_type = rect.getBorderType();
              if(border_type != Rect.NO_BORDER)
              {
                switch(border_type)
                {
                  case Rect.SOLID_BORDER :
                  {
                    switchColor(xg,rect.getBorderColor());
                    break;
                  }
                  case Rect.XOR_BORDER :default :
                  {
                    switchColor(xg,Rect.XOR_HILITE);
                    break;
                  }
                }

                if(rect.getMapType() == Rect.MAP_POLY)
                {
                  if(((Poly)rect).isOpen())
                  {
                    xg.drawPolyline(xx, yy, xx.length);                                
                  }
                  else
                  {
                    xg.drawPolygon(xx, yy, xx.length);                
                  }
                }
                else if(rect.getMapType() == Rect.MAP_OVAL)
                {
                  xg.drawOval(xmin, ymin, xmax - xmin, ymax - ymin);
                }
                else
                {
                  xg.drawRect(xmin, ymin, xmax - xmin, ymax - ymin);
                }
              }
              if((xx.length == 2)&& rect.isPushpin())
              {
                xg.setColor(DjVuImage.BLACK);
                xg.setPaintMode();
                drawLine(xg,xmin+1, ymin+1, xmin+8, ymin+8,3,false);
                drawLine(xg,xmin, ymin+1, xmin+6, ymin+1,3,false);
                drawLine(xg,xmin+1, ymin+1, xmin+1, ymin+6,3,false);
              }
              if(rect.getMapType() == Rect.MAP_TEXT)
              {
                String text=rect.getComment();
                if((text != null)&&switchColor(xg,rect.getTextColor()))
                {
                  final java.awt.FontMetrics fm=xg.getFontMetrics();
                  int y0=ymin+fm.getHeight()+(rect.isPushpin()?10:0);
                  final char [] ch=text.toCharArray();
                  for(int off=0;(off < ch.length)&&(y0<ymax);)
                  {
                    int x0=xmin+4;
                    int x1=xmin+fm.charWidth(ch[off]);
                    int end=off+1;
                    int lastSpace=off;
                    for(;end < ch.length;end++)
                    {
                      char c=ch[end];
                      x1+=fm.charWidth(c);
                      if(Character.isWhitespace(c))
                      {
                        lastSpace=end;
                      }
                      if(x1 > xmax-4)
                      {
                        break;
                      }
                    }
                    if((end < ch.length)&&(lastSpace > off))
                    {
                      end=lastSpace+1;
                    }
                    xg.drawChars(ch, off, end-off, x0, y0);
                    y0+=fm.getHeight();
                    off=end;
                  }
                }
              }
            }
            else if(xx.length == 2 && rect.isPushpin())
            {
              xg.setColor(DjVuImage.BLACK);
              xg.setPaintMode();
              drawLine(xg,xmin+8, ymin+8, xmin+1, ymin+1,3,false);
              drawLine(xg,xmin+9, ymin+8, xmin+3, ymin+8,3,false);
              drawLine(xg,xmin+8, ymin+9, xmin+8, ymin+3,3,false);
            }
            if((rect.getMapType() == Rect.MAP_LINE)&&switchColor(xg,rect.getLineColor()))
            {
              drawLine(xg,xx[0], yy[0], xx[1], yy[1],rect.getLineWidth(),rect.isArrow());
            }
          }
          xg.dispose();             
        }
        if(++j >= jmax)
        {
          break;
        }
        map_area=getMapArea(j);
      }
    }
  }
  
  /**
   * Obtain the current Rect, normally set by the mouse listener as the
   * hyperlink the mouse is currently over.
   *
   * @return the current Rect.
   */
  public Rect getMapRect()
  {
    return mapRect;
  }

  /**
   * Set the current GMapRect, normally set by the mouse listener as the
   * hyperlink the mouse is currently over.
   *
   * @param mapRect the current Rect.
   */
  public void setMapRect(final int index,final Rect mapRect)
  {
    final Vector map_area = getMapArea(0);
    final DjVuImage image=this.image;
    if((image == null)||(map_area == null))
    {
      this.mapRect=null;
      return;
    }
    if(mapRect != this.mapRect)
    {
//      final long lockTime=System.currentTimeMillis();
      synchronized(this)
      {
        if(mapRect != this.mapRect)
        {
          Rectangle repaintBounds = null;

          if((this.mapRect != null) && (this.mapRect.isVisible())  && (!this.mapRect.isPushpin()))
          {
            this.mapRect.setVisible(false);
            repaintBounds = new Rectangle();
            image.transformRectangle(
              mapRectIndex,
              this.mapRect.getBounds(),
              repaintBounds);
          }

          this.mapRect = mapRect;
          mapRectIndex=index;
          
          if((mapRect != null) && (!mapRect.isPushpin())&&(!mapRect.isVisible()))
          {
            mapRect.setVisible(true);

            final Rectangle rect = new Rectangle();
            image.transformRectangle(
              index,
              mapRect.getBounds(),
              rect);

            if(repaintBounds != null)
            {
              repaintBounds.union(rect);
            }
            else
            {
              repaintBounds = rect;
            }
          }

          if(repaintBounds != null)
          {
            repaintBounds.setBounds(
              repaintBounds.x - 2,
              repaintBounds.y - 2,
              repaintBounds.width + 4,
              repaintBounds.height + 4);
            djvuBean.repaintImageCoordinates(repaintBounds);
//            djvuBean.repaint();
          }
        }
//        DjVuObject.checkLockTime(lockTime,10000);
      }
    }
  }

  /**
   * Submit the Rect if right clicked.
   *
   * @param event describing the mouse action.
   */
  public void mouseClicked(final MouseEvent event)
  {
    try
    {
      if((event.getModifiers() & ~InputEvent.BUTTON1_MASK) == 0)
      {
        final Rect rect = getMapRect();
        if(rect != null)
        {
          if(rect.isPushpin())
          {
            rect.setVisible(!rect.isVisible());
            final Rectangle repaintBounds = new Rectangle();
            image.transformRectangle(
              mapRectIndex,
              rect.getBounds(),
              repaintBounds);              
            repaintBounds.setBounds(
              repaintBounds.x - 2,
              repaintBounds.y - 2,
              repaintBounds.width + 4,
              repaintBounds.height + 4);
            djvuBean.repaintImageCoordinates(repaintBounds);
          }
          else
          {
            final String url = rect.getURL();

            if((url != null) && (url.length() > 0))
            {
              final int pageno = djvuBean.getDocument().getPageno(url);
              if(pageno >= 0)
              {
                djvuBean.setPage(pageno + 1);
              }
              else
              {
                djvuBean.setSubmit(getMapRect());
              }
            }
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
   * Event indicating the mouse was dragged.  Performs no operation.
   *
   * @param event describing the mouse action.
   */
  public void mouseDragged(MouseEvent event) {}

  /**
   * Event indicating the mouse was moved.  Used to display Rects
   * that require the mouse to be over the Rect to display.
   *
   * @param event describing the mouse action.
   */
  public void mouseMoved(final MouseEvent event)
  {
    try
    {
      if(!djvuBean.isDecoding())
      {
        setMapRect(
            event.getX(),
            event.getY());
      }
    }
    catch(final Throwable ignored) {}
  }
}
