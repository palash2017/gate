/*
 *  OpenTransactionsException.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, 21 Feb 2000
 *
 *  $Id$
 */

package gate.util;

/** Used to signal an attempt to close all connections to a database while there
  * are still connections in use by the clients of that database.
  */
public class OpenTransactionsException extends GateException {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public OpenTransactionsException() {
  }

  public OpenTransactionsException(String s) {
    super(s);
  }

} // OpenTransactionsException

