/*
 *  DocumentFormatException.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 24/OCT/2000
 *
 *  $Id$
 */

package gate.util;

/**
  * This exception can be used to catch any internal exception thrown by the
  * DocumentFormat class and its subbclasses.
  */
public class DocumentFormatException extends GateException {
  /** Debug flag */
  private static final boolean DEBUG = false;

  public DocumentFormatException(){
    super();
  }
  public DocumentFormatException(String aMessage, Exception anException) {
    super(aMessage, anException);
  }
  public DocumentFormatException(String aMessage) {
    super(aMessage);
  }
  public DocumentFormatException(Exception anException) {
    super(anException);
  }  
} // DocumentFormatException
