/*
 *  GateException.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/01/2000
 *
 *  $Id$
 */

package gate.util;

import java.io.PrintStream;

/** A superclass for exceptions in the GATE packages. Can be used
  * to catch any internal exception thrown by the GATE libraries.
  * (Of course
  * other types of exception may be thrown, but these will be from other
  * sources such as the Java core API.)
  */
public class GateException extends Exception {

  /** Debug flag */
  private static final boolean DEBUG = false;

  protected Throwable e;

  public GateException() {
    super();
  }

  public GateException(String s) {
    super(s);
  }

  public GateException(Throwable e) {
    super(e.toString());
    this.e = e;
  }

  public void printStackTrace(){
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintStream s){
    super.printStackTrace(s);
    if(e!= null){
      System.err.println("From:");
      e.printStackTrace(s);
    }
  }
} // GateException
