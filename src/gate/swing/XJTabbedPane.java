/*  XJTabbedPane.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 04/04/2001
 *
 *  $Id$
 *
 */

package gate.swing;

import java.awt.Point;
import java.awt.Component;

import javax.swing.JTabbedPane;
import javax.swing.Icon;

/**
 * An extended version of {@link javax.swing.JTabbedPane}.
 */
public class XJTabbedPane extends JTabbedPane {

  public XJTabbedPane(int tabPlacement){
    super(tabPlacement);
  }

  /**
   * Gets the tab index for a given location
   */
  public int getIndexAt(Point p){
    for(int i = 0; i < getTabCount(); i++){
      if(getBoundsAt(i).contains(p)) return i;
    }
    return -1;
  }// int getIndexAt(Point p)

  public void setTitleAt(int index, String title) {
    if (title.length() > 15) { // shorten the tab title with ellipsis
      setToolTipText("<html>" + title + "<br>" + getToolTipText() + "</html>");
      title = title.substring(0, 15) + "\u2026";
      super.setTitleAt(index, title);
    } else {
      super.setTitleAt(index, title);
    }
  }

  public void insertTab(String title, Icon icon, Component component, String tip, int index) {
    if (title.length() > 15) { // shorten the tab title with ellispsis
      tip = "<html>" + title + "<br>" + tip + "</html>";
      title = title.substring(0, 15) + "\u2026";
      super.insertTab(title, icon, component, tip, index);
    } else {
      super.insertTab(title, icon, component, tip, index);
    }
  }

}// class XJTabbedPane extends JTabbedPane