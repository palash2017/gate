/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva 12/11/2001
 *
 *  $Id$
 */
package gate.event;

import java.util.EventListener;

/**
 * The listenre for the toplevel events generated by the Gate system.
 */
public interface FeatureMapListener extends EventListener {

  /**
   * Called when a feature map has been updated
   */
  public void featureMapUpdated();

}