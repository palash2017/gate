/*
 *  InvalidRuleException.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, 21.06.2000
 *
 *  $Id$
 */

package gate.creole.tokeniser;

/** Fired when an invalid tokeniser rule is found
  */
public class InvalidRuleException extends TokeniserException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public InvalidRuleException(String s) {
    super(s);
  }

} // class InvalidRuleException
