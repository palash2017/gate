/*
 *  FSMState.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 11/07/2000
 *
 *  $Id$
 */

package gate.creole.gazetteer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/** Implements a state of the deterministic finite state machine of the
 * gazetter.
 *
 */
class FSMState implements Serializable {

  /** Debug flag
   */
  private static final boolean DEBUG = false;

  /** Constructs a new FSMState object and adds it to the list of
   * states of the {@link DefaultGazetteer} provided as owner.
   *
   * @param owner a {@link DefaultGazetteer} object
   */
  public FSMState(DefaultGazetteer owner) {
    myIndex = index++;
    owner.fsmStates.add(this);
  }

  /** Adds a new value to the transition function
   */
// >>> DAM: was - to use charMap
/*
  void put(Character chr, FSMState state) {
    transitionFunction.put(chr,state);
  }
*/
// >>> DAM: TransArray optimization
  void put(char chr, FSMState state) {
    transitionFunction.put(chr,state);
  }
// >>> DAM: end

  /** This method is used to access the transition function of this state.
   */
// >>> DAM: was
/*
  FSMState next(Character chr) {//UnicodeType type){
    return (FSMState)transitionFunction.get(chr);
  }
  */
// >>> DAM: TransArray optimization
  FSMState next(char chr) {//UnicodeType type){
    return (FSMState)transitionFunction.get(chr);
  }
// >>> DAM: end

  /** Returns a GML (Graph Modelling Language) representation of the edges
   * emerging from this state.
   */
//<<< DAM: was - to use new char Iter returned by the charMap iteratior
/*
  String getEdgesGML() {
    String res = "";
    Iterator charsIter = transitionFunction.keySet().iterator();
    Character currentChar;
    FSMState nextState;

    while(charsIter.hasNext()){
      currentChar = (Character)charsIter.next();
      nextState = next(currentChar);
      res += "edge [ source " + myIndex +
      " target " + nextState.getIndex() +
      " label \"'" + currentChar + "'\" ]\n";
    }
*/
// DAM, TransArray optimization
  String getEdgesGML() {
    String res = "";
    char currentChar;
    FSMState nextState;

    for (int i = 0; i < transitionFunction.itemsKeys.length; i++)
    {
      currentChar = transitionFunction.itemsKeys[i];
      nextState = next(currentChar);
      res += "edge [ source " + myIndex +
      " target " + nextState.getIndex() +
      " label \"'" + currentChar + "'\" ]\n";
    }
// >>> DAM, end
    return res;
  }

  /** Checks whether this state is a final one
   */
  boolean isFinal() {
// >>> was
//    return !lookupSet.isEmpty();
// >>> BOBI, Lookup opitimization
    if (lookupSet==null)
        return false;
    return !lookupSet.isEmpty();
// >>> end
  }

  /** Returns a set of {@link Lookup} objects describing the types of lookups
   * the phrase for which this state is the final one belongs to
   */
  Set getLookupSet(){return lookupSet;}

  /** Adds a new looup description to this state's lookup descriptions set
   */
  void addLookup(Lookup lookup) {
// >>> was nothing
// >>> BOBI, Lookup opitimization
    if (lookupSet == null)
        lookupSet = new HashSet(4);
// >>> end

    lookupSet.add(lookup);
  } // addLookup

  /** Removes a looup description from this state's lookup descriptions set
   */
  void removeLookup(Lookup lookup) {
    lookupSet.remove(lookup);
  } // removeLookup

  /** Returns the unique ID of this state.
   */
  int getIndex(){ return myIndex; }


  /** The transition function of this state.
   */
// >>> was
//  Map transitionFunction = new HashMap();
// >>> NASO, hash4 optimization
//  Map transitionFunction = new HashMap(4);
// >>> DAM, TransArray
charMap transitionFunction = new charMap();
// >>> end

  /**    *
   */
// >>> was
//  Set lookupSet = new HashSet();
// >>> NASO, hash4 optimization
//  Set lookupSet = new HashSet(4);
// >>> BOBI, Lookup opitimization
  Set lookupSet;
// >>> end

  /**
   * The unique id of this state. This value is never used by the algorithms but
   * it can be useful for graphical representations.
   */
  int myIndex;

  /**
   * Class memebre used to generate unique ids for the instances
   *
   */
  static int index;

  static{
    index = 0;
  }

} // class FSMState
