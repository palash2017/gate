/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 12/12/2000
 *
 *  $Id$
 */
package gate.event;

/**
 * A listener for events fired by the {@link gate.CreoleRegister}
 * ({@link gate.event.CreoleEvent}).
 * In a Gate system there are many classes that can fire {@link CreoleEvent}s
 * but all this events are collected and fired back by the
 * {@link CreoleRegister} that can be obtained with a call to
 * {@link Gate#getCreoleRegister()}
 */
public interface CreoleListener extends java.util.EventListener{

  /**Called when a new {@link gate.Resource} has been loaded into the system*/
  public void resourceLoaded(CreoleEvent e);

  /**Called when a {@link gate.Resource} has been removed from the system*/
  public void resourceUnloaded(CreoleEvent e);

  /**Called when a {@link gate.Datastore} has been opened*/
  public void datastoreOpened(CreoleEvent e);

  /**Called when a {@link gate.Datastore} has been created*/
  public void datastoreCreated(CreoleEvent e);

  /**Called when a {@link gate.Datastore} has been closed*/
  public void datastoreClosed(CreoleEvent e);

}