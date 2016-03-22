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

import com.lizardtech.djvu.DjVuOptions;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;


/**
 * This class is intended as an AWT replacement of JButton and JToggleButton.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.4 $
 */
public class ToggleButton
  extends Container
  implements ItemSelectable
// , Runnable
{
  //~ Static fields/initializers ---------------------------------------------

//  public final String tip;

  /** query java version. */
  public static final boolean OLD_JAVA =
    System.getProperty("java.version").startsWith("1.1.");

  /** Constant to incidate no border should be painted. */
  public static final int NOBORDER = 0;

  /** Constant to incidate a raised border should be painted. */
  public static final int RAISED_BORDER = 1;

  /** Constant to incidate a simple border should be painted. */
  public static final int SIMPLE_BORDER = 2;

  //~ Instance fields --------------------------------------------------------

  /** The icon which will be showed when disabled. */
  private Image gray = null;

  /** The icon shown when enabled. */
  private Image image = null;

  // support for ActionListeners
  private ListenerSupport actionPerformed =
    new ListenerSupport(ActionListener.class);

//  public Button button;
  // support for ItemListeners
  private ListenerSupport itemState = new ListenerSupport(ItemListener.class);

  /** Object used for locking when waiting for a repaint. */
  private Object repaintLock   = new Object();
  private String actionCommand = null;
  private String label         = null;

  /** Indicates if the button is selectable. */
  private boolean isSelectable = false;

  /** Indicates if the button is selected. */
  private boolean isSelected  = false;
  private double  scaleFactor = 1.0;

  /** Border type to draw. */
  private int borderType  = RAISED_BORDER;
  private int imageHeight = 0;
  private int imageWidth  = 0;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new ToggleButton object equivalent to JButton.
   *
   * @param tip Tool tip to display.  (For future use.)
   * @param text Text to display.
   * @param imageArg Image to display.
   * @param defaultSize default button size.
   */
  public ToggleButton(
    final String    tip,
    final String    text,
    final Object    imageArg,
    final Dimension defaultSize)
  {
    this.image = getImage(imageArg);
    setLabel(text);

    if(image != null)
    {
      setImageWidth(image.getWidth(this));
      setImageHeight(image.getHeight(this));

      if(defaultSize != null)
      {
        if(getImageWidth() < 0)
        {
          setImageWidth(defaultSize.width);
        }

        if(getImageHeight() < 0)
        {
          setImageHeight(defaultSize.height);
        }
      }

      prepareImage(image, this);
    }

    setBackground(new Color(192, 192, 192));
    setForeground(new Color(0, 0, 0));
    enableEvents(AWTEvent.ACTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
  }

  /**
   * Creates a new ToggleButton object equivalent to JToggleButton.
   *
   * @param tip Tool tip to display.  (For future use.)
   * @param text Text to display.
   * @param imageArg Image to display.
   * @param defaultSize default button size.
   * @param isSelected Initial selection value.
   */
  public ToggleButton(
    final String    tip,
    final String    text,
    final Object    imageArg,
    final Dimension defaultSize,
    final boolean   isSelected)
  {
    this(tip, text, imageArg, defaultSize);
    this.isSelected   = isSelected;
    isSelectable      = true;
    enableEvents(AWTEvent.ACTION_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);

    // enableEvents(AWTEvent.ITEM_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Set the action command.
   *
   * @param actionCommand string to set.
   */
  public void setActionCommand(final String actionCommand)
  {
    this.actionCommand = actionCommand;
  }

  /**
   * Query the action command.
   *
   * @return actionCommand string.
   */
  public String getActionCommand()
  {
    return actionCommand;
  }

  /**
   * Query currently registered ActionListener objects.
   *
   * @return array of ActionListener objects.
   */
  public ActionListener[] getActionListeners()
  {
    return (ActionListener[])actionPerformed.getListeners();
  }

  /**
   * Sets the border type.
   *
   * @param borderType how to draw the border.
   */
  public void setBorderType(int borderType)
  {
    if(borderType != this.borderType)
    {
      this.borderType = borderType;
      repaint();
    }
  }

  /**
   * Query the border type.
   *
   * @return the border type.
   */
  public int getBorderType()
  {
    return borderType;
  }

  /**
   * Get the border along the X axis.
   *
   * @return border along the X axis.
   */
  public int getBorderX()
  {
    return 2 + (int)((3 * getScaleFactor()) + 0.5);
  }

  /**
   * Get the border along the Y axis.
   *
   * @return border along the Y axis.
   */
  public int getBorderY()
  {
    return 2 + (int)((3 * getScaleFactor()) + 0.5);
  }

  /**
   * Set the enabled property of this Component.
   *
   * @param enabled true if enabled.
   */
  public void setEnabled(final boolean enabled)
  {
    super.setEnabled(enabled);
    repaint(20L);
  }

  /**
   * Query currently registered ItemListener objects.
   *
   * @return array of ItemListener objects.
   */
  public ItemListener[] getItemListeners()
  {
    return (ItemListener[])itemState.getListeners();
  }

  /**
   * Set the text label for this button.
   *
   * @param text label for this button.
   */
  public void setLabel(String text)
  {
    label = text;
  }

  /**
   * Query the text label for this button.
   *
   * @return text label for this button.
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * Query the maximum image size allowed.
   *
   * @return the maximum image size allowed.
   */
  public Dimension getMaximumSize()
  {
    return getPreferredSize();
  }

  /**
   * Query the minimum image size allowed.
   *
   * @return the minimum image size allowed.
   */
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }

  /**
   * Query the preferred image size allowed.
   *
   * @return the preferred image size allowed.
   */
  public Dimension getPreferredSize()
  {
    Dimension retval = new Dimension(getBorderX() * 2, 0);

    if(image != null)
    {
      if(getImageWidth() > 0)
      {
        retval.width += getImageWidthScaled();
      }

      if(getImageHeight() > 0)
      {
        retval.height = getImageHeightScaled();
      }
    }

    final String label = getLabel();

    if(label != null)
    {
      FontMetrics metrics = getFontMetrics(getFont());
      retval.width += (metrics.stringWidth(label) + getBorderX());

      final int height = metrics.getHeight();

      if(height > retval.height)
      {
        retval.height = height;
      }
    }

    retval.height += (getBorderY() * 2);

    return retval;
  }

  /**
   * Set the scale resizing factor used for the image.
   *
   * @param value scale factor used for resizing the image.
   */
  public void setScaleFactor(double value)
  {
    scaleFactor = value;
  }

  /**
   * Query the scale resizing factor used for the image.
   *
   * @return scale factor used for resizing the image.
   */
  public double getScaleFactor()
  {
    return scaleFactor;
  }

  /**
   * Set the selected value.
   *
   * @param value true if selected.
   */
  public void setSelected(final boolean value)
  {
//    DjVuOptions.out.println(tip+".setSelected("+value+")");
    if(value != isSelected())
    {
      if(isSelectable)
      {
        isSelected = value;
        processEvent(
          new ItemEvent(
            this,
            ItemEvent.ITEM_STATE_CHANGED,
            this,
            isSelected
            ? ItemEvent.SELECTED
            : ItemEvent.DESELECTED));
      }
      else
      {
        processEvent(
          new ActionEvent(
            this,
            ActionEvent.ACTION_PERFORMED,
            getActionCommand()));
      }
    }
  }

  /**
   * Query if selected.
   *
   * @return true if selected.
   */
  public boolean isSelected()
  {
    return isSelected;
  }

  /**
   * Return an array of this item if selected, otherwise an empty array is
   * returned.
   *
   * @return array of selected objects.
   */
  public Object[] getSelectedObjects()
  {
    if(!isSelected)
    {
      return new Object[0];
    }
    else
    {
      Object[] retval = new Object[1];
      retval[0] = this;

      return retval;
    }
  }

  /**
   * Add a new ActionListener.
   *
   * @param listener to add.
   */
  public void addActionListener(final ActionListener listener)
  {
    actionPerformed.addListener(listener);
  }

  /**
   * Add a new ItemListener.
   *
   * @param listener to add.
   */
  public void addItemListener(final ItemListener listener)
  {
    itemState.addListener(listener);
  }

  /**
   * Called to draw this this button.
   *
   * @param g Graphics device to draw to.
   */
  public void drawIcon(final Graphics g)
  {
    final int width  = getSize().width;
    final int height = getSize().height;

    if(
      (width < (2 * (1 + getBorderX())))
      || (height < (2 * (1 + getBorderY()))))
    {
      return;
    }

    Color c = g.getColor();

    switch(getBorderType())
    {
      case RAISED_BORDER :
      {
        g.setColor(getBackground());
        g.fill3DRect(0, 0, width - 1, height - 1, !isSelected);

        break;
      }
      case SIMPLE_BORDER :
      {
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);
        g.setColor(getForeground());
        g.drawRect(0, 0, width - 1, height - 1);
        g.setColor(getBackground());

        break;
      }
      default :
      {
        g.setColor(getBackground());
        g.fillRect(0, 0, width, height);

        break;
      }
    }

    int xOffset = getBorderX();

    if(image != null)
    {
      if(isEnabled())
      {
        drawImage(g, image, xOffset);
      }
      else
      {
        if(gray == null)
        {
          gray =
            createImage(
              new FilteredImageSource(
                image.getSource(),
                new GrayFilter()));
        }

        drawImage(g, gray, xOffset);
      }

      xOffset += (getImageWidthScaled() + getBorderX());
    }

    final String label = getLabel();

    if(label != null)
    {
      Color fgColor = getForeground();

      if(!isEnabled())
      {
//        final int gray =
//          ((fgColor.getRed() * 77) + (fgColor.getGreen() * 59)
//          + (fgColor.getBlue() * 11)) >> 8;
//        fgColor = new Color(gray, gray, gray, fgColor.getAlpha() / 2);
        fgColor = fgColor.darker();
      }

      g.setColor(fgColor);

      final Font f = g.getFont();
      g.setFont(getFont());
      g.drawBytes(
        label.getBytes(),
        0,
        label.getBytes().length,
        xOffset,
        height - 1 - getBorderY());
      g.setFont(f);
    }

    g.setColor(c);
  }

  /**
   * Called to draw the image at the specified offset.
   *
   * @param g graphics object to draw.
   * @param image to draw.
   * @param xOffset offset along the X axis.
   */
  public void drawImage(
    final Graphics g,
    final Image    image,
    final int      xOffset)
  {
    if(getScaleFactor() == 1.0)
    {
      g.drawImage(
        image,
        xOffset,
        getBorderY(),
        getImageWidth(),
        getImageHeight(),
        this);
    }
    else
    {
      g.drawImage(
        image,
        xOffset,
        getBorderY(),
        xOffset + getImageWidthScaled(),
        getBorderY() + getImageHeightScaled(),
        0,
        0,
        getImageWidth(),
        getImageHeight(),
        this);
    }
  }

  /**
   * Callback called to indicate the icon image has been updated.
   *
   * @param img updated image.
   * @param infoflags flags indicating update status.
   * @param x coordinate of the image.
   * @param y coordinate of the image.
   * @param width of the image.
   * @param height height of the image.
   *
   * @return true if further updates are neccissary.
   */
  public boolean imageUpdate(
    Image img,
    int   infoflags,
    int   x,
    int   y,
    int   width,
    int   height)
  {
    if(
      (image == img)
      && ((getImageWidth() != width) || (getImageHeight() != height)))
    {
      if((width != -1) && (height != -1))
      {
        setImageWidth(width);
        setImageHeight(height);
        invalidate();

        if(isShowing())
        {
          final Component parent = getParent();

          if(parent != null)
          {
            parent.validate();
          }
        }

        repaint(20L);
      }
    }

//    return ((ImageObserver.ALLBITS & infoflags) != 0);
    return super.imageUpdate(img, infoflags, x, y, width, height);

//    return ((infoflags
//    & (ImageObserver.ABORT | ImageObserver.ERROR | ImageObserver.ALLBITS)) == 0);
  }

  /**
   * Called to draw the button.
   *
   * @param g Graphics device to draw to.
   */
  public void paint(final Graphics g)
  {
    update(g);
  }

  /**
   * Called to paint the component.
   *
   * @param g graphics object to paint.
   */
  public void paintAll(final Graphics g)
  {
    update(g);
  }

  /**
   * Remove an ActionListener.
   *
   * @param listener to remove.
   */
  public void removeActionListener(final ActionListener listener)
  {
    actionPerformed.removeListener(listener);
  }

  /**
   * Remove an ItemListener.
   *
   * @param listener to remove.
   */
  public void removeItemListener(final ItemListener listener)
  {
    itemState.removeListener(listener);
  }

  /**
   * Called to invoke a repaint, and wait upto the specified timeout value in
   * milliseconds for the update() method to be invoked.
   *
   * @param timeout maximum time to wait.
   */
  public void repaintWait(long timeout)
  {
    synchronized(repaintLock)
    {
      repaint();

      try
      {
        repaintLock.wait(timeout);
      }
      catch(Throwable ignored) {}
    }
  }

  /**
   * Called to draw the button.
   *
   * @param g Graphics device to draw to.
   */
  public void update(final Graphics g)
  {
    if(!isValid())
    {
      final Component parent = getParent();

      if(parent != null)
      {
        parent.validate();
        repaint();
      }
    }
    else
    {
      drawIcon(g);
    }

    synchronized(repaintLock)
    {
      try
      {
        repaintLock.notifyAll();
      }
      catch(Throwable ignored) {}
    }
  }

  /**
   * Called to load an image.
   *
   * @param imageArg argument indicating the image, URL, or string path of
   *        the image to load.
   *
   * @return the loaded image.
   */
  protected Image getImage(Object imageArg)
  {
    if(imageArg == null)
    {
      return null;
    }

    if(imageArg instanceof Image)
    {
      return (Image)imageArg;
    }

    Image image = null;

    try
    {
      image =
        createImage(
          new FilteredImageSource(
            ToolbarImages.createImage(
              this,
              imageArg.toString()).getSource(),
              new TransparentFilter()));
    }
    catch(final Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
    }

    return image;
  }

  /**
   * Set the height of the image.
   *
   * @param height of the image.
   */
  protected void setImageHeight(int height)
  {
    this.imageHeight = height;
  }

  /**
   * Query the height of the image.
   *
   * @return height of the image.
   */
  protected int getImageHeight()
  {
    return imageHeight;
  }

  /**
   * Query the scaled height of the image.
   *
   * @return scaled height of the image.
   */
  protected int getImageHeightScaled()
  {
    return (int)((getScaleFactor() * (double)getImageHeight()) + 0.5);
  }

  /**
   * Set the width of the image.
   *
   * @param width of the image.
   */
  protected void setImageWidth(int width)
  {
    this.imageWidth = width;
  }

  /**
   * Query the width of the image.
   *
   * @return width of the image.
   */
  protected int getImageWidth()
  {
    return imageWidth;
  }

  /**
   * Query the scaled width of the image.
   *
   * @return scaled width of the image.
   */
  protected int getImageWidthScaled()
  {
    return (int)((getScaleFactor() * (double)getImageWidth()) + 0.5);
  }

  /**
   * Called to process an ActionEvent.
   *
   * @param event event to process.
   */
  protected void processActionEvent(final ActionEvent event)
  {
    if(isEnabled())
    {
      if(isSelectable)
      {
        setSelected(!isSelected);
      }
      else
      {
        isSelected = true;

//        final Class [] params={ActionEvent.class};
//        final Object [] args={ event };
//        final Thread  thread = ItemStateSupport.createThread(this,"fireAfterRepaint",params,args);
        final ListenerSupport action = actionPerformed;
        final Thread          thread =
          new Thread()
          {
            public void run()
            {
              repaintWait(50L);
              action.fireActionEvent(event);
              isSelected = false;
              repaint();
            }
          };

        thread.start();
      }
    }

    repaint(20L);
  }

//  /** 
//   * Fire an ItemEvent after repaintWait(50L).
//   */  
//  public void fireAfterRepaint(final ActionEvent event)
//  {
//    repaintWait(50L);
//    actionPerformed.fireActionEvent(event);
//    isSelected = false;
//    repaint();
//  }

  /**
   * Called to process an event.
   *
   * @param event to process.
   */
  protected void processEvent(final AWTEvent event)
  {
    if(event instanceof ItemEvent)
    {
      processItemEvent((ItemEvent)event);
    }
    else if(event instanceof ActionEvent)
    {
      processActionEvent((ActionEvent)event);
    }
    else
    {
      super.processEvent(event);
    }
  }

  /**
   * Called to process an item event.
   *
   * @param event event to process.
   */
  protected void processItemEvent(final ItemEvent event)
  {
    if(isEnabled())
    {
      final boolean value = (event.getStateChange() == ItemEvent.SELECTED);

      if(isSelectable || value)
      {
        isSelected = value;

//        final Class [] params={ ItemEvent.class };
//        final Object [] args={ event };
//        final Thread  thread = ListenerSupport.createThread(this,"fireAfterRepaint",params,args);
        final ListenerSupport state  = itemState;
        final Thread          thread =
          new Thread()
          {
            public void run()
            {
              repaintWait(50L);
              state.fireItemEvent(event);
            }
          };

        thread.start();
      }
    }
  }

//  /** 
//   * Fire an ItemEvent after repaintWait(50L).
//   */  
//  public void fireAfterRepaint(final ItemEvent event)
//  {
//    repaintWait(50L);
//    itemState.fireItemEvent(event);
//  }

  /**
   * Called when the mouse has updated.  If the mouse is clicked the selected
   * property will be updated.
   *
   * @param event describes the mouse update.
   */
  protected void processMouseEvent(final MouseEvent event)
  {
    //DjVuOptions.out.println("MouseEvent "+event.getID()+" "+MouseEvent.MOUSE_CLICKED);
    if(isEnabled() && (event.getID() == MouseEvent.MOUSE_CLICKED))
    {
      setSelected(!isSelected());
    }

    super.processMouseEvent(event);
  }

//  private Vector queueVector=new Vector();
//  private Thread queueThread=null;
//
//  /**
//   * Called to process an event.
//   *
//   * @param event to process.
//   */
//  protected void processEvent(final AWTEvent event)
//  {
//    if((event instanceof ItemEvent)||(event instanceof ActionEvent))
//    {
//      try
//      {
//        synchronized(queueVector)
//        {
//          queueVector.addElement(event);
//          if(queueThread == null)
//          {
//            queueThread=new Thread(this);
//            queueThread.start();
//          }
//          else
//          {
//            queueVector.notifyAll();
//          }
//        }
//        throw new Exception((event instanceof ItemEvent)?(((ToggleButton)((ItemEvent)event).getItem()).tip+","+((ItemEvent)event).getStateChange()):"action");
//      }
//      catch(final Throwable ignored) {ignored.printStackTrace(DjVuOptions.err);}
//    }
//    else
//    {
//      super.processEvent(event);
//    }
//  }
//
//  /**
//   * Called to process an events.
//   *
//   * @param event to process.
//   */
//  public void run()
//  {
//    for(int i=0;;)
//    {
//      try
//      {
//        while(true)
//        {
//          AWTEvent event=null;
//          synchronized(queueVector)
//          {
//            event=(AWTEvent)queueVector.elementAt(0);
//            queueVector.removeElementAt(0);
//          }
//          if(event != null)
//          {
//            i=0;
//            try
//            {
//              if(event instanceof ItemEvent)
//              {
//                processItemEvent((ItemEvent)event);
//              }
//              else if(event instanceof ActionEvent)
//              {
//                processActionEvent((ActionEvent)event);
//              }
//            }
//            catch(final Throwable ignored) {ignored.printStackTrace(DjVuOptions.err);}
//          }
//        }
//      }
//      catch(final Throwable ignored) {}
//      synchronized(queueVector)
//      {
//        if(queueVector.size() == 0)
//        {
//          try
//          {
//            queueVector.wait(100L);
//          }
//          catch(final Throwable ignored) {ignored.printStackTrace(DjVuOptions.err);}
//          if((queueVector.size() == 0)&&(++i >= 100))
//          {
//            queueThread=null;
//            break;
//          }
//        }
//      }
//      try
//      {
//        Thread.sleep(10L);
//      }
//      catch(final Throwable ignored) {ignored.printStackTrace(DjVuOptions.err);}
//    }
//  }
}
