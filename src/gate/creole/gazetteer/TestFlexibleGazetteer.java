/*
 *  TestFlexibleGazetteer.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Mike Dowman, 25/3/2004
 *
 *  $Id$
 */

package gate.creole.gazetteer;

import junit.framework.*;
import gate.*;
import gate.corpora.*;
import java.net.*;
import gate.gui.MainFrame;

public class TestFlexibleGazetteer extends TestCase {

  private static final boolean DEBUG=false;

  public TestFlexibleGazetteer(String name) {
    super(name);
  }

  /** Fixture set up - does nothing */
  public void setUp() throws Exception {
  }

  /** Fixture tear down - does nothing */
  public void tearDown() throws Exception {
  } // tearDown

  /** Tests the flexible gazetteer */
  public void testFlexibleGazetteer() throws Exception {

    // Display the gui for debugging purposes.
    if (DEBUG) {
      MainFrame mainFrame = new MainFrame();
      mainFrame.setVisible(true);
    }

    //get a document - take it from the gate server.
    // tests/doc0.html is a simple html document.
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );

    // Get a tokeniser - just use all the default settings.
    gate.creole.tokeniser.DefaultTokeniser tokeniser=
        (gate.creole.tokeniser.DefaultTokeniser) Factory.createResource(
        "gate.creole.tokeniser.DefaultTokeniser");

    // Get a morphological analyser, again just use all the default settings.
    gate.creole.morph.Morph morphologicalAnalyser=
        (gate.creole.morph.Morph) Factory.createResource(
        "gate.creole.morph.Morph");

    // Get a default gazetteer, again just use all the default settings
    gate.creole.gazetteer.Gazetteer gazetteerInst =
        (gate.creole.gazetteer.DefaultGazetteer) Factory.createResource(
        "gate.creole.gazetteer.DefaultGazetteer");

    //create a flexible gazetteer
    // First create a feature map containing all the relevant parameters.
    FeatureMap params = Factory.newFeatureMap();
    // Create a list of input features with just one feature (root) and add it
    // to the feature map.
    java.util.ArrayList testInputFeatures=new java.util.ArrayList();
    testInputFeatures.add("Token.root");
    params.put("inputFeatureNames", testInputFeatures);
    params.put("gazetteerInst",gazetteerInst);

    // Actually create the gazateer
    FlexibleGazetteer flexGaz = (FlexibleGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.FlexibleGazetteer", params);

    // runtime stuff - set the document to be used with the gazetteer, the
    // tokeniser and the analyser to doc, and run each of them in turn.
    tokeniser.setDocument(doc);
    tokeniser.execute();
    morphologicalAnalyser.setDocument(doc);
    morphologicalAnalyser.execute();
    flexGaz.setDocument(doc);
    flexGaz.execute();

    // Now check that the document has been annotated as expected.
    // First get the default annotations.
    AnnotationSet defaultAnnotations=doc.getAnnotations();

    // Now just get the lookups out of that set.
    AnnotationSet lookups=defaultAnnotations.get("Lookup");

    // And check that all the correct lookups have been found.
    // N.B. If the default gazetteer lists are ever changed, the correct value
    // for the number of lookups found may also change.

    if (DEBUG) {
      System.out.println("There are this many lookup annotations: "+
                         lookups.size());
    }
    assertTrue(lookups.size()== 40);

    // Now clean up so we don't get a memory leak.
    Factory.deleteResource(doc);
    Factory.deleteResource(tokeniser);
    Factory.deleteResource(morphologicalAnalyser);
    Factory.deleteResource(flexGaz);
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestFlexibleGazetteer.class);
  } // suite

  // The main class allows this class to be tested on its own, without the
  // need to call it from another class.
  public static void main(String[] args) {
    try{
      Gate.init();
      TestFlexibleGazetteer testGaz = new TestFlexibleGazetteer("");
      testGaz.setUp();
      testGaz.testFlexibleGazetteer();
      testGaz.tearDown();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main

} // TestFlexibleGazetteer
