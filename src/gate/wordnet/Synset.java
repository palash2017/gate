/*
 *  Synset.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import java.util.*;

import gate.*;
import gate.event.*;


/** Represents WordNet synset.
 */
public interface Synset {

  public int getPOS();

  public String getDescription();

  public boolean isUniqueBeginner();

  public String getGloss();

  public Iterator getWordSenses();

  public WordSense getWordSense(int offset);

  public Iterator getSemanticRealtions();

  public Iterator getSemanticRealtions(int type);

}

