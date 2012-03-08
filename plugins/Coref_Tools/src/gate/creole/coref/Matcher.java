/*
 *  AnaphorMatcher.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 19 Jan 2012
 *  
 *  $Id$
 */
package gate.creole.coref;

import gate.Annotation;

import java.util.List;

public interface Matcher {
  
  public String getAnnotationType();
  
  public String getAntecedentType();
  
  
  public boolean matches(Annotation[] anaphors, int antecedent, 
                                  int anaphor, CorefBase owner);
}
