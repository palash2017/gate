/*
 *  STreeNode.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 20/09/2000
 *
 *  $Id$
 */

package gate.gui;

import javax.swing.tree.*;

import java.awt.Rectangle;

import java.util.Iterator;
import java.util.HashSet;
import java.util.Vector;

import gate.*;
import gate.util.*;


public class STreeNode extends DefaultMutableTreeNode {

  /** Debug flag */
  private static final boolean DEBUG = false;

  static int nextID = 0;

  int level;            // level in the syntax tree
  int nodeID;           //ID of the node

  int start, end;       //the start and end nodes for this annotation
  Annotation annot;     //the annotation that's created during import/export
  											//not to be used otherwise. During import span is set to
                        //be the same as the annotation span. During export the
                        //annotation span is set to be the same as the span.

  public STreeNode(Annotation annot) {
    level = -1;
    nodeID = nextID++;
    //span = annot.getSpans().getElementAt(0);
    //get the first span, there should be no others
		this.annot = annot;
  }// public STreeNode(Annotation annot)

  public STreeNode(int start, int end) {
    level = -1;
    nodeID = nextID++;
    this.start = start;
    this.end = end;
  }// public STreeNode(int start, int end)

  public STreeNode() {
    level = -1;
    nodeID = nextID++;
    start = 0;
    end = 0;
  }// public STreeNode()

  public int getLevel() {
    return level;
  }// public int getLevel()

  public void setLevel(int level) {
    this.level = level;
  }// public void setLevel(int level)

  public int getID() {
    return nodeID;
  }// public int getID()

  public int getStart() {
    return start;
  }// public int getStart()

  public void setStart(int start) {
    this.start = start;
  }// public void setStart(int start)

  public int getEnd() {
    return end;
  }// public int getEnd()

  public void setEnd(int end) {
    this.end = end;
  }// public void setEnd(int end)

  /**
    * This also sets the span to match the annotation span!
    */
  public void setAnnotation(Annotation annot) {
		this.annot = annot;
    this.start = annot.getStartNode().getOffset().intValue();
    this.end = annot.getEndNode().getOffset().intValue();
  }// public void setAnnotation(Annotation annot)

  public Annotation getAnnotation() {
  	return annot;
  }// public Annotation getAnnotation()

  public void disconnectChildren() {
    for (Iterator i = this.children.iterator(); i.hasNext(); )
      ((STreeNode) i.next()).setParent(null);
    this.children.clear();
  }// public void disconnectChildren()

  /**
    * Creates an annotation of the given type. If the children don't have their
    * annotation objects created, it creates them and assigns the pointers.
    * Expects the text string relative to which all offsets were created!
    */
  public boolean createAnnotation(Document doc, String type,
                                    String text, int utteranceOffset) {
  	boolean created = false;

    if (annot != null )
    	return false;

    // check if it has children. If it hasn't then it shouldn't have an
    // annotation because all our leaf nodes are actually just words
    // from the text (e.g. "this", "that"). Their categories are always
    // encoded as non-terminal nodes.
    if ( ! this.getAllowsChildren())
			return false;

    FeatureMap attribs = Factory.newFeatureMap();
    // the text spanned by the annotation is stored as the userObject of the
    // tree node
    // comes from the default Swing tree node
    Vector consists = new Vector();

    attribs.put("text",
                  text.substring(start - utteranceOffset,
                                 end - utteranceOffset)
    );
    attribs.put("cat", (String) this.getUserObject());
    attribs.put("consists", consists);

    // children comes from DefaultMutableTreeNode
    for (Iterator i = children.iterator(); i.hasNext(); ) {
    	STreeNode child = (STreeNode) i.next();
      if (child.getAnnotation() == null) {
      	if (child.getAllowsChildren())
	  	    if (createAnnotation(doc, type, text, utteranceOffset))
            consists.add(child.getAnnotation().getId());
      } else
			  consists.add(child.getAnnotation().getId());
    }

    //!!! Need to account for the name of the Annot Set
    AnnotationSet theSet = doc.getAnnotations();
    try {
      Integer Id = theSet.add(new Long(start), new Long(end), type, attribs);
      this.annot = theSet.get(Id);
      created = true;
    } catch (InvalidOffsetException ex) {
      Out.println("Invalid annotation offsets: "
                            + start + " and/or " + end);
      created = false;
    }

    return created;
  }// public boolean createAnnotation

  /** Store the annotation in the deleted list so it can retrieved later */
  public void removeAnnotation(Document doc) {
  	if (this.annot == null)
    	return;

    doc.getAnnotations().remove(this.annot);

    this.annot = null;
  }//  public void removeAnnotation(Document doc)

} // STreeNode

// $Log$
// Revision 1.7  2001/04/09 10:36:36  oana
// a few changes in the code style
//
// Revision 1.6  2000/11/08 16:35:00  hamish
// formatting
//
// Revision 1.5  2000/10/26 10:45:25  oana
// Modified in the code style
//
// Revision 1.4  2000/10/18 13:26:47  hamish
// Factory.createResource now working, with a utility method that uses
// reflection (via java.beans.Introspector) to set properties on a resource
// from the
//     parameter list fed to createResource.
//     resources may now have both an interface and a class; they are indexed
//        by interface type; the class is used to instantiate them
//     moved createResource from CR to Factory
//     removed Transients; use Factory instead
//
// Revision 1.3  2000/10/16 16:44:32  oana
// Changed the comment of DEBUG variable
//
// Revision 1.2  2000/10/10 15:36:34  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.1  2000/09/20 17:03:37  kalina
// Added the tree viewer from the prototype. It works now with the new
// annotation API.
//
// Revision 1.6  1999/08/23 14:13:38  kalina
// Fixed resizing bugs in tree viewers
//
// Revision 1.5  1999/08/20 21:11:56  kalina
// Fixed most bugs and TreeViewer can now import and export annotations
// correctly
// There is still a delete bug somewhere.
//
// Revision 1.4  1999/08/18 17:55:24  kalina
// Added annotation export for the TreeViewer. Annotation import is the only
// thing that remains.
//
// Revision 1.3  1999/08/13 17:56:31  kalina
// Fixed the annotation of nodes in the TreeViewer to be done with click
//
// Revision 1.2  1999/08/12 16:10:12  kalina
// Added a new tree stereotype. Not in final version but would do for testing.
//
// Improved the tree viewer to allow dynamic creation of all nodes.
// Now I can build many trees or one tree; can delete non-terminal nodes;
// select/unselect nodes for annotation
// Overlapping trees are not a big problem too :-) Not wonderfully drawn but
// would do.
//
// Revision 1.1  1999/08/09 18:00:53  kalina
// Made the tree viewer to display an utterance/sentence annotation to start annotating them
//
