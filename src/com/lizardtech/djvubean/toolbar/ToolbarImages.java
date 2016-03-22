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
import com.lizardtech.djvubean.DjVuImage;
import java.awt.Component;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.*;
import java.util.Hashtable;


/**
 * The ToolbarImages class acts as a static holder for all the images used by
 * the Toolbar class.
 *
 * @author $author$
 * @version $Revision: 1.10 $
 */
public class ToolbarImages
{
  //~ Static fields/initializers ---------------------------------------------

  /** Map each of the images to a name. */
  public static Hashtable table = null;

  //~ Methods ----------------------------------------------------------------

  /**
   * Query/create the Hashtable of image names to images.
   *
   * @param parent The component to create the images for.
   *
   * @return Hashtable of images.
   *
   * @throws IOException If an IOException occurs decoding the static images.
   */
  public static Hashtable getHashtable(final Component parent)
    throws IOException
  {
    if(table == null)
    {
      boolean needGC = false;

      synchronized(ToolbarImages.class)
      {
        if(table == null)
        {
          needGC = DjVuOptions.COLLECT_GARBAGE;

          final Document document = new Document();
          document.setAsync(true);
          final InputStream input=ToolbarImages.class.getResourceAsStream("/com/lizardtech/djvubean/toolbar/toolbar.djvu");
          document.read(input);
          final DjVuPage [] page = { document.getPage(0, DjVuPage.MAX_PRIORITY, true) };
          final DjVuImage image  = new DjVuImage(page,false);
          final Hashtable xtable = new Hashtable();
          xtable.put(
            "select.djvu",
            image.getImage(
              parent,
              new Rectangle(0, 8, 16, 16)));
          xtable.put(
            "search.djvu",
            image.getImage(
              parent,
              new Rectangle(16, 0, 24, 24)));
          xtable.put(
            "hand.djvu",
            image.getImage(
              parent,
              new Rectangle(40, 0, 24, 24)));
          xtable.put(
            "textselect.djvu",
            image.getImage(
              parent,
              new Rectangle(64, 0, 24, 24)));
          xtable.put(
            "actualsi.djvu",
            image.getImage(
              parent,
              new Rectangle(88, 0, 24, 24)));
          xtable.put(
            "fitwidth.djvu",
            image.getImage(
              parent,
              new Rectangle(112, 0, 24, 24)));
          xtable.put(
            "fitpage.djvu",
            image.getImage(
              parent,
              new Rectangle(136, 0, 24, 24)));
          xtable.put(
            "zoomin.djvu",
            image.getImage(
              parent,
              new Rectangle(160, 0, 24, 24)));
          xtable.put(
            "zoomout.djvu",
            image.getImage(
              parent,
              new Rectangle(184, 0, 24, 24)));
          xtable.put(
            "firstpage.djvu",
            image.getImage(
              parent,
              new Rectangle(208, 0, 24, 24)));
          xtable.put(
            "prevpage.djvu",
            image.getImage(
              parent,
              new Rectangle(232, 0, 24, 24)));
          xtable.put(
            "nextpage.djvu",
            image.getImage(
              parent,
              new Rectangle(256, 0, 24, 24)));
          xtable.put(
            "lastpage.djvu",
            image.getImage(
              parent,
              new Rectangle(280, 0, 24, 24)));
          xtable.put(
            "searchbackdoc.djvu",
            image.getImage(
              parent,
              new Rectangle(304, 0, 24, 24)));
          xtable.put(
            "searchback.djvu",
            image.getImage(
              parent,
              new Rectangle(328, 0, 24, 24)));
          xtable.put(
            "searchfwd.djvu",
            image.getImage(
              parent,
              new Rectangle(352, 0, 24, 24)));
          xtable.put(
            "searchfwddoc.djvu",
            image.getImage(
              parent,
              new Rectangle(376, 0, 24, 24)));
          xtable.put(
            "zoomselect.djvu",
            image.getImage(
              parent,
              new Rectangle(400, 0, 24, 24)));
          xtable.put(
            "lizardtech.djvu",
            image.getImage(
              parent,
              new Rectangle(424, 0, 98, 24)));
          table = xtable;
        }
      }

      if(needGC)
      {
        System.gc();
      }
    }

    return table;
  }

  /**
   * Returns the specified image in the Hashtable.
   *
   * @param parent Component to create the image.
   * @param id Name of the image.
   *
   * @return The newly created image.
   *
   * @throws IOException if an IOException occurs decoding the static data.
   */
  public static Image createImage(
    final Component parent,
    final String    id)
    throws IOException
  {
    final Image retval=((Image[])getHashtable(parent).get(id))[0];
    return retval;
  }
}
