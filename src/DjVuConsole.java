//C- -------------------------------------------------------------------
//C- Java DjVu (r) (v. 0.8)
//C- Copyright (c) 2005 Foxtrot Technologies Inc.
//C-
//C- This software is subject to, and may be distributed under, the
//C- GNU General Public License, Version 2. The license should have
//C- accompanied the software or you may obtain a copy of the license
//C- from the Free Software Foundation at http://www.fsf.org .
//C-
//C- In addition, as a special exception, Foxtrot Technologies Inc. 
//C- gives permission to link the code of this program with the 
//C- proprietary Java implementation provided by Sun (or other vendors 
//C- as well), and distribute linked combinations including the two. You 
//C- must obey the GNU General Public License in all respects for all of 
//C- the code used other than the proprietary Java implementation. If you 
//C- modify this file, you may extend this exception to your version of 
//C- the file, but you are not obligated to do so. If you do not wish to 
//C- do so, delete this exception statement from your version.
//C- -------------------------------------------------------------------
//C- Developed by Bill C. Riemers, Foxtrot Technologies Inc.
//C- -------------------------------------------------------------------
//
/** 
 * This class is needed by java 1.1 to display a java console.
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import com.lizardtech.djvu.DjVuOptions;

/**
 * This class implements a simple Java Console for use on platforms
 * which have no other console.
 *
 * @author docbill
 */
public class DjVuConsole 
  extends OutputStream
  implements WindowListener
{
  // This is the text area to display.
  private final TextArea text=new TextArea();
  
  // The buffer is used to accumulate one full line 
  // prior to updating the display.
  private final StringBuffer buffer=new StringBuffer();
  
  // This is the frame to display.
  private Frame frame;
  
  /**
   * Creates a new instance of DjVuConsole 
   */
  public DjVuConsole() 
  {
    frame=new Frame("Java DjVu Console");
    frame.setLayout( new BorderLayout() );
    frame.add( "Center", text );
    DjVuOptions.out=DjVuOptions.err=new PrintStream(this);
    frame.setSize(640, 480);
    frame.setVisible(true);
    frame.addWindowListener(this);
  }
    
  /** 
   * Called when the window is closing.
   */
  public void windowClosing(WindowEvent e)
  {
    DjVuOptions.out=System.out;
    DjVuOptions.err=System.err;
    frame.setVisible(false);
    frame.dispose();
    frame=null;
  }
  
  /** 
   * Called when the window is activated.
   */
  public void windowActivated(WindowEvent e) {}
  
  /** 
   * Called when the window is closed.
   */
  public void windowClosed(WindowEvent e) {}

  /** 
   * Called when the window is deactivated.
   */
  public void windowDeactivated(WindowEvent e) {}
  
  /** 
   * Called when the window is deiconified.
   */
  public void windowDeiconified(WindowEvent e) {}
  
  /** 
   * Called when the window is iconified.
   */
  public void windowIconified(WindowEvent e) {}
  
  /**
   * Called when the window is opened
   */
  public void windowOpened(WindowEvent e) {}
  
  /**
   * Write the specified character to the console window.
   *
   * @param c the character to write
   */
  public void write(int c)
  {
    switch(c)
    {
      case '\n':
        buffer.append((char)c);
        text.append(buffer.toString());
        buffer.setLength(0);
        break;
      case '\r':
        break;
      default:
        buffer.append((char)c);
    }
  }
}
