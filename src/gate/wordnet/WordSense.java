/*
 *  WordSense.java
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


/** Represents WordNet word sense.
 */
public interface WordSense {

  public Word getWord();

  public int getPOS();

  public Synset getSynset();

  public int getSenseNumber();

  public int getOrderInSynset();

  public boolean isSemcor();

  public List getLexicalRelations() throws WordNetException ;

  public List getLexicalRelations(int type) throws WordNetException ;

}

