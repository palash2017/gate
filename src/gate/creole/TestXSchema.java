/*
 *	TestXSchema.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *	Cristian URSU, 11/Octomber/2000
 *
 *	$Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;

import junit.framework.*;

import gate.*;
import gate.util.*;

/** Annotation schemas test class.
  */
public class TestXSchema extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestXSchema(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testFromAndToXSchema() throws Exception {

    ResourceData resData = (ResourceData)
      Gate.getCreoleRegister().get("gate.creole.AnnotationSchema");

    FeatureMap parameters = Factory.newFeatureMap();
    parameters.put("xmlFileUrl", resData.getXmlFileUrl());

    AnnotationSchema annotSchema = (AnnotationSchema)
      Factory.createResource("gate.creole.AnnotationSchema", parameters);

    // Create an annoatationSchema from a URL.
    URL url = Gate.getUrl("tests/xml/POSSchema.xml");
    annotSchema.fromXSchema(url);

    String s = annotSchema.toXSchema();
    // write back the XSchema fom memory
    // File file = Files.writeTempFile(new ByteArrayInputStream(s.getBytes()));
    // load it again.
    //annotSchema.fromXSchema(file.toURL());
    annotSchema.fromXSchema(new ByteArrayInputStream(s.getBytes()));
  } // testFromAndToXSchema()

  /** Test creation of annotation schemas via gate.Factory */
  public void testFactoryCreation() throws Exception {

    ResourceData resData = (ResourceData)
      Gate.getCreoleRegister().get("gate.creole.AnnotationSchema");

    FeatureMap parameters = Factory.newFeatureMap();
    parameters.put("xmlFileUrl", resData.getXmlFileUrl());

    AnnotationSchema schema = (AnnotationSchema)
      Factory.createResource("gate.creole.AnnotationSchema", parameters);

    if(DEBUG) {
      Out.prln("schema RD: " + resData);
      Out.prln("schema: " + schema);
    }

  } // testFactoryCreation()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXSchema.class);
  } // suite

} // class TestXSchema