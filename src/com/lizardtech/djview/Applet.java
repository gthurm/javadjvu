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
package com.lizardtech.djview;

import com.lizardtech.djvu.*;
import com.lizardtech.djvubean.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.*;
import java.util.Properties;
import java.lang.reflect.*;

/**
 * A class for displaying djvu documents.  Very simmular to the LizardTech
 * DjVu plugin.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.22 $
 */
public class Applet
  extends java.applet.Applet
  implements PropertyChangeListener, Runnable
{
  //~ Static fields/initializers ---------------------------------------------

  /** Magic scale value used to zoom to fit width. */
  public static final int FIT_WIDTH = -1;

  /** Magic scale value used to zoom to fit page. */
  public static final int FIT_PAGE = -2;

  /** Constant string indicating the image should be displayed. */
  public static final String IMAGE_STRING = "Image";

  /** Constant string indicating the hidden text should be displayed. */
  public static final String TEXT_STRING = "Text";

  /** The jar file which we should run, if main is invoked. */
  public final String jarName="djvuframe.jar";
  
  /** The main class to use, if main is invoked and the jar file is not available. */
  public final String mainClass="com.lizardtech.djview.frame.Frame";

  //~ Instance fields --------------------------------------------------------

  /** The cardlayout which displays either the image or the hidden text. */
  public CardLayout cardLayout = null;

  /** This container will contain a scrollable DjVu Image */
  protected Container scrollPane = null;

  /** The current document being displayed. */
  protected Document document = null;

  /** The current URL being displayed. */
  protected URL url = null;

  /** True until the first time the mouse is moved over the applet. */
  protected boolean first = true;

  /** True if a scrollpane should be used. This is intended for debug purposes. */
  protected boolean useScrollPane = false;

  /** The current zoom factor. */
  protected int     scale     = 100;
  private Component splitPane = null;

  // This container will be the center pane.
  private Container centerPane = null;

  /** The panel which displays the DjVu page. */
  private DjVuBean djvuBean  = null;
  private Object   parentRef = null;
  private boolean  validDjVu = false;
  private static Object console=null;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new Applet object.
   */
  public Applet() {}

  //~ Methods ----------------------------------------------------------------
  
  /**
   * This applet may also be invoked as a program using javaw.   In that event
   * we first try running a jar file.  If that fails we try the mainClass.
   *
   * @param args Should contain the target URL to view.
   */
  public static void main(final String[] args)
  {
    try
    {
      Class c = null;
      try
      {
        final String name="/"+Applet.class.getName().replace('.', '/')+".class";
        String r=Applet.class.getResource(name).toString();
        if(r.startsWith("jar:"))
        {
          r=r.substring(4, r.lastIndexOf('!'));
          r=r.substring(0, r.lastIndexOf("/")+1)+"djvuframe.jar";
          final URL u = new URL("jar", "", r + "!/");
       	  final URLConnection uc = u.openConnection();
	  final Object attr = uc.getClass().getMethod("getMainAttributes",null).invoke(uc,null);
          final Object o=Class.forName("java.util.jar.Attributes$Name").getField("MAIN_CLASS").get(null);
          final String className=(String)attr.getClass().getMethod("getValue", new Class [] { o.getClass() } ).invoke(attr,new Object [] {o});
          c=(new URLClassLoader(new URL[] { new URL(r) })).loadClass(className);
        }
      }
      catch(final Throwable ignored) {}
      if(c == null)
      {
        c=Class.forName("com.lizardtech.djview.frame.Frame");
      }
      c.getMethod("main",new Class [] { args.getClass() } ).invoke(null,new Object [] { args } );
    }
    catch(Throwable exp)
    {
      if(DjVuObject.hasReferences)
      DjVuOptions.err.println(
              DjVuObject.hasReferences
              ?"The package has been built without application support."  
              :"The version of Java is only supported when running as an applet.");  
      //exp.printStackTrace(DjVuOptions.err);
    }
  }

  /**
   * Query and/or create the cardlayout for displaying text or image.
   *
   * @return the cardlayout.
   *
   * @throws IOException if an error occures decoding the document
   */
  public CardLayout getCardLayout()
    throws IOException
  {
    CardLayout retval = cardLayout;

    if(retval == null)
    {
      getCenterPane();
      retval = cardLayout;
    }

    return retval;
  }

  /**
   * Query and/or create the center pane.
   *
   * @return the center pane.
   *
   * @throws IOException if an error occures decoding the document
   */
  public Container getCenterPane()
    throws IOException
  {
    Container retval = centerPane;

    if(retval == null)
    {
      synchronized(this)
      {
        retval = centerPane;

        if(retval == null)
        {
          retval = new Panel();
          retval.setBackground(new Color(128, 128, 128));

          final CardLayout cardLayout = new CardLayout();
          retval.setLayout(cardLayout);
          retval.add(
            IMAGE_STRING,
            getScrollPane());
          retval.add(
            TEXT_STRING,
            getDjVuBean().getTextArea());
          centerPane        = retval;
          this.cardLayout   = cardLayout;
        }
      }
    }

    return retval;
  }

  /**
   * Query and/or create the DjVuBean being displayed.
   *
   * @return the DjVuBean.
   *
   * @throws IOException if an error occures decoding the document
   */
  public DjVuBean getDjVuBean()
    throws IOException
  {
    DjVuBean retval = djvuBean;

    if(retval == null)
    {
      try
      {
        djvuBean =
          retval = useScrollPane
            ? (new DjVuBean())
            : (new DjVuViewport());
        final Properties properties = retval.getProperties();
        String[][]       pinfo = getParameterInfo();

        for(int i = 0; i < pinfo.length;)
        {
          final String name  = pinfo[i++][0];
          final String value = getParameter(name);

          if(value != null)
          {
            properties.put(
              name.toLowerCase(),
              value);
          }
        }

        final URL[] bases = {getDocumentBase(), getCodeBase()};
        String      src =
          properties.getProperty(
            "data",
            properties.getProperty("src"));
        Throwable lastExp = null;

        for(int i = 0; i < bases.length; i++)
        {
          try
          {
            if(src != null)
            {
              try
              {
                if(bases[i] != null)
                {
                  setURL(new URL(bases[i], src));
                }
                else if(src != null)
                {
                  setURL(new URL(src));
                }
              }
              catch(Throwable exp)
              {
                src = null;
                exp.printStackTrace(DjVuOptions.err);
              }
            }
            else if(i > 0)
            {
              break;
            }

            URL url = getURL();

            try
            {
              DjVuOptions.out.println("Trying " + url);
              retval.setURL(url);
            }
            catch(final IOException exp)
            {
              System.gc();

              URL nurl = new URL(url, "directory.djvu");

              if(nurl.equals(url))
              {
                setURL(new URL(url, "index.djvu"));
              }
              else if(url.equals(new URL(url, "index.djvu")))
              {
                setURL(nurl);
              }
              else
              {
                lastExp = exp;

                continue;
              }

              url = getURL();
              DjVuOptions.out.println("Trying " + url);
              retval.setURL(url);
            }

            lastExp = null;

            break;
          }
          catch(final java.lang.SecurityException exp)
          {
            lastExp = exp;
          }
        }

        if(lastExp != null)
        {
          throw lastExp;
        }
      }
      catch(final IOException exp)
      {
        throw exp;
      }
      catch(final RuntimeException exp)
      {
        throw exp;
      }
      catch(final Throwable exp)
      {
        final ByteArrayOutputStream s=new ByteArrayOutputStream();
        final PrintStream p=new PrintStream(s);
        exp.printStackTrace(p);
        throw new RuntimeException(new String(s.toByteArray()));
      }
    }

    return retval;
  }

  /**
   * Method to obtain an array of parameters accepted by this applet.
   *
   * @return array of parameters accepted by this applet.
   */
  public String[][] getParameterInfo()
  {
    final String[][] pinfo =
    {
      {"data", "url", "DjVu Document to load"},
      {"AboutURL", "url", "URL to load for about"},
      {"Cache", "boolean", "False if last and next page should not be cached"},
      {"Console", "boolean", "Used to enable the DjVu Java Console"},
      {"HelpURL", "url", "URL to load for help"},
      {"Keys", "boolean", "False if keyboard shortcuts"},
      {"LogoURL", "url", "URL to load when clicking on the logo"},
      {"MouseMode", "pan,zoom,text", "Initial mouse mode"},
      {"Menu", "boolean", "False if the menu should not be used"},
      {"NavPane", "outline,none", "Navigation mode"},
      {"Page", "number", "Initial page number"},
      {"PageLayout", "single,book,cover", "Page layout"},
      {"Prefetch", "boolean", "True if indirect pages should be prefetched"},
      {"SearchText", "string", "Text to initially search for"},
      {"Toolbar", "boolean", "False if the toolbar should not be used"},
      {
        "Zoom", "number,width,page",
        "Initial zoom value False if the toolbar should not be used"
      },
      {
          "ZoomFast", "boolean", 
          "True if fit page and fit width sizes should be rounded down for faster viewing."
      }
    };

    return pinfo;
  }

  /**
   * Query and/or create the component to use as scroll pane by the DjVuBean.
   *
   * @return the component to use as a scroll pane.
   *
   * @throws IOException if an error occures decoding the document
   */
  public Component getScrollPane()
    throws IOException
  {
    Container retval = scrollPane;

    if(retval == null)
    {
      final DjVuBean page = getDjVuBean();

      if(page instanceof DjVuViewport)
      {
        retval = new Panel();
        retval.setLayout(new BorderLayout());
        retval.add(
          "East",
          ((DjVuViewport)page).getScrollbar(Scrollbar.VERTICAL));
        retval.add(
          "South",
          ((DjVuViewport)page).getScrollbar(Scrollbar.HORIZONTAL));
        retval.add("Center", page);
      }
      else
      {
        retval = scrollPane = new ScrollPane();
        retval.setBackground(new Color(128, 128, 128));
        retval.add(page);
      }
    }

    return retval;
  }

  /**
   * Called to create a split pane for displaying outline navigation along
   * side of the DjVuBean.
   *
   * @param leftPane outline navigation.
   * @param centerPane DjVuBean.
   *
   * @return the component to display.
   */
  public Component getSplitPane(
    final Component leftPane,
    final Component centerPane)
  {
    if(leftPane == null)
    {
      return centerPane;
    }

    if(centerPane == null)
    {
      return leftPane;
    }

    Container retval = null;

    try
    {
      final Class                         jsplitClass =
        Class.forName("javax.swing.JSplitPane");
      final Class[]                       params =
      {Integer.TYPE, Component.class, Component.class};
      final Constructor jsplitConstructor =
        jsplitClass.getConstructor(params);
      final Field       horizontalField =
        jsplitClass.getField("HORIZONTAL_SPLIT");
      final Object                        horizontal =
        horizontalField.get(jsplitClass);
      final Object[]                      args =
      {horizontal, leftPane, centerPane};
      retval = (Container)jsplitConstructor.newInstance(args);
    }
    catch(final Throwable ignored)
    {
      retval = new Panel(new BorderLayout());
      retval.add(leftPane, BorderLayout.WEST);
      retval.add(centerPane, BorderLayout.CENTER);
    }

    return retval;
  }

  /**
   * Set the url to be rendered.  The page will not be updated until init()
   * is called.
   *
   * @param url to render.
   */
  public void setURL(final URL url)
  {
    if((url != this.url) && ((url == null) || !url.equals(this.url)))
    {
      synchronized(this)
      {
        this.url   = url;
        document   = null;
      }
    }
  }

  /**
   * Query the URL displayed by this applet.
   *
   * @return URL to display.
   */
  public URL getURL()
  {
    URL retval = url;

    if(url == null)
    {
      try
      {
        retval   = getDocumentBase();
        url =
          retval =
            (retval == null)
            ? (new URL(
              "http://www.lizardtech.com/download/files/win/djvuplugin/en_US/welcome.djvu"))
            : (new URL(retval, "index.djvu"));
      }
      catch(Throwable ignored) {}
    }

    return retval;
  }

  /**
   * Query if a DjVu document has been successfully initialized.
   *
   * @return True if init() was successfull.
   */
  public boolean isValidDjVu()
  {
    return validDjVu;
  }

  /**
   * Initialize the currently selected url, and render the first page of the
   * document.
   */
  public void init()
  {
    validDjVu    = false;

    try
    {
      final long startTime = System.currentTimeMillis();
      DjVuOptions.out.println(Applet.class.getName() + " loaded");
          
      final DjVuBean   djvuBean   = getDjVuBean();
      final Properties properties = djvuBean.getProperties();
      String[][]       pinfo      = getParameterInfo();

      for(int i = 0; i < pinfo.length;)
      {
        final String name  = pinfo[i++][0];
        final String value = getParameter(name);

        if(value != null)
        {
          properties.put(name, value);
        }
      }
      
      if((console == null)
        &&System.getProperty("java.version").startsWith("1.1.")
        &&DjVuBean.stringToBoolean(
          properties.getProperty("console"),
          false))
      {
        try
        {
          console=Class.forName("DjVuConsole").newInstance();
        }
        catch(final Throwable ignored) {}
      }

      final Dimension screenSize = getToolkit().getScreenSize();
      djvuBean.addPropertyChangeListener(this);

      if(screenSize.width < 640)
      {
        final DjVuImage image = djvuBean.getImageWait();
      }

      setLayout(new BorderLayout());

      final Component toolbar = djvuBean.getToolbar();

      if(toolbar != null)
      {
        add(toolbar, BorderLayout.NORTH);
      }

      add(
        getSplitPane(
          djvuBean.getOutline(),
          getCenterPane()),
        BorderLayout.CENTER);

      final Dimension size = getSize();

      if(size != null)
      {
        djvuBean.setTargetWidth(size.width);
      }

      final long   t = System.currentTimeMillis() - startTime;
      final String d = "000" + t;
      DjVuOptions.out.println(
        Applet.class.getName() + " initialized in " + (t / 1000L) + "."
        + d.substring(d.length() - 3) + " seconds");
      validDjVu = true;
    }
    catch(Throwable exp)
    {
      validDjVu = false;
      removeAll();
      setLayout(new BorderLayout());
      final ByteArrayOutputStream s=new ByteArrayOutputStream();
      final PrintStream p=new PrintStream(s);
      exp.printStackTrace(p);
      add(
        new TextArea(new String(s.toByteArray())),
        BorderLayout.CENTER);
      exp.printStackTrace(DjVuOptions.err);
      System.gc();
    }

    final Applet runnable = new Applet();
    runnable.parentRef = DjVuObject.createWeakReference(this, this);
    new Thread(runnable).start();
  }

  /**
   * Called when a DjVuBean propery has changed.
   *
   * @param event describing the property change.
   */
  public void propertyChange(final PropertyChangeEvent event)
  {
    try
    {
      final String name = event.getPropertyName();

      if("status".equalsIgnoreCase(name))
      {
        final Object value=event.getNewValue();
        if(isShowing())
        {
          getAppletContext().showStatus((value != null)?value.toString():"");
        }
      }
      else if("mode".equalsIgnoreCase(name))
      {
        switch(((Number)event.getNewValue()).intValue())
        {
          case DjVuBean.TEXT_MODE :
          {
            getDjVuBean().getTextArea();
            getCardLayout().show(
              getCenterPane(),
              TEXT_STRING);

            break;
          }
          default :
          {
            getCardLayout().show(
              getCenterPane(),
              IMAGE_STRING);

            break;
          }
        }
      }
      else if("page".equalsIgnoreCase(name))
      {
        final Object object = event.getNewValue();

        DjVuOptions.out.println("Page " + object);

        final DjVuBean djvuBean = getDjVuBean();

        if(djvuBean.getMode() == DjVuBean.TEXT_MODE)
        {
          getDjVuBean().getTextArea();
        }
      }
      else if("submit".equalsIgnoreCase(name))
      {
        Object object = event.getNewValue();

        DjVuOptions.out.println("Submit " + object);

        if(object instanceof String)
        {
          getAppletContext().showDocument(
            new URL(
              getURL(),
              (String)object),
            Integer.toString(object.hashCode()));
        }
        else if(object instanceof URL)
        {
          getAppletContext().showDocument(
            (URL)object,
            Integer.toString(object.hashCode()));
        }
        else if(object instanceof Number)
        {
          getDjVuBean().setPage(((Number)object).intValue());
        }
        else if(object instanceof Hyperlink)
        {
          Hyperlink rect = (Hyperlink)object;

          // DjVuOptions.out.println("submit "+rect.getURL());
          final String url = rect.getURL();

          if((url != null) && (url.length() > 0))
          {
            final String target = rect.getTarget();

            if(target != null)
            {
              getAppletContext().showDocument(
                new URL(
                  getURL(),
                  url),
                target);
            }
            else
            {
              getAppletContext().showDocument(new URL(
                  getURL(),
                  url));
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
   * Perform regular garbage collection...
   */
  public void run()
  {
    while(DjVuObject.getFromReference(parentRef) != null)
    {
//      final Runtime run  = Runtime.getRuntime();
//      final long    used = run.totalMemory() - run.freeMemory();
//      DjVuOptions.out.println("memory=" + used);
      try
      {
        Thread.sleep(5000L);
      }
      catch(final Throwable ignored) {}

      System.gc();
    }
  }
}
