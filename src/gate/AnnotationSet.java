/*
 *  AnnotationSet.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 7/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import gate.event.AnnotationSetListener;
import gate.event.GateListener;
import gate.util.InvalidOffsetException;

/** Annotation sets */
public interface AnnotationSet extends Set, Cloneable, Serializable
{
  /** Create and add an annotation with pre-existing nodes,
    * and return its id
    */
  public Integer add(Node start, Node end, String type, FeatureMap features);

  /** Create and add an annotation with a pre-existing ID */
  public void add(
    Integer id, Long start, Long end, String type, FeatureMap features
  ) throws InvalidOffsetException;

  /** Create and add an annotation and return its id */
  public Integer add(Long start, Long end, String type, FeatureMap features)
    throws InvalidOffsetException;

  /** Add an existing annotation. Returns true when the set is modified. */
  public boolean add(Object o);

  /** Get an iterator for this set */
  public Iterator iterator();

  /** The size of this set */
  public int size();

  /** Remove an element from this set. */
  public boolean remove(Object o);

  /** Find annotations by id */
  public Annotation    get(Integer id);

  /** Get all annotations */
  public AnnotationSet get();

  /** Select annotations by type */
  public AnnotationSet get(String type);

  /** Select annotations by a set of types. Expects a Set of String. */
  public AnnotationSet get(Set types);

  /** Select annotations by type and features */
  public AnnotationSet get(String type, FeatureMap constraints);

  /** Select annotations by type and feature names
   *  It returns all annotations of the given type that
   *  have the given set of features, regardless of their
   *  concrete values
   *  If the type == null, then select regardless of type
   *  */
  public AnnotationSet get(String type, Set featureNames);

  /** Select annotations by type, features and offset */
  public AnnotationSet get(String type, FeatureMap constraints, Long offset);

  /** Select annotations by offset. This returns the set of annotations
    * whose start node is the least such that it is greater than or equal
    * to offset. If a positional index doesn't exist it is created.
    */
  public AnnotationSet get(Long offset);

  /** Select annotations by offset. This returns the set of annotations
    * that overlap totaly or partially the interval defined by the two
    * provided offsets
    */
  public AnnotationSet get(Long startOffset, Long endOffset);

  /** Select annotations by offset and type. This returns the set of annotations
    * that overlap totaly or partially the interval defined by the two
    * provided offsets and are of the given type
    */
  public AnnotationSet get(String type, Long startOffset, Long endOffset);

  /** Select annotations by offset. This returns the set of annotations
    * that are contained in the interval defined by the two
    * provided offsets. The difference with get(startOffset, endOffset) is
    * that the latter also provides annotations that have a span which
    * covers completely and is bigger than the given one. Here we only get
    * the annotations between the two offsets.
    */
  public AnnotationSet getContained(Long startOffset, Long endOffset);


  /** Get the node with the smallest offset */
  public Node firstNode();

  /** Get the node with the largest offset */
  public Node lastNode();

  /** Get the first node that is relevant for this annotation set and which has
    * the offset larger than the one of the node provided.
    */
  public Node nextNode(Node node);

  /** Get the name of this set. */
  public String getName();

  /** Get a set of java.lang.String objects representing all the annotation
    * types present in this annotation set.
    */
  public Set getAllTypes();

  /** Get the document this set is attached to. */
  public Document getDocument();

  public void addAnnotationSetListener(AnnotationSetListener l);

  public void removeAnnotationSetListener(AnnotationSetListener l);

  public void addGateListener(GateListener l);

  public void removeGateListener(GateListener l);

} // interface AnnotationSet
