/*
 *  InvalidOffsetException.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Valentin Tablan, Jan/2000
 *
 *  $Id$
 */

package gate.util;

/** Used to signal an attempt to create a node with an invalid offset.
  * An invalid offset is a negative value, or
  * an offset bigger than the document size, or a start greater than an end. 
  */
public class InvalidOffsetException extends GateException {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public InvalidOffsetException() {
  }

  public InvalidOffsetException(String s) {
    super(s);
  }

} // InvalidOffsetException
