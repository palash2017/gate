/*
 *  ProgressPrinter.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 21/07/2000
 *
 *  $Id$
 */

package gate.util;

import java.io.*;

import gate.gui.*;


public class ProgressPrinter implements ProgressListener {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public ProgressPrinter(PrintStream out, int numberOfSteps) {
    this.out = out;
    this.numberOfSteps = numberOfSteps;
  }

  public ProgressPrinter(PrintStream out) {
    this.out = out;
  }

  public void processFinished() {
    for(int i = currentValue; i < numberOfSteps; i++) {
      out.print("#");
    }
    out.println("]");
    currentValue = 0;
    started = false;
  }

  public void progressChanged(int newValue) {
    if(!started){
      out.print("[");
      started = true;
    }
    newValue = newValue * numberOfSteps / 100;
    if(newValue > currentValue){
      for(int i = currentValue; i < newValue; i++) {
        out.print("#");
      }
      currentValue = newValue;
    }
  }

  int currentValue = 0;

  int numberOfSteps = 70;

  PrintStream out;

  boolean started = false;

} // class ProgressPrinter
