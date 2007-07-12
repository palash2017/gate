/*
 *  LexKBSynset.java
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 28/January/2003
 *
 *  $Id$
 */
package gate.lexicon;


public interface MutableLexicalKnowledgeBase extends LexicalKnowledgeBase {

  /** returns the lexicon version */
  public void setVersion(String newVersion);

  /** add a new word */
  public MutableWord addWord(String lemma);

  /** add a new synset */
  public MutableLexKBSynset addSynset();

  public void addPOSType(Object newPOSType);

  public void removeWord(MutableWord theWord);

  public void removeSynset(MutableLexKBSynset synset);
}