/*
 *  TestPR.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Oana Hamza,
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;
import gnu.regexp.*;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;
import gate.persist.*;
import gate.annotation.*;

/** Test the PRs on three documents */
public class TestPR extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  protected static Document doc1;
  protected static Document doc2;
  protected static Document doc3;

  /** Construction */
  public TestPR(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    //get 3 documents
    if (doc1 == null)
      doc1 = Factory.newDocument(
        new URL(TestDocument.getTestServerName() +
        "tests/ft-bt-03-aug-2001.html")
      );

    if (doc2 == null)
      doc2 = Factory.newDocument(
        new URL(TestDocument.getTestServerName() +
          "tests/gu-Am-Brit-4-aug-2001.html")
      );

    if (doc3 == null)
      doc3 = Factory.newDocument(
        new URL(TestDocument.getTestServerName() +
          "tests/in-outlook-09-aug-2001.html")
      );

  } // setUp

  /** Put things back as they should be after running tests.
    */
  public void tearDown() throws Exception {
  } // tearDown

  public void testTokenizer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                    "gate.creole.tokeniser.DefaultTokeniser", params);


    //run the tokeniser for doc1
    tokeniser.setDocument(doc1);
    tokeniser.execute();
    assertTrue("Found in ft-bt-03-aug-2001.html "+ doc1.getAnnotations().size() +
      " Token annotations, instead of the expected 1286.",
      doc1.getAnnotations().size()== 1286);

    //run the tokeniser for doc2
    tokeniser.setDocument(doc2);
    tokeniser.execute();
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().size() +
      " Token annotations, instead of the expected 2144.",
      doc2.getAnnotations().size()== 2144);

    //run the tokeniser for doc3
    tokeniser.setDocument(doc3);
    tokeniser.execute();
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().size() +
      " Token annotations, instead of the expected 2812.",
      doc3.getAnnotations().size()== 2812);

    Factory.deleteResource(tokeniser);
  }// testTokenizer

  public void testGazetteer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    DefaultGazetteer gaz = (DefaultGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.DefaultGazetteer", params);

    //run gazetteer for doc1
    gaz.setDocument(doc1);
    gaz.execute();
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 47.",
      doc1.getAnnotations().get("Lookup").size()== 47);

    //run gazetteer for doc2
    gaz.setDocument(doc2);
    gaz.execute();
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 99.",
      doc2.getAnnotations().get("Lookup").size()== 99);

    //run gazetteer for doc3
    gaz.setDocument(doc3);
    gaz.execute();
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Lookup").size() +
      " Lookup annotations, instead of the expected 112.",
      doc3.getAnnotations().get("Lookup").size()== 112);
    Factory.deleteResource(gaz);
  }//testGazetteer

  public void testSplitter() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    SentenceSplitter splitter = (SentenceSplitter) Factory.createResource(
                          "gate.creole.splitter.SentenceSplitter", params);

    //run splitter for doc1
    splitter.setDocument(doc1);
    splitter.execute();
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 22.",
      doc1.getAnnotations().get("Sentence").size()== 22);

    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 36.",
      doc1.getAnnotations().get("Split").size()== 36);


    //run splitter for doc2
    splitter.setDocument(doc2);
    splitter.execute();
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 53.",
      doc2.getAnnotations().get("Sentence").size()== 53);

    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 71.",
      doc2.getAnnotations().get("Split").size()== 71);

    //run splitter for doc3
    splitter.setDocument(doc3);
    splitter.execute();

    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Sentence").size() +
      " Sentence annotations, instead of the expected 65.",
      doc3.getAnnotations().get("Sentence").size()== 65);

    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Split").size() +
      " Split annotations, instead of the expected 85.",
      doc3.getAnnotations().get("Split").size()== 85);
    Factory.deleteResource(splitter);
  }//testSplitter

  public void testTagger() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    POSTagger tagger = (POSTagger) Factory.createResource(
                          "gate.creole.POSTagger", params);


    //run the tagger for doc1
    tagger.setDocument(doc1);
    tagger.execute();

    HashSet fType = new HashSet();
    fType.add("category");
    AnnotationSet annots =
                  doc1.getAnnotations().get("Token", fType);

    assertTrue("Found in ft-bt-03-aug-2001.html "+ annots.size() +
      " Token annotations with category feature, instead of the expected 675.",
      annots.size() == 675);

    //run the tagger for doc2
    tagger.setDocument(doc2);
    tagger.execute();
    annots = doc2.getAnnotations().get("Token", fType);
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+ annots.size() +
      " Token annotations with category feature, instead of the expected 1131.",
      annots.size() == 1131);

    //run the tagger for doc3
    tagger.setDocument(doc3);
    tagger.execute();
    annots = doc3.getAnnotations().get("Token", fType);
    assertTrue("Found in in-outlook-09-aug-2001.html "+ annots.size() +
      " Token annotations with category feature, instead of the expected 1426.",
      annots.size() == 1426);
    Factory.deleteResource(tagger);
  }//testTagger()

  public void testTransducer() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    ANNIETransducer transducer = (ANNIETransducer) Factory.createResource(
                          "gate.creole.ANNIETransducer", params);

    //run the transducer for doc1
    transducer.setDocument(doc1);
    transducer.execute();
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 16",
      doc1.getAnnotations().get("Organization").size()== 16);
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 3",
      doc1.getAnnotations().get("Location").size()== 3);
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 3",
      doc1.getAnnotations().get("Person").size()== 3);
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 6",
      doc1.getAnnotations().get("Date").size()== 6);
    assertTrue("Found in ft-bt-03-aug-2001.html "+
      doc1.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 1",
      doc1.getAnnotations().get("Money").size()== 1);

    //run the transducer for doc2
    transducer.setDocument(doc2);
    transducer.execute();
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 18",
      doc2.getAnnotations().get("Organization").size()== 18);
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 9",
      doc2.getAnnotations().get("Location").size()== 9);
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 1",
      doc2.getAnnotations().get("Person").size()== 1);
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 6",
      doc2.getAnnotations().get("Date").size()== 6);
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+
      doc2.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 3",
      doc2.getAnnotations().get("Money").size()== 3);

    //run the transducer for doc3
    transducer.setDocument(doc3);
    transducer.execute();
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Organization").size() +
      " Organization annotations, instead of the expected 9",
      doc3.getAnnotations().get("Organization").size()== 9);
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Location").size() +
      " Location annotations, instead of the expected 12",
      doc3.getAnnotations().get("Location").size()== 12);
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Person").size() +
      " Person annotations, instead of the expected 8",
      doc3.getAnnotations().get("Person").size()== 8);
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Date").size() +
      " Date annotations, instead of the expected 7",
      doc3.getAnnotations().get("Date").size()== 7);
    assertTrue("Found in in-outlook-09-aug-2001.html "+
      doc3.getAnnotations().get("Money").size() +
      " Money annotations, instead of the expected 4",
      doc3.getAnnotations().get("Money").size()== 4);

    Factory.deleteResource(transducer);
  }//testTransducer

  public void testOrthomatcher() throws Exception {
    FeatureMap params = Factory.newFeatureMap();

    OrthoMatcher orthomatcher = (OrthoMatcher) Factory.createResource(
                          "gate.creole.orthomatcher.OrthoMatcher", params);


    // run the orthomatcher for doc1
    orthomatcher.setDocument(doc1);
    orthomatcher.execute();

    HashSet fType = new HashSet();
    fType.add("matches");
    AnnotationSet annots =
                  doc1.getAnnotations().get(null,fType);

    assertTrue("Found in ft-bt-03-aug-2001.html "+ annots.size() +
      " annotations with matches feature, instead of the expected 29.",
      annots.size() == 29);

    //run the orthomatcher for doc2
    orthomatcher.setDocument(doc2);
    orthomatcher.execute();
    annots = doc2.getAnnotations().get(null,fType);
    assertTrue("Found in gu-Am-Brit-4-aug-2001.html "+ annots.size() +
      " annotations with matches feature, instead of the expected 35.",
      annots.size() == 33);

    //run the orthomatcher for doc3
    orthomatcher.setDocument(doc3);
    orthomatcher.execute();

    annots = doc3.getAnnotations().get(null,fType);
    assertTrue("Found in in-outlook-09-aug-2001.html "+ annots.size() +
      " annotations with matches feature, instead of the expected 20.",
      annots.size() == 20);
    Factory.deleteResource(orthomatcher);
  }//testOrthomatcher

  /** A test for comparing the annotation sets*/
  public void testAllPR() throws Exception {

    // create annotation schema
    AnnotationSchema annotationSchema = new AnnotationSchema();

    // verify if the saved data store is the same with the just processed file
    // first document
    String urlBaseName = Gate.locateGateFiles();


//    RE re1 = new RE("build/gate.jar!");
//    RE re2 = new RE("jar:");
//    urlBaseName = re1.substituteAll( urlBaseName,"classes");
//    urlBaseName = re2.substituteAll( urlBaseName,"");


    if (urlBaseName.endsWith("/gate/build/gate.jar!/")) {
      StringBuffer buff = new StringBuffer(
                            urlBaseName.substring(
                              0,
                              urlBaseName.lastIndexOf("build/gate.jar!/"))
                            );
      buff.append("classes/");
      buff.delete(0, "jar:file:".length());
      buff.insert(0, "file://");
      urlBaseName = buff.toString();
    }

    URL urlBase = new URL(urlBaseName + "gate/resources/gate.ac.uk/");

    URL storageDir = null;
    storageDir = new URL(urlBase, "tests/ft");

    //open the data store
    DataStore ds = Factory.openDataStore
                    ("gate.persist.SerialDataStore",
                     storageDir.toExternalForm());

    //get LR id
    String lrId = (String)ds.getLrIds
                                ("gate.corpora.DocumentImpl").get(0);

    // get the document from data store
    FeatureMap features = Factory.newFeatureMap();
    features.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
    Document document = (Document) Factory.createResource(
                                      "gate.corpora.DocumentImpl",
                                      features);
    // get annotation set
    AnnotationSet annotSet = document.getAnnotations();
    // get the annotation set from the first processed document
    AnnotationSet annotSet1 = doc1.getAnnotations();
    // compare the annotation set

    // organization type
    annotationSchema.setAnnotationName("Organization");
    // create an annotation diff
    FeatureMap parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    AnnotationDiff annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Organization precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Organization recall average in ft-bt-03-aug-2001.html is " +
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Organization f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // location type
    annotationSchema.setAnnotationName("Location");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Location precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Location recall average in ft-bt-03-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Location f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // person type
    annotationSchema.setAnnotationName("Person");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Person precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Person recall average in ft-bt-03-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Person f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

     // date type
    annotationSchema.setAnnotationName("Date");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Date precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Date recall average in ft-bt-03-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Date f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // money type
    annotationSchema.setAnnotationName("Money");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Money precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Money recall average in ft-bt-03-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Money f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // Lookup type
    annotationSchema.setAnnotationName("Lookup");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Lookup precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Lookup recall average in ft-bt-03-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Lookup f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // Token type
    annotationSchema.setAnnotationName("Token");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc1);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Token precision average in ft-bt-03-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Token recall average in ft-bt-03-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Token f-measure average in ft-bt-03-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // second document
    storageDir = null;
    storageDir = new URL(urlBase, "tests/gu");

    //open the data store
    ds = Factory.openDataStore("gate.persist.SerialDataStore",
                               storageDir.toExternalForm());
    //get LR id
    lrId = (String)ds.getLrIds("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    features = Factory.newFeatureMap();
    features.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
    document = (Document) Factory.createResource(
                                      "gate.corpora.DocumentImpl",
                                      features);
    // get annotation set
    annotSet = document.getAnnotations();

    // get the annotation set from the second processed document
    AnnotationSet annotSet2 = doc2.getAnnotations();


    // organization type
    annotationSchema.setAnnotationName("Organization");
    // create an annotation diff
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Organization precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Organization recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Organization f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // location type
    annotationSchema.setAnnotationName("Location");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Location precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Location recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Location f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // person type
    annotationSchema.setAnnotationName("Person");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Person precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Person recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Person f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

     // date type
    annotationSchema.setAnnotationName("Date");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Date precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Date recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Date f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // money type
    annotationSchema.setAnnotationName("Money");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Money precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Money recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Money f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // Lookup type
    annotationSchema.setAnnotationName("Lookup");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Lookup precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Lookup recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Lookup f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // Token type
    annotationSchema.setAnnotationName("Token");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc2);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Token precision average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Token recall average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Token f-measure average in gu-Am-Brit-4-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // third document
    storageDir = null;
    storageDir = new URL(urlBase, "tests/in");

    //open the data store
    ds = Factory.openDataStore("gate.persist.SerialDataStore",
                               storageDir.toExternalForm());
    //get LR id
    lrId = (String)ds.getLrIds("gate.corpora.DocumentImpl").get(0);
    // get the document from data store
    features = Factory.newFeatureMap();
    features.put(DataStore.DATASTORE_FEATURE_NAME, ds);
    features.put(DataStore.LR_ID_FEATURE_NAME, lrId);
    Document doc = (Document) Factory.createResource(
                                "gate.corpora.DocumentImpl",
                                features);
    // get annotation set
    annotSet = document.getAnnotations();
    // get the annotation set from the third processed document
    AnnotationSet annotSet3 = doc3.getAnnotations();

    // organization type
    annotationSchema.setAnnotationName("Organization");
    // create an annotation diff
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Organization precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Organization recall average in in-outlook-09-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Organization f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // location type
    annotationSchema.setAnnotationName("Location");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Location precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);

    assertTrue("Location recall average in in-outlook-09-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);

    assertTrue("Location f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // person type
    annotationSchema.setAnnotationName("Person");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Person precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Person recall average in in-outlook-09-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Person f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

     // date type
    annotationSchema.setAnnotationName("Date");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Date precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Date recall average in in-outlook-09-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Date f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // money type
    annotationSchema.setAnnotationName("Money");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Money precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Money recall average in in-outlook-09-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Money f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // Lookup type
    annotationSchema.setAnnotationName("Lookup");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Lookup precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Lookup recall average in in-outlook-09-aug-2001.html is "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Lookup f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);

    // Token type
    annotationSchema.setAnnotationName("Token");
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",document);
    parameters.put("responseDocument",doc3);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);

    // Create Annotation Diff visual resource
    annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assertTrue("Token precision average in in-outlook-09-aug-2001.html is "+
      annotDiff.getPrecisionAverage()+ " instead of 1.0 ",
      annotDiff.getPrecisionAverage()== 1.0);
    assertTrue("Token recall average is in in-outlook-09-aug-2001.html "+
      annotDiff.getRecallAverage()+ " instead of 1.0 ",
      annotDiff.getRecallAverage()== 1.0);
    assertTrue("Token f-measure average in in-outlook-09-aug-2001.html is "+
      annotDiff.getFMeasureAverage()+ " instead of 1.0 ",
      annotDiff.getFMeasureAverage()== 1.0);
  } // testAllPR()


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestPR.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestPR testPR = new TestPR("");
      testPR.setUp();
      testPR.testTokenizer();
      testPR.testGazetteer();
      testPR.testSplitter();
      testPR.testTagger();
      testPR.testTransducer();
      testPR.testOrthomatcher();
      testPR.testAllPR();
      testPR.tearDown();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main
} // class TestPR