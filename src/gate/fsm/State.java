/*
*	State.java
*
*	Valentin Tablan, 11/Apr/2000
*
*	$Id$
*/

package gate.fsm;

import java.util.*;
import gate.jape.*;
/**
* This class implements a Finite State Machine state.
*/
public class State {

  /**
  * Build a new state belonging to the given FSM.
  *@param owner the FSM that owns this state.
  */
  public State(FSM owner) {
    myIndex = State.index++;
    owner.addState(this);
    isFinal = false;
  }

  /**
  * Reports if this state is a final one.
  * Note: A state has an associated action if and only if it is final.
  */
  public boolean isFinal(){
    return isFinal;
  }

  /**
  *  Gets the set of transitions for this state.
  *@return a Set contining objects of type gate.fsm.Transition
  */
  protected Set getTransitions(){
    return transitions;
  }

  /*
  * Adds an action to this state. A state having at least one action is a
  * final one. An action is actually a gate.jape.RightHandSide object.
  *@param trans the last transition used to reach this state before the
  *action needs to be fired.
  *@param rhs the action
  */
  //I don't think this complicated mechenism is actually necessary.
  //It looks like a final state can only have one associated action so
  //we might be able to simplify thing a little.
  //Note: I was rather dizzy when I thought this might be needed so I might
  //have been wrong :)
  protected void setAction(RightHandSide rhs){
    action = rhs;
    isFinal = true;
  }

  /**
  * Gets the action associated to this state when reaced via a given transition.
  *@param key one of the transitions that reach this state
  *@return the action associated to this state when accessed via the given
  *transition
  */
  public RightHandSide getAction(){
    return action;
  }

  /**
  * Gets the set of all actions associated to this state
  */
  //If I am right this set (collection) will always be a singleton.
//  protected Collection getAllActions(){
//    return actions.values();
//  }

  /**
  * Adds a new transition to the list of outgoing transitions for this state.
  *@param transition the transition to be added
  */
  public void addTransition(Transition transition){
    transitions.add(transition);
  }

  /**
  * Gets the index of this state. Each state has a unique index (a int value).
  * This value is not actually used by any of the algorithms. It is useful only
  * as a way of refering to states.
  *@return the index associated to this state
  */
  protected int getIndex(){
    return myIndex;
  };


  /**
  * Returns a GML (graph modelling language) representation for the edges
  * corresponding to transitions departing from this state in the
  * transition graph of the FSM to which this state belongs
  *@return a string value contining the GML text
  */
  public String getEdgesGML(){
    String res = "";
    Iterator transIter = transitions.iterator();
    BasicPatternElement bpe;
    while(transIter.hasNext()){
      Transition currentTrans = (Transition)transIter.next();
      res += "edge [ source " + myIndex +
             " target " + currentTrans.getTarget().getIndex() +
             " label \"" + currentTrans.shortDesc() + ":";
             bpe = currentTrans.getConstraints();
             if(bpe == null) res += "null";
             else res += bpe.shortDesc();
             res += " :" + currentTrans.getLabels() +
             "\" ]\n";
    }
    return res;
  }

  /**
  * Returns a textual description of this state
  *@return a String value.
  */
  public String toString(){
    String res = "State " + myIndex;
    if(isFinal()) res += "\nFinal!";
    res += "\nTransitions:\n";
    Iterator transIter = transitions.iterator();
    while(transIter.hasNext()){
      res += transIter.next().toString();
    }
    return res;
  }


  /**
  * A set of objects of type gata.fsm.Transition representing the outgoing
  * transitions.
  */
  private Set transitions = new HashSet();

  /**
  * Is this state a final one?
  */
  protected boolean isFinal = false;
  /**
  * The right hand side associated to the rule for which this state recognizes
  * the lhs.
  */

  /**
  * The actions associated to this state.
  * We use a map because we associate an action to which transition that reaches
  * this state.
  */
//  protected Map actions = new HashMap();

  /**
  * The member from the time we only used one action for each final state.
  * It is still here because we might reverse to this architecture.
  */
  protected RightHandSide action = null;

  /**
  * The unique index of this state.
  */
  protected int myIndex;

  /**
  * The class data member used for generating unique indices for State
  * instances.
  */
  protected static int index = 0;
}
