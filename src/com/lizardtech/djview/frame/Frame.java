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
package com.lizardtech.djview.frame;

import java.lang.reflect.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import com.lizardtech.djvu.DjVuOptions;


/**
 * A class for displaying djvu documents with javaw.  Very simmular to the
 * DjVuLibre djview command.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.10 $
 */
public class Frame
  extends java.awt.Frame
  implements AppletStub, Runnable, ActionListener
{
  //~ Static fields/initializers ---------------------------------------------

  private static int openCount = 0;
  private static final Class classApplet;
  private static final Method isValidDjVuMethod;
  private static final String classAppletName="com.lizardtech.djview.Applet";
  static
  {
      Class xclassApplet=null;
      try
      {
        xclassApplet=Class.forName(classAppletName);
      }
      catch(final ClassNotFoundException exp)
      {
         exp.printStackTrace(DjVuOptions.err);
         System.exit(1);
      }
      classApplet=xclassApplet;
      Method xisValidDjVuMethod=null;
      try
      {
          xisValidDjVuMethod=classApplet.getMethod("isValidDjVu", null);
      }
      catch(final Throwable exp)
      {
          exp.printStackTrace(DjVuOptions.err);
          System.exit(1);
      }
      isValidDjVuMethod=xisValidDjVuMethod;
  }

  //~ Instance fields --------------------------------------------------------

  private final AppletContext appletContext;
  private final Button        back    = new Button("Back");
  private final Button        forward = new Button("Forward");

  /** Create an applet for this document. */
  private final Hashtable parameters     = new Hashtable();
  private final Panel     panel          = new Panel();
  private String          currentURL     = null;
  private final TextField input          = new TextField();
  private URL             documentBase   = null;
  private Vector          history        = new Vector();
  private boolean         isDjVu         = false;
  private int             currentHistory = 0;

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates a new Frame object.
   */
  public Frame()
  {
    this(null);
  }

  /**
   * Creates a new Frame object.
   *
   * @param url The initial URL to load.
   */
  public Frame(final String url)
  {
    panel.add(input);
    panel.add(back);
    panel.add(forward);
    openCount++;
    appletContext =
      new AppletContext()
        {
          public void showStatus(final String status) {DjVuOptions.out.println("status: "+status);}

          public void showDocument(
            final URL url,
            String    target)
          {
            final Frame f = new Frame();
            f.setURL(url);
            (new Thread(f)).start();
          }

          public void showDocument(final URL url)
          {
            setURL(url);
          }

          public void setStream(
            final String key,
            InputStream  stream) {}

          public Iterator getStreamKeys()
          {
            return null;
          }

          public InputStream getStream(final String key)
          {
            return null;
          }

          public Image getImage(final URL url)
          {
            return null;
          }

          public AudioClip getAudioClip(final URL url)
          {
            return null;
          }

          public Enumeration getApplets()
          {
            return null;
          }

          public Applet getApplet(final String name)
          {
            return null;
          }
        };

    try
    {
      documentBase =
        (new File(
          System.getProperties().getProperty("user.dir", "/"),
          "/index.djvu")).toURL();
    }
    catch(final Throwable ignored) {}

    setURL(url);
  }

  //~ Methods ----------------------------------------------------------------

  /**
   * Get the applet context, used for retrieving web contents.
   *
   * @return null
   */
  public AppletContext getAppletContext()
  {
    return appletContext;
  }

  /**
   * Get the codebase for the applet.
   *
   * @return null.
   */
  public URL getCodeBase()
  {
    return null;
  }

  /**
   * Get the document base for the applet.
   *
   * @return null.
   */
  public URL getDocumentBase()
  {
    return documentBase;
  }

  /**
   * Lookup a parameter.
   *
   * @param name The name of the parameter to lookup.
   *
   * @return the parameter value.
   */
  public String getParameter(String name)
  {
    Object retval = parameters.get(name);

    return (retval != null)
    ? retval.toString()
    : null;
  }

  /**
   * Set the URL to display in this frame.
   *
   * @param url to display.
   */
  public void setURL(final String url)
  {
    if((url == null) || (url.length() == 0))
    {
      setURL((URL)null);
    }
    else
    {
      try
      {
        setURL(new URL(
            getDocumentBase(),
            url));
      }
      catch(final Throwable ignored) {}
    }
  }

  private void printFile(final StringBuffer text,final File x, final String name)
    throws MalformedURLException
  {
    if(x.canRead())
    {
      String size="-";
      if(!x.isDirectory())
      {
        double s=((double)x.length())/1024D;
        if(s >= 1024D)
        {
          size=(Math.ceil(s/102.4D)/10D)+"M";
        }
        else
        {
          size=((int)Math.ceil(s)+"K");
        }
      }
      text.append("<tr><td>&nbsp;</td><td><a href=\""+(x.toURL())+"\">"+name+"</a></td><td align=\"right\">"+(new Date(x.lastModified()))+"</td><td align=\"right\">"+size+"</td></tr>\n");
    }
  }
  
  /**
   * Set the URL to display in this frame.
   *
   * @param url to display.
   */
  public void setURL(final URL url)
  {
    String urlString = "";

    Component component = null;
    if(url != null)
    {
      urlString = url.toString();

      if(history.size() <= currentHistory)
      {
        history.insertElementAt(
          url.toString(),
          currentHistory);
      }
      else if(!history.elementAt(currentHistory).equals(urlString))
      {
        history.setSize(++currentHistory);
        history.insertElementAt(
          url.toString(),
          currentHistory);
      }

      JEditorPane jeditorPane = null;
      if(url.getProtocol().equals("file"))
      {
        File f=new File(url.getPath());
        if(f.isDirectory())
        {
          final String path=f.getAbsolutePath();
          final StringBuffer text=new StringBuffer("<html><head>Index of "+path+"</head><body><h1>Index of "+path+"</h1><table><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b>Name</b></td><td><b>Last Modified</b></td><td><b>Size</b></td></tr>\n");
          try
          {
            printFile(text,f.getParentFile(), "<em>Parent Directory</em>");
          }
          catch(final Throwable exp)
          {
            final File [] roots=f.listRoots();
            for(int i=0;i<roots.length;i++)
            {
              try
              {
                final File x=roots[i];
                printFile(text,x, "<em>"+x.getAbsolutePath()+"</em>");
              }
              catch(final Throwable ignored) {}
            }
          }
          final File [] list=f.listFiles();
          for(int i=0;i<list.length;i++)
          {
             try
             {
               final File x=list[i];
               if(x.isDirectory()&&!x.isHidden())
               {
                 printFile(text,x,x.getName()+File.separator);
               }
             }
             catch(final Throwable ignored) {}
          }
          for(int i=0;i<list.length;i++)
          {
             try
             {
               final File x=list[i];
               if(!x.isDirectory()&&!x.isHidden())
               {
                 printFile(text,x,x.getName());
               }
             }
             catch(final Throwable ignored) {}
          }
          text.append("</table></body></html>");    
          jeditorPane = new JEditorPane("text/html",text.toString());
        }
      }
      if(jeditorPane == null)
      {
        try
        {
          if(jeditorPane != null)
          {
            throw new Exception();
          }
          new com.lizardtech.djvu.Document(url);
        }
        catch(final Throwable exp)
        {
          try
          {
            jeditorPane = new JEditorPane(url);
          }
          catch(final Throwable ignored) {}
        }
      }
      if((jeditorPane != null)
          && !(("text/plain".equals(jeditorPane.getContentType())
          && (jeditorPane.getText().startsWith("AT&T")))))
      {
        try
        {
//          jeditorPane.setEditable(false);
//          use reflection for gcj compatability.
          final Class[]  params = {Boolean.TYPE};
          final Object[] args = {Boolean.FALSE};
          JEditorPane.class.getMethod("setEditable", params).invoke(
            jeditorPane,
            args);
          jeditorPane.addHyperlinkListener(
            new HyperlinkListener()
            {
              public void hyperlinkUpdate(final HyperlinkEvent e)
              {
                try
                {
                  if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                  {
                    setURL(e.getURL());
                  }
                }
                catch(final Throwable exp)
                {
                  exp.printStackTrace(DjVuOptions.err);
                  System.gc();
                }
              }
            });
          component = new JScrollPane(jeditorPane);
          jeditorPane.setCaretPosition(0);
        }
        catch(Throwable ignored) {}
      }

      if(component == null)
      {
        parameters.put("data", urlString);
      
        try
        {
          final Applet s = (Applet)classApplet.newInstance();
          s.setStub(this);
          s.init();
          component   = s;
          isDjVu      = ((Boolean)isValidDjVuMethod.invoke(s,null)).booleanValue();
        }
        catch(final Throwable exp)
        {
          exp.printStackTrace(DjVuOptions.err);
          component=null;
          isDjVu=false;
        }
        
      }
    }
    else
    {
      isDjVu=false;        
    }

    removeAll();
    invalidate();
    System.gc();
    setLayout(new BorderLayout());
    input.setColumns(80);
    input.setText(urlString);
    input.removeActionListener(this);
    input.addActionListener(this);
    back.setEnabled((currentHistory > 0));
    back.removeActionListener(this);
    back.addActionListener(this);
    forward.setEnabled(((currentHistory + 1) < history.size()));
    forward.removeActionListener(this);
    forward.addActionListener(this);
    add(panel, BorderLayout.NORTH);
    if(component == null)
    {
      component=new TextArea("No Data Loaded");
    }
    add(component, BorderLayout.CENTER);
    validate();
  }

  /**
   * Called by the buttons and text fields when an update occures.
   *
   * @param e DOCUMENT ME!
   */
  public void actionPerformed(ActionEvent e)
  {
    Object source = e.getSource();

    if(source == input)
    {
      final String newURL = e.getActionCommand();

      if(!newURL.equals(currentURL))
      {
        DjVuOptions.out.println("setURL " + currentHistory);
        history.setSize(++currentHistory);
        setURL(newURL);
      }
    }
    else if(source == back)
    {
      DjVuOptions.out.println("back " + currentHistory);
      setURL(history.elementAt(--currentHistory).toString());
    }
    else if(source == forward)
    {
      DjVuOptions.out.println("forward " + currentHistory);
      setURL(history.elementAt(++currentHistory).toString());
    }
  }

  /**
   * This applet may also be invoked as a program using javaw.
   *
   * @param args Should contain the target URL.
   */
  public static void main(String[] args)
  {
    try
    {
      final Frame f = new Frame((args.length > 0)
          ? (args[0])
          : ".");
      f.run();
      Thread.sleep(30000L);
    }
    catch(Throwable exp)
    {
      exp.printStackTrace(DjVuOptions.err);
      System.exit(1);
    }
  }

  /**
   * Test if the applet active?
   *
   * @return true
   */
  public boolean isActive()
  {
    return true;
  }

  /**
   * Resize the window.
   *
   * @param width The new window width.
   * @param height The new window height.
   */
  public void appletResize(
    int width,
    int height)
  {
    setSize(width, height);
  }

  /**
   * Called to show the window.
   */
  public void run()
  {
    setSize(800, 600);
    addWindowListener(
      new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          if(--openCount < 1)
          {
            System.exit(0);
          }

          setVisible(false);
          dispose();
        }
      });
    setVisible(true);
  }
}
