/*
 *	Annotation.java
 *
 *	Hamish Cunningham, 19/Jan/00
 *
 *	$Id$
 */

package gate;

import gate.util.*;

/** An Annotation is an arc in an AnnotationGraph. It is immutable, to avoid
  * the situation where each annotation has to point to its parent graph in
  * order to tell it to update its indices when it changes.
  * <P> Changes from TIPSTER: no ID; single span only.
  */
public interface Annotation extends FeatureBearer, IdBearer, Comparable {

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType();

  /** The start node. */
  public Node getStartNode();

  /** The end node. */
  public Node getEndNode();

  /** Ordering */
  public int compareTo(Object o) throws ClassCastException;

} // interface Annotation
