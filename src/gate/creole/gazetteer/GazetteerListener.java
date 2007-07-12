/*  GazetteerListener.java
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  borislav popov 08/05/2000
 *
 *  $Id$
 */
package gate.creole.gazetteer;

import java.util.EventListener;

/**
 * Listener for GazetteerEvents
 */
public interface GazetteerListener extends EventListener {

  /**
   * Called when a Gazetteer event has occured
   * @param e Gazetteer Event   */
  public void processGazetteerEvent(GazetteerEvent e);

}