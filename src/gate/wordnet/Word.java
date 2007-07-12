/*
 *  Word.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
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

import java.util.List;


/** Represents WordNet word.
 */
public interface Word {

  /** returns the senses of this word */
  public List getWordSenses() throws WordNetException;

  /** returns the lemma of this word */
  public String getLemma();

  /** returns the number of senses of this word (not necessarily loading them from storage) */
  public int getSenseCount();

}

