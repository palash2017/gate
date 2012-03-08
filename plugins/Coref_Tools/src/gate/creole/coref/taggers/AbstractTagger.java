/*
 *  AbstractTagger.java
 *
 *  Copyright (c) 1995-2012, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 28 Feb 2012
 *
 *  $Id$
 */
package gate.creole.coref.taggers;

import gate.creole.coref.Tagger;

/**
 *
 */
public abstract class AbstractTagger implements Tagger {
  
  protected String annotationType;

  protected AbstractTagger(String annotationType) {
    this.annotationType = annotationType;
  }

  /* (non-Javadoc)
   * @see gate.creole.coref.Tagger#getAnnotationType()
   */
  @Override
  public String getAnnotationType() {
    return annotationType;
  }
}
