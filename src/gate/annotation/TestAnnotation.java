/*
 *	TestAnnotation.java
 *
 *	Hamish Cunningham, 7/Feb/00
 *
 *	$Id$
 */

package gate.annotation;

import java.util.*;
import junit.framework.*;
import gate.*;
import gate.util.*;

/** Tests for the Annotation classes
  */
public class TestAnnotation extends TestCase
{
  /** Construction */
  public TestAnnotation(String name) { super(name); }

  /** A document */
  protected Document doc1;

  /** Fixture set up */
  public void setUp() {
    // doc1 = TestDocument.newDoc();
  } // setUp

  /** Test indexing by offset */
  public void testOffsetIndex() {
    AnnotationSet as = new AnnotationSetImpl(doc1);
    AnnotationSet asBuf;
    Integer newId;
    FeatureMap fm = new SimpleFeatureMapImpl();
    Annotation a;
    Node startNode;
    Node endNode;

    newId = as.add(new Long(10), new Long(20), "T", fm);
    assertEquals(newId.intValue(), 0);
    a = as.get(newId);

    startNode = a.getStartNode();
    endNode = a.getEndNode();
    assertEquals(startNode.getId().intValue(), 0);
    assertEquals(endNode.getId().intValue(), 1);
    assertEquals(startNode.getOffset().longValue(), 10);
    assertEquals(endNode.getOffset().longValue(), 20);

    newId = as.add(new Long(10), new Long(30), "T", fm);
    assertEquals(newId.intValue(), 1);
    a = as.get(newId);

    startNode = a.getStartNode();
    endNode = a.getEndNode();
    assertEquals(startNode.getId().intValue(), 0);
    assertEquals(endNode.getId().intValue(), 2);
    assertEquals(startNode.getOffset().longValue(), 10);
    assertEquals(endNode.getOffset().longValue(), 30);

    asBuf = as.get(new Long(10));
    assertEquals(asBuf.size(), 2);

  } // testOffsetIndex()

  /** Test type index */
  public void testTypeIndex() {
    AnnotationSet as = new AnnotationSetImpl(doc1);
    AnnotationSet asBuf;
    Integer newId;
    FeatureMap fm = new SimpleFeatureMapImpl();
    Annotation a;
    Node startNode;
    Node endNode;

    as.get("T"); // to trigger type indexing
    as.add(new Long(10), new Long(20), "T1", fm);    // 0
    as.add(new Long(10), new Long(20), "T2", fm);    // 1
    as.add(new Long(10), new Long(20), "T3", fm);    // 2
    as.add(new Long(10), new Long(20), "T1", fm);    // 3
    as.add(new Long(10), new Long(20), "T1", fm);    // 4
    as.add(new Long(10), new Long(20), "T1", fm);    // 5
    as.add(new Long(10), new Long(20), "T3", fm);    // 6
    as.add(new Long(10), new Long(20), "T1", fm);    // 7
    as.add(new Long(10), new Long(20), "T3", fm);    // 8
    as.add(new Long(10), new Long(20), "T1", fm);    // 9
    as.add(new Long(10), new Long(20), "T1", fm);    // 10

    asBuf = as.get("T");
    assertEquals(null, asBuf);

    asBuf = as.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = as.get("T2");
    assertEquals(1, asBuf.size());
    asBuf = as.get("T3");
    assertEquals(3, asBuf.size());

    // let's check that we've only got two nodes
    SortedSet sortedAnnots = new TreeSet();
    sortedAnnots.addAll( ((AnnotationSetImpl) as).annotsById.values() );
    Iterator iter = sortedAnnots.iterator();
    int idCounter = 0;
    while(iter.hasNext()) {
      a = (Annotation) iter.next();
      assertEquals(idCounter++, a.getId().intValue());

      startNode = a.getStartNode();
      endNode = a.getEndNode();
      assertEquals(0, startNode.getId().intValue());
      assertEquals(10, startNode.getOffset().longValue());
      assertEquals(1, startNode.getId().intValue());
      assertEquals(20, startNode.getOffset().longValue());
    }

  } // testTypeIndex()

  /** Test AnnotationSetImpl */
  public void testAnnotationSet() {
    // constuct an empty AS
    AnnotationSet as = new AnnotationSetImpl(doc1);
    assertEquals(as.size(), 0);

    // add some annotations
    Integer newId;
    newId =
      as.add(new Long(0), new Long(10), "Token", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 0);
    newId =
      as.add(new Long(11), new Long(12), "Token", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 1);
    assertEquals(as.size(), 2);
    assert(! as.isEmpty());
    newId =
      as.add(new Long(15), new Long(22), "Syntax", new SimpleFeatureMapImpl());

    // get by ID; remove; add(object)
    Annotation a = as.get(new Integer(1));
    as.remove(a);
    assertEquals(as.size(), 2);
    as.add(a);
    assertEquals(as.size(), 3);

    // iterate over the annotations
    Iterator iter = as.iterator();
    while(iter.hasNext()) {
      a = (Annotation) iter.next();
      if(a.getId().intValue() != 2)
        assertEquals(a.getType(), "Token");
      assertEquals(a.getFeatures().size(), 0);
    }

    // add some more
    newId =
      as.add(new Long(0), new Long(12), "Syntax", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 3);
    newId =
      as.add(new Long(14), new Long(22), "Syntax", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 4);
    assertEquals(as.size(), 5);
    newId =
      as.add(new Long(15), new Long(22), "Syntax", new SimpleFeatureMapImpl());


    // indexing by type
    as.indexByType();
    AnnotationSet tokenAnnots = as.get("Token");
    assertEquals(tokenAnnots.size(), 2);

    // indexing by position
    AnnotationSet annotsAfter10 = as.get(new Long(15));
    if(annotsAfter10 == null)
      fail("no annots found after offset 10");
    assertEquals(annotsAfter10.size(), 2);

  } // testAnnotationSet

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotation.class);
  } // main

} // class TestAnnotation
