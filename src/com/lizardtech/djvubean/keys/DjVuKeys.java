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
package com.lizardtech.djvubean.keys;

import com.lizardtech.djvubean.*;
import java.awt.event.*;

/**
 * This class implements a keyboard short cuts  the user may use to navigate the
 * DjVu Document.
 *
 * @author Bill C. Riemers
 * @version $Revision: 1.3 $
 */
public class DjVuKeys implements KeyListener 
{
  // DjVuBean to control
  protected final DjVuBean bean;


  /**
   * Creates a new MenuBar object.
   *
   * @param bean DjVuBean to add this menu to.
   */
  public DjVuKeys(final DjVuBean bean)
  {
    this.bean=bean;
    bean.addKeyListener(this);
    final java.awt.TextArea text=bean.getTextArea();
    if(text != null)
    {
      text.addKeyListener(this);
    }
//    bean.addPropertyChangeListener(this);
    bean.properties.put(
      "addOn.keys",
      bean.properties.getProperty("keys", "true"));
  }

  /**
   * Query if keyboard shortcuts are enabled.
   */
  public boolean isEnabled()
  {
    return DjVuBean.stringToBoolean(bean.properties.getProperty("addOn.keys"),true);      
  }
  
    /** Handle the key typed event from the text field. */
    public void keyTyped(KeyEvent e)
    {
      if(isEnabled())
      {
        switch(e.getKeyChar())
        {
            case '1':
                bean.setZoom(DjVuBean.ZOOM100);
                break;
            case '2':
                bean.setZoom(DjVuBean.ZOOM150);
                break;
            case '3':
                bean.setZoom(DjVuBean.ZOOM300);
                break;
            case '4':
                bean.setZoom("400%");
                break;
            case '5':
                bean.setZoom("500%");
                break;
            case '6':
                bean.setZoom("600%");
                break;
            case '7':
                bean.setZoom("700%");
                break;
            case '8':
                bean.setZoom("800%");
                break;
            case '9':
                bean.setZoom("900%");
                break;
            case '-':
            case '_':
                bean.setZoom(DjVuBean.ZOOM_OUT);
                break;
            case '=':
            case '+':
                bean.setZoom(DjVuBean.ZOOM_IN);
                break;
            case ' ':
                bean.setPage(bean.getPage()+bean.getVisiblePageCount());
                break;
            case 'g':
                bean.properties.put("addOn.toolbar.page","true");
                break;
            case 'f':
                String v=bean.properties.getProperty("addOn.finder");
                if("true".equalsIgnoreCase(v))
                {
                  bean.properties.put("addOn.finder","false");
                }
                else if("false".equalsIgnoreCase(v))
                {
                  bean.properties.put("addOn.finder","true");                    
                }
                break;
            case '0':
            case 'p':
                bean.setZoom(DjVuBean.ZOOM_FIT_PAGE);
                break;
            case 'w':
                bean.setZoom(DjVuBean.ZOOM_FIT_WIDTH);
                break;
            case KeyEvent.VK_BACK_SPACE:
                bean.setPage(bean.getPage()-bean.getVisiblePageCount());
                break;                
//            default:
//  	        displayInfo(e, "KEY TYPED: ");
//                break;
        }
      }
    }

    /** Handle the key pressed event from the text field. */
    public void keyPressed(KeyEvent e)
    {
      if(isEnabled())
      {
        switch(e.getKeyCode())
        {
            case KeyEvent.VK_PAGE_DOWN:
                bean.setScroll(DjVuBean.SCROLL_PAGE_DOWN);
                break;
            case KeyEvent.VK_PAGE_UP:
                bean.setScroll(DjVuBean.SCROLL_PAGE_UP);
                break;
            case KeyEvent.VK_DOWN:
                bean.setScroll(DjVuBean.SCROLL_DOWN);
                break;
            case KeyEvent.VK_UP:
                bean.setScroll(DjVuBean.SCROLL_UP);
                break;
            case KeyEvent.VK_LEFT:
                bean.setScroll(DjVuBean.SCROLL_LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                bean.setScroll(DjVuBean.SCROLL_RIGHT);
                break;
//            default:
//                displayInfo(e, "KEY PRESSED: ");
//                break;
        }
      }
    }

    /** Handle the key released event from the text field. */
    public void keyReleased(KeyEvent e) {
//      displayInfo(e, "KEY RELEASED: ");
    }
    
//    protected void displayInfo(KeyEvent e, String s)
//    {
//        //You should only rely on the key char if the event
//        //is a key typed event.
//        int id = e.getID();
//        String keyString;
//        if (id == KeyEvent.KEY_TYPED) {
//            char c = e.getKeyChar();
//            keyString = "key character = '" + c + "'";
//        } else {
//            int keyCode = e.getKeyCode();
//            keyString = "key code = " + keyCode
//                        + " ("
//                        + KeyEvent.getKeyText(keyCode)
//                        + ")";
//        }

//        int modifiers = e.getModifiersEx();
//        String modString = "modifiers = " + modifiers;
//        String tmpString = KeyEvent.getModifiersExText(modifiers);
//        if (tmpString.length() > 0) {
//            modString += " (" + tmpString + ")";
//        } else {
//            modString += " (no modifiers)";
//        }

//        String actionString = "action key? ";
//        if (e.isActionKey()) {
//            actionString += "YES";
//        } else {
//            actionString += "NO";
//        }

//        String locationString = "key location: ";
//        int location = e.getKeyLocation();
//        if (location == KeyEvent.KEY_LOCATION_STANDARD) {
//            locationString += "standard";
//        } else if (location == KeyEvent.KEY_LOCATION_LEFT) {
//            locationString += "left";
//        } else if (location == KeyEvent.KEY_LOCATION_RIGHT) {
//            locationString += "right";
//        } else if (location == KeyEvent.KEY_LOCATION_NUMPAD) {
//            locationString += "numpad";
//        } else { // (location == KeyEvent.KEY_LOCATION_UNKNOWN)
//            locationString += "unknown";
//        }
//        DjVuOptions.out.println(s+"\t"+keyString+"\t"+modString+"\t"+actionString+"\t"+locationString);
//    }
}

