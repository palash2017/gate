/*
 *  LexicalRelation.java
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



/** Represents WordNet lexical relation.
 */
public interface LexicalRelation extends Relation {

  /** returns the source (WordSense) of this lexical relation */
  public WordSense getSource();

  /** returns the target (WordSense) of this lexical relation */
  public WordSense getTarget();

}

