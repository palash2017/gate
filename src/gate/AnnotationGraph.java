/*
	AnnotationGraph.java

	Hamish Cunningham, 19/Jan/2000

	$Id$
*/

package gate;
import java.util.*;
import gate.util.*;

/** Annotation graphs are defined at 
  * <A HREF=http://www.ldc.upenn.edu/annotation/>the LDC's annotation site</A>
  */
public interface AnnotationGraph 
{
  /** find a node by ID */
  public Node getNode(Long id);

//  /** Greatest lower bound on an annotation: the greatest anchor in the AG
//    * such that there is a node with this anchor which structurally precedes
//    * the start node of annotation a. */
//  public Long greatestLowerBound(Annotation a);
//
//  /** Least upper bound on an annotation: the smallest anchor in the AG
//    * such that there is a node with this anchor is structurally preceded
//    * by the end node of annotation a. */
//  public Long leastUpperBound(Annotation a);
//
//  /** The set of annotations overlapping a */
//  public AnnotationGraph getOverlappingAnnotations(Annotation a);
//
//  /** The set of annotations included by a */
//  public AnnotationGraph getIncludedAnnotations(Annotation a);

  /** Get annotations by type */
  public AnnotationGraph getAnnotations(String type);

  /** Get annotations by type and features */
  public AnnotationGraph getAnnotations(String type, FeatureSet features);

  /** Get annotations by type and equivalence class */
  public AnnotationGraph getAnnotations(String type, String equivalenceClass);

  /** Get annotations by type and position. This is the set of annotations of
    * a particular type which share the smallest leastUpperBound that is >=
    * offset */
  public AnnotationGraph getAnnotations(String type, Long offset);

  /** Get annotations by type, features and offset */
  public AnnotationGraph getAnnotations(String type, FeatureSet features,
					Long offset);

  /** Get annotations by type, equivalence class and offset */
  public AnnotationGraph getAnnotations(String type, String equivalenceClass,
					Long offset);

  /**Creates a new node with the offset offset
  @param offset the offset in document where the node will point*/
  public Node putNodeAt(Long id,double offset)throws gate.util.InvalidOffsetException;
  /**Returns the Id of the annotation graph*/
  public Long getId();

  public Annotation newAnnotation(Long id, Node start, Node end, String type, String equivalenceClass);

  public Annotation newAnnotation(Long id,long start, long end, String type, String equivalenceClass);

} // interface AnnotationGraph
